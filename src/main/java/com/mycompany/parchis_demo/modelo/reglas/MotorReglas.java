package com.mycompany.parchis_demo.modelo.reglas;

import com.mycompany.parchis_demo.modelo.*;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;

import java.util.*;
import java.util.function.Consumer;

public class MotorReglas {

    private final List<Regla> reglas = new ArrayList<>();

    /** Reglas oficiales segun PDF (sin bonos) */
    public static MotorReglas defaultRules() {
        return new MotorReglas()
            .registrar(new ReglaSacarConCinco())      // 10: salida con 5 (con posible captura)
            .registrar(new ReglaPasilloYMeta())       // 20: entrada a pasillo y avance/meta exacta
            .registrar(new ReglaMovimientoBloqueos()); // 30: movimiento anillo + captura simple
    }

    public MotorReglas registrar(Regla r) {
        reglas.add(r);
        reglas.sort(Comparator.comparingInt(Regla::prioridad));
        return this;
    }

    /** Ejecuta reglas hasta que una consuma el movimiento. */
    public boolean procesarMovimiento(Partida p, Jugador j, Ficha f, int pasos,
                                      Consumer<EventoPartida> emitir) {
        for (Regla r : reglas) {
            if (r.aplica(p, j, f, pasos)) {
                var res = r.ejecutar(p, j, f, pasos, emitir);
                if (res.consumioMovimiento) return true;
            }
        }
        return false;
    }

    /** ¿Existe al menos una ficha movible con ese tiro? */
    public boolean existeMovimiento(Partida p, Jugador j, int pasos) {
        for (Ficha f : j.getFichas()) {
            if (f.estaEnMeta()) continue;

            // salida con 5
            if (f.estaEnBase() && pasos == 5) {
                int salida = p.getTablero().casillaSalida(j.getColor());
                if (p.getTablero().movimientoLegal(j, f, pasos)) return true;
            }

            // anillo
            if (f.estaEnAnillo() && p.getTablero().movimientoLegal(j, f, pasos)) return true;

            // pasillo/meta (exactitud)
            if (f.estaEnPasillo() && p.getTablero().avanzarEnPasillo(f, pasos) >= 0) return true;
        }
        return false;
    }

    /** Penalización: última ficha vuelve a base si hay tres 6 seguidos. */
    public void penalizarTresSeises(Partida p, Jugador j, Ficha ultima,
                                    Consumer<EventoPartida> emitir) {
        if (ultima != null && !ultima.estaEnBase() && !ultima.estaEnMeta()) {
            int desde = ultima.getPosicion();
            p.getTablero().quitarFichaDeAnillo(ultima);
            ultima.volverACasa();
            emitir.accept(new EventoPartida(
                TipoEvento.TRES_SEISES_PIERDE_TURNO, j, ultima, 0, desde, -1,
                "Tres 6 seguidos: la ultima ficha vuelve a base"
            ));
        }
    }

    /* ================= Helpers compartidos ================= */

    /** Mueve en anillo (quita de origen, coloca en destino y emite FICHA_MOVIDA). */
    static void moverEnAnillo(Partida p, Jugador j, Ficha f, int desde, int hasta, int pasos,
                              Consumer<EventoPartida> emitir) {
        p.getTablero().quitarFichaDeAnillo(f);
        p.getTablero().colocarFichaEnAnillo(f, hasta);
        emitir.accept(new EventoPartida(
            TipoEvento.FICHA_MOVIDA, j, f, pasos, desde, hasta,
            j.getNombre() + " movio ficha " + f.getId() + " de " + desde + " a " + hasta
        ));
    }

    /** Captura simple: rival vuelve a base y se emite CAPTURA. */
    static void capturar(Partida p, Jugador atacante, Ficha victima, Consumer<EventoPartida> emitir) {
        int posVictima = victima.getPosicion();
        p.getTablero().quitarFichaDeAnillo(victima);
        victima.volverACasa();
        emitir.accept(new EventoPartida(
            TipoEvento.CAPTURA, atacante, victima, 0, posVictima, -1,
            atacante.getNombre() + " capturo una ficha " + victima.getColor()
        ));
    }
}
