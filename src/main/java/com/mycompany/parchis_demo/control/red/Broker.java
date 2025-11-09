package com.mycompany.parchis_demo.control.red;

import com.google.gson.Gson;
import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.Jugador;
import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Broker (servidor) que recibe mensajes JSON desde los clientes (ProxyCliente)
 * y redistribuye eventos. Gestiona registro previo (nombre + color único),
 * autodiscovery UDP, y ciclo de vida de la partida.
 *
 * Reglas de turno:
 *  - El broker es la ÚNICA fuente de verdad para el cambio de turno.
 *  - Lee flags en FICHA_MOVIDA: turnoExtra (true/false) y pierdeTurno (true/false).
 *  - Si pierdeTurno==true  -> avanza al siguiente.
 *  - Si turnoExtra==true    -> NO avanza (repite el mismo jugador).
 *  - En otro caso           -> avanza al siguiente.
 */
public class Broker {

    private ServerSocket servidor;
    private final List<ClienteInfo> clientes = new ArrayList<>();
    private final Map<Integer, PrintWriter> salidasPorId = new ConcurrentHashMap<>();
    private final Map<Integer, Jugador> jugadoresRegistrados = new ConcurrentHashMap<>();
    private final Set<Color> coloresDisponibles =
            EnumSet.of(Color.ROJO, Color.AZUL, Color.VERDE, Color.AMARILLO);

    private final Gson gson = new Gson();

    private int contadorJugadores = 0;     // ID incremental asignado al conectar
    private boolean partidaIniciada = false;
    private int jugadorActualId = 1;       // índice lógico de turno (1..N)

    // Puertos por defecto para TCP y discovery UDP
    private final int puertoTcp = 5000;
    private final int puertoDiscovery = 5001;

    /** Información básica de un cliente conectado. */
    private static class ClienteInfo {
        PrintWriter out;
        int jugadorId;
        ClienteInfo(PrintWriter out, int jugadorId) {
            this.out = out;
            this.jugadorId = jugadorId;
        }
    }

    // ============================================================
    // =================== ARRANQUE DEL BROKER ====================
    // ============================================================

