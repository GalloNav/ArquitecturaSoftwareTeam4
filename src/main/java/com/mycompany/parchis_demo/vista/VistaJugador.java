package com.mycompany.parchis_demo.vista;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.mycompany.parchis_demo.control.ControladorTurno;
import com.mycompany.parchis_demo.control.red.DiscoveryClient;
import com.mycompany.parchis_demo.control.red.ProxyCliente;
import com.mycompany.parchis_demo.modelo.*;
import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;

/**
 * Clase que representa la vista del jugador en el juego Parchís.
 */
public class VistaJugador {

    private Jugador jugador;
    private ProxyCliente proxy;
    private ControladorTurno controlador;
    private int miIdAsignado = -1;
    private boolean partidaIniciada = false;
    private boolean esMiTurno = false;
    private boolean registrado = false;
    private volatile boolean seguir = true;

    private final Scanner scanner = new Scanner(System.in);
    private final Map<Integer, EstadoJ> estadoGlobal = new ConcurrentHashMap<>();
    private final Set<Color> coloresLibres = EnumSet.of(Color.ROJO, Color.AZUL, Color.VERDE, Color.AMARILLO);

    // --- clase auxiliar para mostrar estado de todos los jugadores ---
    private static class EstadoJ {
        int id;
        String nombre;
        Color color;
        String[] pos = {"BASE", "BASE", "BASE", "BASE"};
    }

    public VistaJugador(Jugador jugador) {
        this.jugador = jugador;
        this.proxy = new ProxyCliente();
    }

    // ==============================================================
    // =============== CONEXIÓN Y EVENTOS ===========================
    // ==============================================================

    public void conectar(String host, int puerto) {
        proxy.conectar(host, puerto);
        proxy.setManejadorEventos(this::actualizar);
    }

    public void actualizar(EventoPartida evento) {
        // 1. Asignación de ID
        if (evento.getTipoEvento() == TipoEvento.JUGADOR_CONECTADO &&
                evento.getMensaje().startsWith("Tu ID es:")) {

            String[] partes = evento.getMensaje().split(":");
            miIdAsignado = Integer.parseInt(partes[1].trim());
            jugador.setId(miIdAsignado);

            ensureJugadorEnEstado(jugador);
            System.out.println("\n___________________________________________");
            System.out.println("  | CONECTADO | Tu ID es: " + miIdAsignado);
            System.out.println("  Esperando registro de nombre y color...");
            System.out.println("______________________________________________\n");

            flujoRegistro();
            return;
        }

        // 2. Lista viva de colores disponibles
        if (evento.getTipoEvento() == TipoEvento.COLORES_DISPONIBLES && evento.getMensaje() != null) {
            actualizarColoresLibres(evento.getMensaje());
        }

        // 3. Registro aceptado / rechazado
        if (evento.getTipoEvento() == TipoEvento.REGISTRO_ACEPTADO && evento.getJugador() != null) {
            jugador.setNombre(evento.getJugador().getNombre());
            jugador.setColor(evento.getJugador().getColor());
            registrado = true;
            System.out.println("[Registro] Aceptado: " + jugador.getNombre() + " (" + jugador.getColor() + ")");
        }

        if (evento.getTipoEvento() == TipoEvento.REGISTRO_RECHAZADO) {
            System.out.println("[Registro] Rechazado: " + evento.getMensaje());
            registrado = false;
            flujoRegistro(); // vuelve a intentar
        }

        // 4. Difusión de nuevos jugadores
        if (evento.getTipoEvento() == TipoEvento.JUGADOR_ACTUALIZADO && evento.getJugador() != null) {
            ensureJugadorEnEstado(evento.getJugador());
            System.out.println("[Conectado] " + evento.getJugador().getNombre() + " (" + evento.getJugador().getColor() + ")");
        }

        // 5. Inicio / fin de partida
        if (evento.getTipoEvento() == TipoEvento.PARTIDA_INICIADA) {
            partidaIniciada = true;
            System.out.println("\n>>> " + evento.getMensaje() + " <<<\n");
            return;
        }

        if (evento.getTipoEvento() == TipoEvento.PARTIDA_TERMINADA ||
            evento.getTipoEvento() == TipoEvento.VICTORIA) {
            System.out.println("[Evento] " + evento.getMensaje());
            seguir = false;
            return;
        }

        // 6. Mensajes generales de juego
        if (evento.getTipoEvento() == TipoEvento.FICHA_MOVIDA ||
            evento.getTipoEvento() == TipoEvento.CAPTURA ||
            evento.getTipoEvento() == TipoEvento.JUGADOR_DESCONECTADO ||
            evento.getTipoEvento() == TipoEvento.MOVIMIENTO_IMPOSIBLE) {
            System.out.println("[Evento] " + evento.getMensaje());
        }

        // 7. Cambios de turno
        if (evento.getTipoEvento() == TipoEvento.TURNO_CAMBIADO && partidaIniciada) {
            if (evento.getMensaje().contains("jugador " + miIdAsignado)) {
                esMiTurno = true;
                System.out.println("\n________________________________________");
                System.out.println("***| ES TU TURNO |***");
                System.out.println("Presiona ENTER para lanzar el dado...");
                System.out.println("__________________________________________");
            } else {
                esMiTurno = false;
                System.out.println("\n>>> " + evento.getMensaje() + " - Esperando...\n");
            }
        }

        // 8. Actualizaciones de tablero (para mostrar con “s”)
        if (evento.getJugador() != null && evento.getFicha() != null) {
            ensureJugadorEnEstado(evento.getJugador());
            EstadoJ e = estadoGlobal.get(evento.getJugador().getId());
            e.pos[evento.getFicha().getId()] = "A" + evento.getHasta();
        }
    }

