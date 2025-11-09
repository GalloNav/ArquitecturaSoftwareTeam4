package com.mycompany.parchis_demo.modelo.reglas;

import com.mycompany.parchis_demo.modelo.*;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;
import java.util.function.Consumer;

public class ReglaSacarConCinco implements Regla {

    @Override 
    public int prioridad() { 
        return 10; 
    }

    @Override
    public boolean aplica(Partida p, Jugador j, Ficha f, int pasos) {
        return f.estaEnBase() && pasos == 5;
    }

    @Override
    public ResultadoRegla ejecutar(Partida p, Jugador j, Ficha f, int pasos,
                                   Consumer<EventoPartida> emitir) {
        int salida = p.getTablero().casillaSalida(j.getColor());

        // ¿legal caer en salida? (incluye caso de 1 rival capturable, no barrera/segura)
        if (!p.getTablero().movimientoLegal(j, f, pasos)) return ResultadoRegla.seguir();

        // Captura en salida si hay un único rival y no es segura
        if (p.getTablero().hayRivalSolo(salida, j.getColor())) {
            Ficha victima = p.getTablero().buscarRivalEn(salida, j.getColor());
            MotorReglas.capturar(p, j, victima, emitir);
        }

        // Colocar ficha en salida
        p.getTablero().colocarFichaEnAnillo(f, salida);

        emitir.accept(new EventoPartida(
            TipoEvento.FICHA_SALE_BASE, j, f, pasos, -1, salida,
            j.getNombre() + " saco ficha " + f.getId() + " a la salida"
        ));

        return ResultadoRegla.ok();
    }
}
