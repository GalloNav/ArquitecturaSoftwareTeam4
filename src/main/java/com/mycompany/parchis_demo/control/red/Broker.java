package com.mycompany.parchis_demo.control.red;

import com.google.gson.Gson;
import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Broker (servidor) que recibe mensajes JSON desde los clientes (ProxyCliente)
 * a través de sockets TCP, interpreta su tipo de evento mediante el método
 * {@link #procesarEvento(EventoPartida, PrintWriter)} y los redistribuye a
 * los jugadores conectados según corresponda.
 */
public class Broker {

    private ServerSocket servidor;
    private final List<ClienteInfo> clientes = new ArrayList<>();
    private final Gson gson = new Gson();
    private int contadorJugadores = 0;
    private boolean partidaIniciada = false;
    private int jugadorActualId = 1;

    /**
     * Clase interna para almacenar información del cliente
     */
    private static class ClienteInfo {
        PrintWriter out;
        int jugadorId;

        ClienteInfo(PrintWriter out, int jugadorId) {
            this.out = out;
            this.jugadorId = jugadorId;
        }
    }

    /**
     * Inicia el broker en el puerto especificado y escucha nuevas conexiones
     */
    public void iniciarServidor(int puerto) {
        try {
            servidor = new ServerSocket(puerto);
            System.out.println("___________________________________________");
            System.out.println("   BROKER INICIADO EN EL PUERTO " + puerto);
            System.out.println("   Esperando jugadores...");
            System.out.println("___________________________________________");

            while (true) {
                Socket socket = servidor.accept();
                contadorJugadores++;
                
                System.out.println("\n[Broker] Jugador " + contadorJugadores + " conectado desde: " + socket.getInetAddress());

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                ClienteInfo clienteInfo = new ClienteInfo(out, contadorJugadores);
                clientes.add(clienteInfo);

                // Enviar ID asignado al cliente
                EventoPartida eventoId = new EventoPartida(
                    TipoEvento.JUGADOR_CONECTADO,
                    null,
                    "Tu ID es: " + contadorJugadores
                );
                out.println(gson.toJson(eventoId));

                // Notificar a todos los demás jugadores
                EventoPartida eventoConexion = new EventoPartida(
                    TipoEvento.JUGADOR_CONECTADO,
                    null,
                    "Jugador " + contadorJugadores + " se ha conectado. Total: " + clientes.size()
                );
                enviarEventoATodos(eventoConexion, out);

                System.out.println("[Broker] Total de jugadores conectados: " + clientes.size());

                // Crea y lanza un hilo por cliente
                Thread hilo = new Thread(new HiloClienteServidor(socket, this, out));
                hilo.start();

                // Si hay 2 o más jugadores y no ha iniciado, iniciar partida
                if (clientes.size() >= 2 && !partidaIniciada) {
                    iniciarPartida();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inicia la partida cuando hay suficientes jugadores
     */
    private synchronized void iniciarPartida() {
        partidaIniciada = true;
        System.out.println("\n[Broker] | PARTIDA INICIADA con " + clientes.size() + " jugadores. |");
        
        EventoPartida eventoInicio = new EventoPartida(
            TipoEvento.PARTIDA_INICIADA,
            null,
            "La partida ha comenzado con " + clientes.size() + " jugadores."
        );
        enviarEventoATodos(eventoInicio, null);

        // Dar turno al primer jugador
        EventoPartida primerTurno = new EventoPartida(
            TipoEvento.TURNO_CAMBIADO,
            null,
            "Turno del jugador 1"
        );
        enviarEventoATodos(primerTurno, null);
        System.out.println("[Broker] Turno inicial asignado al Jugador 1");
    }

    /**
     * DISPATCHER que procesa eventos y gestiona los cambios de turno.
     */
    public synchronized void procesarEvento(EventoPartida evento, PrintWriter emisor) {
        if (evento == null) {
            System.out.println("[Broker] Error: evento nulo recibido.");
            return;
        }

        System.out.println("[Broker] Evento recibido: " + evento.getTipoEvento() + " - " + evento.getMensaje());

        // Reenviar a todos (incluyendo al emisor para que vea confirmación)
        enviarEventoATodos(evento, null);

        // Cambio de turno solo si fue un movimiento de ficha
        if (evento.getTipoEvento() == TipoEvento.FICHA_MOVIDA) {
            // Calcular siguiente jugador
            jugadorActualId = (jugadorActualId % clientes.size()) + 1;

            EventoPartida cambioTurno = new EventoPartida(
                TipoEvento.TURNO_CAMBIADO,
                null,
                "Turno del jugador " + jugadorActualId
            );

            enviarEventoATodos(cambioTurno, null);
            System.out.println("[Broker] Turno cambiado -> Jugador " + jugadorActualId);
        }
    }

    /**
     * Elimina un cliente desconectado de la lista de conexiones activas.
     */
    public synchronized void eliminarCliente(PrintWriter out) {
        ClienteInfo clienteRemovido = null;
        for (ClienteInfo cliente : clientes) {
            if (cliente.out == out) {
                clienteRemovido = cliente;
                break;
            }
        }
        
        if (clienteRemovido != null) {
            clientes.remove(clienteRemovido);
            System.out.println("[Broker] Jugador " + clienteRemovido.jugadorId + " desconectado. Quedan: " + clientes.size());
            
            EventoPartida eventoDesconexion = new EventoPartida(
                TipoEvento.JUGADOR_DESCONECTADO,
                null,
                "Jugador " + clienteRemovido.jugadorId + " se ha desconectado"
            );
            enviarEventoATodos(eventoDesconexion, null);
        }
    }

    /**
     * Envía un evento a todos los clientes conectados excepto al emisor.
     */
    public synchronized void enviarEventoATodos(EventoPartida evento, PrintWriter emisor) {
        String json = gson.toJson(evento);
        for (ClienteInfo cliente : clientes) {
            if (cliente.out != emisor) {
                cliente.out.println(json);
            }
        }
    }

    public static void main(String[] args) {
        Broker servidor = new Broker();
        servidor.iniciarServidor(5000);
    }
}