    // ==============================================================
    // =============== LOOP PRINCIPAL DE JUEGO ======================
    // ==============================================================

    public void iniciarJuego() {
        System.out.println("\nEsperando tu turno...");
        while (seguir) {
            if (esMiTurno) {
                System.out.println("\nAcciones: [ENTER] lanzar dado | [s] estado global | [i] iniciar partida");
                System.out.print("> ");
                String cmd = scanner.nextLine();

                if (cmd.isEmpty()) {
                    int idFicha = leerIdFicha();
                    ResultadoTurno res = controlador.procesarTurno(jugador, idFicha);
                    if (res.getValorDado() != null)
                        System.out.println("[" + jugador.getNombre() + "] lanzo: " + res.getValorDado());

                    if (!res.exito()) {
                        System.out.println("[Movimiento inválido] " + res.mensaje());
                        esMiTurno = true;
                    } else {
                        if (res.turnoExtra()) {
                            System.out.println("¡Tienes turno extra!");
                            esMiTurno = true;
                        } else {
                            esMiTurno = false;
                            System.out.println("\nEsperando a los demás jugadores...\n");
                        }
                    }
                    continue;
                }

                if (cmd.equalsIgnoreCase("s")) {
                    imprimirEstadoGlobal();
                    esMiTurno = true;
                    continue;
                }

                if (cmd.equalsIgnoreCase("i")) {
                    proxy.enviarEvento(new EventoPartida(
                            TipoEvento.SOLICITAR_INICIO, jugador, "Solicita iniciar la partida"));
                    esMiTurno = true;
                    continue;
                }

                System.out.println("(Comando no reconocido. Usa ENTER, 's' o 'i')");
                esMiTurno = true;
            }

            try { Thread.sleep(350); } catch (InterruptedException ignored) {}
        }
        System.out.println("\nLa partida ha terminado. Cerrando cliente...\n");
    }

