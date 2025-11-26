package com.mycompany.parchis_demo.control;

import com.mycompany.parchis_demo.control.red.ProxyCliente;
import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.Ficha;
import com.mycompany.parchis_demo.modelo.Jugador;
import com.mycompany.parchis_demo.modelo.Partida;
import com.mycompany.parchis_demo.modelo.ResultadoTurno;
import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;

import java.util.HashMap;
import java.util.Map;

public class ControladorTurno {

    private final Partida partida;
    final ProxyCliente proxy;  // paquete-privado para que VistaJugador lo pueda usar en main si quiere

    // Contador de seises por jugador (3 seises → pierde turno)
    private final Map<Integer, Integer> seisesSeguidos = new HashMap<>();

    // último error de validación
    private String ultimoErrorMovimiento = null;

    public ControladorTurno(Partida partida, ProxyCliente proxy) {
        this.partida = partida;
        this.proxy = proxy;
    }

    // ======================================================
    // 1) Tirar dado (la vista llama esto ANTES de elegir ficha)
    // ======================================================

    /** Lanza el dado de la partida y devuelve su valor (1..6). */
    public int tirarDado() {
        return partida.getDado().lanzar();
    }
    
    /**
    * Método para compatibilidad con Broker (cuando el broker lanza el dado). 
    * Lanza el dado internamente y delega a procesarTurnoConValor. 
    */
   public ResultadoTurno procesarTurno(Jugador jugador, int idFicha) {
       int valorDado = tirarDado();  // Lanza el dado aquí
       return procesarTurnoConValor(jugador, idFicha, valorDado);
   }

    // ======================================================
    // 2) Procesar turno usando el valor YA lanzado
    // ======================================================

    /**
     * El dado ya fue lanzado en la vista. Aquí solo usamos ese valor.
     * Reglas:
     *  - Si ficha en base y dado 1–4: no se mueve, se pierde el turno y pasa al siguiente.
     *  - Si ficha en base y dado 5: sale a su casilla de salida.
     *  - Dado 6: turno extra (salvo que sea el tercer seis seguido).
     *  - Capturas usando Tablero.hayRivalSolo/ buscarRivalEn.
     */
    public ResultadoTurno procesarTurnoConValor(Jugador jugador, int idFicha, int valorDado) {
        Ficha ficha = jugador.seleccionarFicha(idFicha);

        if (ficha == null) {
            return new ResultadoTurno(false, "Ficha no encontrada", false, false, valorDado);
        }

        // Caso especial: ficha en base + dado < 5  => no movimiento, turno se consume
        boolean sinMovimientoBase = ficha.estaEnBase() && valorDado < 5;

        // Solo validamos normal si NO estamos en el caso base+<5
        if (!sinMovimientoBase && !validarMovimiento(ficha, valorDado, jugador)) {
            String msg = (ultimoErrorMovimiento != null)
                    ? ultimoErrorMovimiento
                    : "Movimiento inválido";
            return new ResultadoTurno(false, msg, false, false, valorDado);
        }

        // ===== Reglas de seises =====
        int s = seisesSeguidos.getOrDefault(jugador.getId(), 0);
        if (valorDado == 6) s++; else s = 0;
        seisesSeguidos.put(jugador.getId(), s);

        boolean pierdeTurno = false;
        boolean turnoExtra  = false;

        if (s >= 3) {           // 3 seises seguidos -> pierde turno
            pierdeTurno = true;
            turnoExtra = false;
            seisesSeguidos.put(jugador.getId(), 0);
        } else if (valorDado == 6) {
            turnoExtra = true;  // 6 → turno extra
        }

        // ===== Aplicar movimiento (o “no movimiento”) =====
        int desde;
        int hasta;

        if (sinMovimientoBase) {
            // En base + dado < 5: no se mueve, pero el turno pasa al siguiente jugador
            desde = ficha.getPosicion(); // normalmente 0 o lo que use tu Ficha para base
            hasta = desde;
        } else if (ficha.estaEnBase()) {
            // Salir de base con 5
            Color c = jugador.getColor();
            desde = ficha.getPosicion();
            hasta = partida.getTablero().casillaSalida(c);
        } else {
            // Movimiento normal en el anillo (0..67 con wrap)
            desde = ficha.getPosicion();
            hasta = partida.getTablero().calcularNuevaPosicion(ficha, valorDado);

            // Si quieres que META sea “68” lógica, puedes, por ejemplo,
            // decidir aquí que cuando cumpla la vuelta completa y reste poco
            // lo mandes a una representación especial, etc.
            // De momento respetamos el anillo 0..67 del Tablero.
        }

        // Actualiza posición de la ficha en el modelo
        ficha.setPosicion(hasta);

        // ===== Capturas (solo si realmente avanzó) =====
        boolean huboCaptura = false;
        if (!sinMovimientoBase && !ficha.estaEnBase()) {
            huboCaptura = verificarCaptura(ficha, hasta);
        }

        // ===== Evento hacia el broker =====
        String texto;
        if (sinMovimientoBase) {
            texto = jugador.getNombre() + " (J" + jugador.getId() + ") lanzó " + valorDado
                    + " pero no puede salir de base y pierde el turno";
            // Ojo: pierdeTurno lo maneja el broker según flags; aquí lo dejamos
            // como “turnoExtra = false” y “pierdeTurno = false”,
            // y el broker ya avanza al siguiente al no haber turnoExtra.
        } else {
            texto = jugador.getNombre() + " (J" + jugador.getId() + ") lanzo " + valorDado +
                    " y movio su ficha de " + formatoPosicion(desde) +
                    " a " + formatoPosicion(hasta);
        }

        EventoPartida ev = new EventoPartida(
                TipoEvento.FICHA_MOVIDA,
                jugador,
                ficha,
                valorDado,
                desde,
                hasta,
                texto
        );
        ev.setTurnoExtra(turnoExtra);
        ev.setPierdeTurno(pierdeTurno);

        if (proxy != null) {
            proxy.enviarEvento(ev);
        }
        partida.notificarObservadores(ev);

        // Para la Vista:
        //  - exito siempre true (en el caso base+<5 no mostramos "Movimiento inválido")
        //  - turnoExtra controla si repite o pasa al siguiente
        return new ResultadoTurno(true, texto, turnoExtra, huboCaptura, valorDado);
    }