    public void iniciarServidor(int puerto) {
        try {
            servidor = new ServerSocket(puerto);
            System.out.println("___________________________________________");
            System.out.println("   BROKER INICIADO EN EL PUERTO " + puerto);
            System.out.println("   Esperando jugadores...");
            System.out.println("___________________________________________");

            // Autodescubrimiento UDP (en un hilo aparte, daemon)
            Thread disc = new Thread(new DiscoveryResponder(puertoTcp, puertoDiscovery), "discovery-responder");
            disc.setDaemon(true);
            disc.start();

            while (true) {
                Socket socket = servidor.accept();
                int id = ++contadorJugadores;

                System.out.println("\n[Broker] Jugador " + id + " conectado desde: " + socket.getInetAddress());

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                ClienteInfo clienteInfo = new ClienteInfo(out, id);
                clientes.add(clienteInfo);
                salidasPorId.put(id, out);

                // 1) Enviar ID al nuevo cliente
                out.println(gson.toJson(new EventoPartida(
                        TipoEvento.JUGADOR_CONECTADO, null, "Tu ID es: " + id)));

                // 2) Enviar al nuevo la lista de colores disponibles
                out.println(gson.toJson(new EventoPartida(
                        TipoEvento.COLORES_DISPONIBLES, null, "Disponibles: " + coloresDisponibles)));

                // 3) Notificar al resto que se conectó alguien
                enviarEventoATodos(new EventoPartida(
                        TipoEvento.JUGADOR_CONECTADO, null,
                        "Jugador " + id + " se ha conectado. Total: " + clientes.size()), out);

                System.out.println("[Broker] Total de jugadores conectados: " + clientes.size());

                // Hilo por cliente para escuchar sus mensajes
                Thread hilo = new Thread(new HiloClienteServidor(socket, this, out));
                hilo.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // ==================== LÓGICA DE PARTIDA =====================
    // ============================================================

    private synchronized void iniciarPartida() {
        if (partidaIniciada) return;

        // Asegurar que haya al menos 2 JUGADORES REGISTRADOS (no solo conectados)
        if (jugadoresRegistrados.size() < 2) {
            enviarEventoATodos(new EventoPartida(
                    TipoEvento.MOVIMIENTO_IMPOSIBLE, null,
                    "No se puede iniciar: se requieren al menos 2 jugadores registrados"), null);
            return;
        }

        partidaIniciada = true;
        System.out.println("\n[Broker] | PARTIDA INICIADA con " + clientes.size() + " jugadores. |");

        enviarEventoATodos(new EventoPartida(
                TipoEvento.PARTIDA_INICIADA, null,
                "La partida ha comenzado con " + clientes.size() + " jugadores."), null);

        // Turno del jugador 1 (por simplicidad mantenemos 1..N)
        jugadorActualId = 1;
        enviarEventoATodos(new EventoPartida(
                TipoEvento.TURNO_CAMBIADO, null,
                "Turno del jugador " + jugadorActualId), null);
        System.out.println("[Broker] Turno inicial asignado al Jugador " + jugadorActualId);
    }

    /**
     * Dispatcher de eventos recibidos desde los clientes.
     * Decide si se difunden a todos, si se procesa lógicamente (registro/inicio),
     * y cómo se avanza el turno en base a flags.
     */
    public synchronized void procesarEvento(EventoPartida evento, PrintWriter emisor) {
        if (evento == null) return;

        System.out.println("[Broker] Evento recibido: " + evento.getTipoEvento() + " - " + evento.getMensaje());

        // === Solicitud de inicio ===
        if (evento.getTipoEvento() == TipoEvento.SOLICITAR_INICIO) {
            if (!partidaIniciada) {
                iniciarPartida();
            }
            return; // no redistribuir
        }

        // === Registro de jugador: nombre + color ===
        if (evento.getTipoEvento() == TipoEvento.SOLICITAR_REGISTRO) {
            Jugador req = evento.getJugador(); // el cliente envía su Jugador con id + nombre + color
            if (req == null) return;

            // validar color disponible
            if (!coloresDisponibles.contains(req.getColor())) {
                PrintWriter outCli = salidasPorId.get(req.getId());
                if (outCli != null) {
                    outCli.println(gson.toJson(new EventoPartida(
                            TipoEvento.REGISTRO_RECHAZADO, null,
                            "Color no disponible: " + req.getColor())));
                    outCli.println(gson.toJson(new EventoPartida(
                            TipoEvento.COLORES_DISPONIBLES, null,
                            "Disponibles: " + coloresDisponibles)));
                }
                return;
            }

            // Aceptar: reservar color y guardar perfil
            coloresDisponibles.remove(req.getColor());
            jugadoresRegistrados.put(req.getId(), req);

            // 1) Confirmación directa al cliente
            PrintWriter outCli = salidasPorId.get(req.getId());
            if (outCli != null) {
                outCli.println(gson.toJson(new EventoPartida(
                        TipoEvento.REGISTRO_ACEPTADO, req,
                        "Registro aceptado: " + req.getNombre() + " (" + req.getColor() + ")")));
            }

            // 2) Difundir perfil actualizado a todos
            enviarEventoATodos(new EventoPartida(
                    TipoEvento.JUGADOR_ACTUALIZADO, req,
                    req.getNombre() + " eligió color " + req.getColor()), null);

            // 3) Difundir lista viva de colores
            enviarEventoATodos(new EventoPartida(
                    TipoEvento.COLORES_DISPONIBLES, null,
                    "Disponibles: " + coloresDisponibles), null);

            return;
        }

        // === Broadcast normal de cualquier otro evento ===
        enviarEventoATodos(evento, null);

        // === Cambio de turno tras FICHA_MOVIDA (usando flags) ===
        if (evento.getTipoEvento() == TipoEvento.FICHA_MOVIDA && partidaIniciada) {
            boolean repetir = evento.isTurnoExtra();
            boolean pierde  = evento.isPierdeTurno();

            if (pierde) {                 // 3 x 6 → pasa turnos (no repite)
                jugadorActualId = (jugadorActualId % clientes.size()) + 1;
            } else if (!repetir) {        // movimiento normal → avanza
                jugadorActualId = (jugadorActualId % clientes.size()) + 1;
            }
            // si repetir==true → NO avanza (repite el mismo jugador)

            enviarEventoATodos(new EventoPartida(
                    TipoEvento.TURNO_CAMBIADO, null,
                    "Turno del jugador " + jugadorActualId), null);
            System.out.println("[Broker] Turno cambiado -> Jugador " + jugadorActualId);
        }
    }

    /**
     * Elimina un cliente desconectado de la lista y actualiza estado.
     */
    public synchronized void eliminarCliente(PrintWriter out) {
        ClienteInfo clienteRemovido = null;
        for (ClienteInfo c : clientes) {
            if (c.out == out) {
                clienteRemovido = c;
                break;
            }
        }

        if (clienteRemovido != null) {
            clientes.remove(clienteRemovido);
            salidasPorId.remove(clienteRemovido.jugadorId);

            // Si estaba registrado, liberar color
            Jugador reg = jugadoresRegistrados.remove(clienteRemovido.jugadorId);
            if (reg != null && reg.getColor() != null) {
                coloresDisponibles.add(reg.getColor());
                enviarEventoATodos(new EventoPartida(
                        TipoEvento.COLORES_DISPONIBLES, null,
                        "Disponibles: " + coloresDisponibles), null);
            }

            System.out.println("[Broker] Jugador " + clienteRemovido.jugadorId +
                    " desconectado. Quedan: " + clientes.size());

            enviarEventoATodos(new EventoPartida(
                    TipoEvento.JUGADOR_DESCONECTADO, null,
                    "Jugador " + clienteRemovido.jugadorId + " se ha desconectado"), null);
        }

        // Finalizar partida si quedan < 2 jugadores
        if (clientes.isEmpty()) {
            partidaIniciada = false;
            enviarEventoATodos(new EventoPartida(
                    TipoEvento.PARTIDA_TERMINADA, null,
                    "Partida finalizada: no hay jugadores"), null);
        } else if (partidaIniciada && clientes.size() < 2) {
            partidaIniciada = false;
            enviarEventoATodos(new EventoPartida(
                    TipoEvento.PARTIDA_TERMINADA, null,
                    "Partida finalizada: menos de 2 jugadores"), null);
        } else {
            if (jugadorActualId > clientes.size()) jugadorActualId = 1;
        }
    }

    /** Envía un evento a todos los clientes, excepto al emisor si se pasa. */
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