    private void flujoRegistro() {
        System.out.print("Escribe tu nombre y presiona ENTER: ");
        String nombre = scanner.nextLine().trim();
        if (nombre.isEmpty()) nombre = "Jugador" + miIdAsignado;

        Color elegido = null;
        while (elegido == null) {
            System.out.println("Colores disponibles:");
            int idx = 1;
            Map<Integer, Color> mapa = new HashMap<>();
            for (Color c : coloresLibres) {
                System.out.println(" (" + idx + ") " + c);
                mapa.put(idx, c);
                idx++;
            }
            if (mapa.isEmpty()) {
                System.out.println("No hay colores disponibles. Espera...");
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                continue;
            }

            System.out.print("Selecciona una opción: ");
            String op = scanner.nextLine().trim();
            try {
                int sel = Integer.parseInt(op);
                if (mapa.containsKey(sel)) {
                    elegido = mapa.get(sel);
                } else {
                    System.out.println("Opción invalida.");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Opción invalida.");
            }
        }

        Jugador registro = new Jugador(miIdAsignado, nombre, "", elegido);
        proxy.enviarEvento(new EventoPartida(
                TipoEvento.SOLICITAR_REGISTRO, registro,
                "Registro: " + nombre + " (" + elegido + ")"));
    }

    private void actualizarColoresLibres(String mensaje) {
        try {
            coloresLibres.clear();
            int i = mensaje.indexOf('['), j = mensaje.indexOf(']');
            if (i >= 0 && j > i) {
                String inside = mensaje.substring(i + 1, j);
                for (String tok : inside.split(",")) {
                    String t = tok.trim();
                    if (!t.isEmpty()) coloresLibres.add(Color.valueOf(t));
                }
            }
        } catch (Exception ignored) {}
    }

    private void ensureJugadorEnEstado(Jugador j) {
        if (j == null) return;
        estadoGlobal.computeIfAbsent(j.getId(), k -> {
            EstadoJ e = new EstadoJ();
            e.id = j.getId();
            e.nombre = j.getNombre();
            e.color = j.getColor();
            return e;
        });
    }

    private void imprimirEstadoGlobal() {
        System.out.println("\n===== Estado de la partida =====");
        if (estadoGlobal.isEmpty()) {
            System.out.println("(sin datos aún)");
        } else {
            estadoGlobal.values().forEach(e -> {
                System.out.print(e.nombre + " (" + e.color + "): ");
                for (int i = 0; i < e.pos.length; i++) {
                    System.out.print(" f" + i + "=" + e.pos[i] + " ");
                }
                System.out.println();
            });
        }
        System.out.println("================================\n");
    }

    private int leerIdFicha() {
        while (true) {
            System.out.print("Elige ficha a mover (0..3): ");
            String entrada = scanner.nextLine().trim();
            try {
                int id = Integer.parseInt(entrada);
                if (id >= 0 && id <= 3) return id;
            } catch (NumberFormatException ignored) {}
            System.out.println("Entrada inválida. Debe ser 0, 1, 2 o 3.");
        }
    }

    // ==============================================================
    // =============== MAIN =========================================
    // ==============================================================

    public static void main(String[] args) {
        Jugador jugador = new Jugador(0, "SinNombre", "", Color.ROJO);
        Partida partida = new Partida("P001");
        partida.agregarJugador(jugador);

        VistaJugador vista = new VistaJugador(jugador);
        ControladorTurno ctrl = new ControladorTurno(partida, vista.proxy);
        vista.controlador = ctrl;

        // --- Autodescubrimiento ---
        int discoveryPort = 5001;
        DiscoveryClient.Result r = DiscoveryClient.discover(discoveryPort, 2500);
        if (r != null) {
            System.out.println("Broker descubierto en " + r.host + ":" + r.port);
            vista.conectar(r.host, r.port);
        } else {
            System.out.println("No se encontró broker. Intentando en localhost:5000");
            vista.conectar("127.0.0.1", 5000);
        }

        System.out.println("\nCliente iniciado - Esperando conexión al broker...\n");
        vista.iniciarJuego();
    }
}
