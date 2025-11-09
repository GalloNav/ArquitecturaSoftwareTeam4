package com.mycompany.parchis_demo.modelo.reglas;

import com.mycompany.parchis_demo.modelo.*;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;
import java.util.function.Consumer;

public class ReglaPasilloYMeta implements Regla {

    @Override public int prioridad() { return 20; }

    @Override
    public boolean aplica(Partida p, Jugador j, Ficha f, int pasos) {
        // 1) Desde anillo: si el destino es la casilla de entrada al pasillo del color
        if (f.estaEnAnillo()) {
            int destino = p.getTablero().calcularNuevaPosicion(f, pasos);
            if (p.getTablero().esEntradaPasillo(j.getColor(), destino)) return true;
        }
        // 2) Ya en pasillo: si cabe avanzar exacto
        if (f.estaEnPasillo()) {
            return p.getTablero().avanzarEnPasillo(f, pasos) >= 0;
        }
        return false;
    }

    @Override
    public ResultadoRegla ejecutar(Partida p, Jugador j, Ficha f, int pasos,
                                   Consumer<EventoPartida> emitir) {

        // Caso 1: entra al pasillo
        if (f.estaEnAnillo()) {
            int desde = f.getPosicion();
            int destino = p.getTablero().calcularNuevaPosicion(f, pasos);

            // quitar del anillo y marcar pasillo
            p.getTablero().quitarFichaDeAnillo(f);
            f.entrarPasillo();

            emitir.accept(new EventoPartida(
                TipoEvento.ENTRA_PASILLO_COLOR, j, f, pasos, desde, destino,
                j.getNombre() + " entra a su pasillo"
            ));
            return ResultadoRegla.ok();
        }

        // Caso 2: mover dentro del pasillo (sin capturas/barreras; exactitud)
        int idxDesde = f.getIndicePasillo();
        int idxDestino = p.getTablero().avanzarEnPasillo(f, pasos);
        if (idxDestino < 0) return ResultadoRegla.seguir();

        f.setIndicePasillo(idxDestino);

        emitir.accept(new EventoPartida(
            TipoEvento.FICHA_MOVIDA, j, f, pasos, idxDesde, idxDestino,
            j.getNombre() + " avanza en pasillo a " + idxDestino
        ));

        // ¿meta exacta?
        if (p.getTablero().puedeEntrarMeta(f, 0)) {
            f.llegarMeta();
            emitir.accept(new EventoPartida(
                TipoEvento.FICHA_EN_META, j, f, 0, idxDestino, -1,
                j.getNombre() + " metió una ficha a meta"
            ));
            // puedes llamar aquí a p.verificarCondicionesVictoria() si lo prefieres
        }

        return ResultadoRegla.ok();
    }
}
