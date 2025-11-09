package com.mycompany.parchis_demo.modelo.reglas;

import com.mycompany.parchis_demo.modelo.*;
import java.util.function.Consumer;

public class ReglaMovimientoBloqueos implements Regla {

    @Override 
    public int prioridad() { return 30; }

    @Override
    public boolean aplica(Partida p, Jugador j, Ficha f, int pasos) {
        // Sólo si está en anillo; base/pasillo/meta se resuelven con otras reglas
        return f.estaEnAnillo() && p.getTablero().movimientoLegal(j, f, pasos);
    }

    @Override
    public ResultadoRegla ejecutar(Partida p, Jugador j, Ficha f, int pasos,
                                   Consumer<EventoPartida> emitir) {
        int desde = f.getPosicion();
        int hasta = p.getTablero().calcularNuevaPosicion(f, pasos);

        // Si el destino es entrada de pasillo, no mover aquí; lo hace la otra regla
        if (p.getTablero().esEntradaPasillo(j.getColor(), hasta)) {
            return ResultadoRegla.seguir();
        }

        // Captura simple si hay un solo rival y no es segura
        if (p.getTablero().hayRivalSolo(hasta, j.getColor())) {
            Ficha victima = p.getTablero().buscarRivalEn(hasta, j.getColor());
            MotorReglas.capturar(p, j, victima, emitir);
        }

        // Mover en anillo
        MotorReglas.moverEnAnillo(p, j, f, desde, hasta, pasos, emitir);
        return ResultadoRegla.ok();
    }
}
