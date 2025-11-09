package com.mycompany.parchis_demo.control;

import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.Jugador;
import java.io.*;
import java.net.*;
import java.util.*;
/**
 *
 * @author Sergio Aboytia
 */
public class ControladorRed {
    private ServerSocket servidorSocket;
    private Map<Jugador, Socket> clientesConectados;
    private boolean enEjecucion;

    public ControladorRed() {
        clientesConectados = new HashMap<>();
    }

    public void iniciarServidor(int puerto) {
        try {
            servidorSocket = new ServerSocket(puerto);
            enEjecucion = true;
            System.out.println("Servidor iniciado en el puerto " + puerto);
            aceptarConexiones();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void aceptarConexiones() {
        new Thread(() -> {
            while (enEjecucion) {
                try {
                    Socket socket = servidorSocket.accept();
                    System.out.println("Cliente conectado: " + socket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void enviarEvento(Jugador jugador, EventoPartida evento) {
        Socket socket = clientesConectados.get(jugador);
        if (socket != null) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(evento);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void enviarEventoATodos(EventoPartida evento) {
        for (Socket s : clientesConectados.values()) {
            try {
                ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                out.writeObject(evento);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
