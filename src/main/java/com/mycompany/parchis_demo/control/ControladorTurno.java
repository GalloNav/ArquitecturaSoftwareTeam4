package com.mycompany.parchis_demo.control;

import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.Ficha;
import com.mycompany.parchis_demo.modelo.Jugador;
import com.mycompany.parchis_demo.modelo.Partida;
import com.mycompany.parchis_demo.modelo.ResultadoTurno;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;

/**
 *
 * @author Sergio Aboytia
 */
public class ControladorTurno {
    private Partida partida;
    private int tirosConsecutivos;

    public ControladorTurno(Partida partida) {
        this.partida = partida;
        this.tirosConsecutivos = 0;
    }
    
    public ResultadoTurno procesarTurno(Jugador jugador, int idFicha) {
        int valorDado = partida.getDado().lanzar();
        Ficha ficha = jugador.seleccionarFicha(idFicha);

        if (ficha == null)
            return new ResultadoTurno(false, "Ficha no encontrada", false, false);

        if (!validarMovimiento(ficha, valorDado))
            return new ResultadoTurno(false, "Movimiento inválido", false, false);

        aplicarMovimiento(ficha, valorDado);
        verificarCaptura(ficha, ficha.getPosicion());

        boolean turnoExtra = partida.getDado().esTurnoExtra();
        return new ResultadoTurno(true, "Ficha movida", turnoExtra, false);
    }
    
    public boolean validarMovimiento(Ficha ficha, int casillas) {
        return !ficha.estaEnMeta();
    }
    
    public void aplicarMovimiento(Ficha ficha, int casillas) {
        int desde = ficha.getPosicion();
        int hasta = partida.getTablero().calcularNuevaPosicion(ficha, casillas);
        ficha.setPosicion(hasta);
        registrarMovimiento(ficha, desde, hasta);
    }
    
    public void verificarCaptura(Ficha ficha, int nuevaPosicion) {
        for (Jugador j : partida.getJugadores()) {
            if (j.getColor() != ficha.getColor()) {
                for (Ficha f : j.getFichas()) {
                    if (f.getPosicion() == nuevaPosicion && partida.getTablero().puedeCapturar(f)) {
                        procesarCaptura(ficha, f);
                        return;
                    }
                }
            }
        }
    }
    
    public void procesarCaptura(Ficha atacante, Ficha victima) {
        victima.volverACasa();
        partida.notificarObservadores(new EventoPartida(
            TipoEvento.CAPTURA, null, atacante, 0, 0, atacante.getPosicion(),
            "¡" + atacante.getColor() + " capturó una ficha de " + victima.getColor() + "!"
        ));
    }
    
    public void registrarMovimiento(Ficha ficha, int desde, int hasta) {
        partida.notificarObservadores(new EventoPartida(
            TipoEvento.FICHA_MOVIDA, null, ficha, 0, desde, hasta, "Ficha movida"
        ));
    }
}
