package com.mycompany.parchis_demo.control;

import com.mycompany.parchis_demo.control.red.ProxyCliente;
import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.Ficha;
import com.mycompany.parchis_demo.modelo.Jugador;
import com.mycompany.parchis_demo.modelo.Partida;
import com.mycompany.parchis_demo.modelo.ResultadoTurno;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;

import java.util.HashMap;
import java.util.Map;

public class ControladorTurno {

    private final Partida partida;
    private final ProxyCliente proxy;

    // Contador de seises por jugador (para “3 seises pierde turno”)
    private final Map<Integer, Integer> seisesSeguidos = new HashMap<>();

    public ControladorTurno(Partida partida, ProxyCliente proxy) {
        this.partida = partida;
        this.proxy = proxy;
    }

    /**
     * Procesa el turno del jugador sobre la ficha indicada.
     * - Lanza el dado.
     * - Valida el movimiento.
     * - Aplica movimiento y verifica captura.
     * - Emite un único evento FICHA_MOVIDA al broker con flags de turnoExtra/pierdeTurno.
     * - NO cambia de turno aquí. El broker decide con base en esos flags.
     */
    public ResultadoTurno procesarTurno(Jugador jugador, int idFicha) {
        int valorDado = partida.getDado().lanzar();

        Ficha ficha = jugador.seleccionarFicha(idFicha);
        if (ficha == null) {
            return new ResultadoTurno(false, "Ficha no encontrada", false, false, valorDado);
        }

        if (!validarMovimiento(ficha, valorDado)) {
            return new ResultadoTurno(false, "Movimiento inválido", false, false, valorDado);
        }

        // === Reglas de seises (contador por jugador) ===
        int s = seisesSeguidos.getOrDefault(jugador.getId(), 0);
        if (valorDado == 6) s++; else s = 0;
        seisesSeguidos.put(jugador.getId(), s);

        boolean pierdeTurno = false;
        boolean turnoExtra   = false;

        if (s >= 3) {                // 3 seises seguidos => pierde turno (y se resetea)
            pierdeTurno = true;
            turnoExtra = false;
            seisesSeguidos.put(jugador.getId(), 0);
        } else if (valorDado == 6) { // exactamente un 6 (no es el 3ro) => repite
            turnoExtra = true;
        }

        // === Aplicar movimiento en el modelo local ===
        int desde = ficha.getPosicion();
        int hasta = partida.getTablero().calcularNuevaPosicion(ficha, valorDado);
        ficha.setPosicion(hasta);

        // === Verificar captura (y emitir evento CAPTURA si sucede) ===
        boolean huboCaptura = verificarCaptura(ficha, hasta);

        // === Emitir UN evento FICHA_MOVIDA al broker con flags ===
        EventoPartida ev = new EventoPartida(
                TipoEvento.FICHA_MOVIDA,
                jugador,
                ficha,
                valorDado,
                desde,
                hasta,
                jugador.getNombre() + " (J" + jugador.getId() + ") lanzo " + valorDado +
                        " y movio su ficha de " + desde + " a " + hasta
        );
        ev.setTurnoExtra(turnoExtra);
        ev.setPierdeTurno(pierdeTurno);

        if (proxy != null) {
            proxy.enviarEvento(ev);
        }
        partida.notificarObservadores(ev);

        return new ResultadoTurno(true, "Ficha movida", turnoExtra, huboCaptura, valorDado);
    }

    /**
     * Validación mínima: no mover si ya está en meta.
     * (Si tu Tablero tiene reglas más ricas, puedes delegar: tablero.movimientoLegal(j,f,pasos))
     */
    public boolean validarMovimiento(Ficha ficha, int casillas) {
        return !ficha.estaEnMeta();
    }

    /**
     * Solicita iniciar partida al broker (la decisión la toma el broker).
     */
    public void iniciarPartidaSiPosible() {
        if (proxy != null) {
            proxy.enviarEvento(new EventoPartida(
                    TipoEvento.SOLICITAR_INICIO,
                    null,
                    "Solicita iniciar la partida"
            ));
        }
    }

    /**
     * Retorna un snapshot textual del estado (para imprimir en consola si lo deseas).
     * Asume:
     *  - posicion==0 => BASE
     *  - 0<pos<68   => A{pos} (anillo)
     *  - pos>=68    => META
     */
    public String snapshotEstadoGlobal() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== Estado de la partida =====\n");
        for (Jugador j : partida.getJugadores()) {
            sb.append(j.getNombre()).append(" (").append(j.getColor()).append("): ");
            for (Ficha f : j.getFichas()) {
                String pos;
                int p = f.getPosicion();
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

    /**
     * Verifica si en la casilla hay exactamente un rival y NO es segura para capturar.
     * Si hay captura, envía el evento CAPTURA y devuelve true.
     */
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

    /**
     * Procesa una captura: la víctima vuelve a casa y se emite el evento CAPTURA.
     */
    private void procesarCaptura(Ficha atacante, Ficha victima) {
        victima.volverACasa();

        EventoPartida evento = new EventoPartida(
                TipoEvento.CAPTURA,
                null,
                atacante,
                0,
                0,
                atacante.getPosicion(),
                atacante.getColor() + " capturo una ficha de " + victima.getColor()
        );

        if (proxy != null) {
            proxy.enviarEvento(evento);
        }
        partida.notificarObservadores(evento);
    }
}