    // ======================================================
    // 3) Validación usando tu Tablero
    // ======================================================

    private boolean validarMovimiento(Ficha ficha, int pasos, Jugador jugador) {
        ultimoErrorMovimiento = null;

        // En meta: ya no puede mover
        if (ficha.estaEnMeta()) {
            ultimoErrorMovimiento = "La ficha ya está en meta";
            return false;
        }

        // En base
        if (ficha.estaEnBase()) {
            if (pasos < 5) {
                // Aquí NO es error: se considera "sin movimiento" y se maneja en procesarTurnoConValor
                return true;
            }
            // pasos >= 5 -> delegamos la validación al Tablero
            boolean ok = partida.getTablero().movimientoLegal(jugador, ficha, pasos);
            if (!ok) {
                ultimoErrorMovimiento = "Para salir de base necesitas un 5 y que la salida esté libre";
            }
            return ok;
        }

        // Fichas que ya están en el anillo / pasillo
        boolean ok = partida.getTablero().movimientoLegal(jugador, ficha, pasos);
        if (!ok) {
            ultimoErrorMovimiento = "Movimiento inválido (barrera, casilla segura o fuera de rango)";
        }
        return ok;
    }
    //Esto para mostrar las posiciones en texto
    private String formatoPosicion(int p) {
        if (p < 0) return "BASE";
        if (p >= 68) return "META";
        return "A" + p;
    }

    // ======================================================
    // 4) Iniciar partida bajo solicitud (cliente -> broker)
    // ======================================================

    public void iniciarPartidaSiPosible() {
        if (proxy != null) {
            proxy.enviarEvento(new EventoPartida(
                    TipoEvento.SOLICITAR_INICIO,
                    null,
                    "Solicita iniciar la partida"
            ));
        }
    }

    // ======================================================
    // 5) Snapshot opcional (si quieres mostrar desde el modelo)
    // ======================================================

    public String snapshotEstadoGlobal() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== Estado de la partida =====\n");
        for (Jugador j : partida.getJugadores()) {
            sb.append(j.getNombre()).append(" (").append(j.getColor()).append("): ");
            for (Ficha f : j.getFichas()) {
                int p = f.getPosicion();
                String pos;
                if (p <= 0) pos = "BASE";
                else if (p >= 68) pos = "META";
                else pos = "A" + p;
                sb.append(" f").append(f.getId()).append("=").append(pos).append("  ");
            }
            sb.append("\n");
        }
        sb.append("================================\n");
        return sb.toString();
    }

    // ======================================================
    // 6) Capturas usando el Tablero
    // ======================================================

    private boolean verificarCaptura(Ficha fichaQueLlega, int nuevaPosicion) {
        if (partida.getTablero().hayRivalSolo(nuevaPosicion, fichaQueLlega.getColor())) {
            Ficha victima = partida.getTablero().buscarRivalEn(nuevaPosicion, fichaQueLlega.getColor());
            if (victima != null) {
                procesarCaptura(fichaQueLlega, victima);
                return true;
            }
        }
        return false;
    }

    private void procesarCaptura(Ficha atacante, Ficha victima) {
        victima.volverACasa();

        EventoPartida evento = new EventoPartida(
                TipoEvento.CAPTURA,
                null,
                atacante,
                0,
                0,
                atacante.getPosicion(),
                atacante.getColor() + " capturó una ficha de " + victima.getColor()
        );

        if (proxy != null) {
            proxy.enviarEvento(evento);
        }
        partida.notificarObservadores(evento);
    }
}
