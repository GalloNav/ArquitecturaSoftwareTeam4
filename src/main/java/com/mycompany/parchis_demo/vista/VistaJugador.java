package com.mycompany.parchis_demo.vista;

import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.Jugador;
import java.net.Socket;

/**
 *
 * @author Sergio Aboytia
 */
public class VistaJugador {
    private Jugador jugador;
    private Socket socketCliente;

    public VistaJugador(Jugador jugador, Socket socketCliente) {
        this.jugador = jugador;
        this.socketCliente = socketCliente;
    }

    public void actualizar(EventoPartida evento) {
        mostrarMensaje(evento.getMensaje());
    }

    public void mostrarTablero() {
        System.out.println("Mostrando tablero...");
    }

    public void mostrarTurnoActual() {
        System.out.println("Turno actual: " + jugador.getNombre());
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println("[Mensaje] " + mensaje);
    }

}
