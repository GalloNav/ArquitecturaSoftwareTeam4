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
 * Vista de consola para el jugador (LAN).
 * - Registro previo: nombre + color único (coordina con el Broker).
 * - Lobby: iniciar partida [i], ver estado [s].
 * - En partida: lanzar con ENTER, ver estado [s].
 */
public class VistaJugador {

    // ======= Estado local =======
    private final Scanner scanner = new Scanner(System.in);

    private Jugador jugador;
    private ProxyCliente proxy;
    private ControladorTurno controlador;

    private volatile int miIdAsignado = -1;
    private volatile boolean partidaIniciada = false;
    private volatile boolean esMiTurno = false;
    private volatile boolean registrado = false;
    private volatile boolean debeRegistrar = false;
    private volatile boolean seguir = true;

    // “Espejo” para imprimir estado global con [s]
    private static class EstadoJ {
        int id;
        String nombre;
        Color color;
        String[] pos = {"BASE", "BASE", "BASE", "BASE"}; // f0..f3
    }
    private final Map<Integer, EstadoJ> estadoGlobal = new ConcurrentHashMap<>();

    // Lista viva de colores (se actualiza con eventos del broker)
    private final Set<Color> coloresLibres = EnumSet.of(Color.ROJO, Color.AZUL, Color.VERDE, Color.AMARILLO);

    // ======= Ctor & conexión =======
    public VistaJugador(Jugador jugador) {
        this.jugador = jugador;
        this.proxy = new ProxyCliente();
    }

    public void conectar(String host, int puerto) {
        proxy.conectar(host, puerto);
        proxy.setManejadorEventos(this::actualizar);
    }

    // ======= Manejo de eventos del broker =======
    public void actualizar(EventoPartida evento) {
        if (evento == null) return;

        // 1) Asignación de ID al conectar
        if (evento.getTipoEvento() == TipoEvento.JUGADOR_CONECTADO &&
            evento.getMensaje() != null &&
            evento.getMensaje().startsWith("Tu ID es:")) {

            String[] partes = evento.getMensaje().split(":");
            miIdAsignado = Integer.parseInt(partes[1].trim());
            jugador.setId(miIdAsignado);

            System.out.println("\n___________________________________________");
            System.out.println("  | CONECTADO | Tu ID es: " + miIdAsignado);
            System.out.println("  Esperando registro de nombre y color...");
            System.out.println("______________________________________________\n");

            debeRegistrar = true; // El registro ocurrirá en el hilo principal (iniciarJuego)
            return;
        }

        // 2) Lista viva de colores
        if (evento.getTipoEvento() == TipoEvento.COLORES_DISPONIBLES && evento.getMensaje() != null) {
            actualizarColoresLibres(evento.getMensaje());
        }

        // 3) Registro aceptado / rechazado
        if (evento.getTipoEvento() == TipoEvento.REGISTRO_ACEPTADO && evento.getJugador() != null) {
            jugador.setNombre(evento.getJugador().getNombre());
            jugador.setColor(evento.getJugador().getColor());
            registrado = true;
            debeRegistrar = false;
            ensureJugadorEnEstado(evento.getJugador());
            System.out.println("[Registro] Aceptado: " + jugador.getNombre() + " (" + jugador.getColor() + ")");
        }

        if (evento.getTipoEvento() == TipoEvento.REGISTRO_RECHAZADO) {
            registrado = false;
            debeRegistrar = true; // volverá a pedir en el hilo principal
            System.out.println("[Registro] Rechazado: " + evento.getMensaje());
        }

        // 4) Difusión del perfil actualizado (nombre + color) a todos
        if (evento.getTipoEvento() == TipoEvento.JUGADOR_ACTUALIZADO && evento.getJugador() != null) {
            ensureJugadorEnEstado(evento.getJugador());
            System.out.println("[Conectado] " + evento.getJugador().getNombre() + " (" + evento.getJugador().getColor() + ")");
        }

        // 5) Inicio / fin
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

        // 6) Mensajes generales
        if (evento.getTipoEvento() == TipoEvento.FICHA_MOVIDA ||
            evento.getTipoEvento() == TipoEvento.CAPTURA ||
            evento.getTipoEvento() == TipoEvento.MOVIMIENTO_IMPOSIBLE ||
            evento.getTipoEvento() == TipoEvento.JUGADOR_DESCONECTADO ||
            evento.getTipoEvento() == TipoEvento.JUGADOR_CONECTADO) {
            System.out.println("[Evento] " + evento.getMensaje());
        }

        // 7) Turno
        if (evento.getTipoEvento() == TipoEvento.TURNO_CAMBIADO && partidaIniciada) {
            if (evento.getMensaje() != null && evento.getMensaje().contains("jugador " + miIdAsignado)) {
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

        // 8) Actualizaciones para imprimir con [s]
        if (evento.getTipoEvento() == TipoEvento.JUGADOR_DESCONECTADO && evento.getJugador() != null) {
            estadoGlobal.remove(evento.getJugador().getId());
        }
        if (evento.getJugador() != null && evento.getFicha() != null) {
            ensureJugadorEnEstado(evento.getJugador());
            EstadoJ e = estadoGlobal.get(evento.getJugador().getId());
            // preferimos posicionNueva (si tu EventoPartida lo tiene); si no, usar getHasta()
            int hasta = (evento.getPosicionNueva() != 0) ? evento.getPosicionNueva() : evento.getHasta();
            e.pos[evento.getFicha().getId()] = (hasta >= 68) ? "META" : ("A" + hasta);
        }
        if (evento.getTipoEvento() == TipoEvento.FICHA_SALE_BASE && evento.getJugador() != null && evento.getFicha() != null) {
            ensureJugadorEnEstado(evento.getJugador());
            EstadoJ e = estadoGlobal.get(evento.getJugador().getId());
            e.pos[evento.getFicha().getId()] = "A" + evento.getHasta();
        }
        if (evento.getTipoEvento() == TipoEvento.ENTRA_PASILLO_COLOR && evento.getJugador() != null && evento.getFicha() != null) {
            ensureJugadorEnEstado(evento.getJugador());
            estadoGlobal.get(evento.getJugador().getId()).pos[evento.getFicha().getId()] = "PAS(0)";
        }
        if (evento.getTipoEvento() == TipoEvento.FICHA_EN_META && evento.getJugador() != null && evento.getFicha() != null) {
            ensureJugadorEnEstado(evento.getJugador());
            estadoGlobal.get(evento.getJugador().getId()).pos[evento.getFicha().getId()] = "META";
        }
    }

    // ======= Bucle principal =======
    public void iniciarJuego() {
        System.out.println("\nConectado. Esperando a que se inicie la partida...");
        while (seguir) {

            // Registro previo (solo aquí; evita doble Scanner)
            if (debeRegistrar || (!registrado && miIdAsignado > 0)) {
                flujoRegistro();
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                continue;
            }

            // Lobby (antes de iniciar)
            if (!partidaIniciada) {
                System.out.println("\n--- Lobby ---");
                System.out.println("Comandos: [i] iniciar partida  |  [s] estado global  |  [ENTER] refrescar");
                System.out.print("> ");
                String cmd = scanner.nextLine().trim();

                if (cmd.equalsIgnoreCase("i")) {
                    proxy.enviarEvento(new EventoPartida(
                            TipoEvento.SOLICITAR_INICIO, jugador, "Solicita iniciar la partida"));
                    continue;
                }
                if (cmd.equalsIgnoreCase("s")) {
                    imprimirEstadoGlobal();
                    continue;
                }
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                continue;
            }

            // En partida
            if (esMiTurno) {
                System.out.println("\nAcciones: [ENTER] lanzar dado | [s] estado global");
                System.out.print("> ");
                String cmd = scanner.nextLine();

                if (cmd.isEmpty()) {
                    // 1) Tirar dado
                    int valor = controlador.tirarDado();
                    System.out.println("[" + jugador.getNombre() + "] lanzó: " + valor);

                    // 2) Elegir ficha DESPUES de ver el valor
                    int idFicha = leerIdFicha(); // 0..3
                    ResultadoTurno res = controlador.procesarTurnoConValor(jugador, idFicha, valor);

                    if (!res.exito()) {
                        System.out.println("[Movimiento inválido] " + res.mensaje());
                        esMiTurno = true; // reintenta
                    } else {
                        esMiTurno = res.turnoExtra();
                        if (!esMiTurno) System.out.println("\nEsperando a los demás jugadores...\n");
                        else System.out.println("¡Tienes turno extra!");
                    }
                    continue;
                }

                if (cmd.equalsIgnoreCase("s")) {
                    imprimirEstadoGlobal();
                    esMiTurno = true;
                    continue;
                }

                System.out.println("(Comando no reconocido. Usa ENTER o 's')");
                esMiTurno = true;
            } else {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            }
        }
        System.out.println("\nLa partida ha terminado. Cerrando cliente...\n");
    }

    // ======= Registro: nombre + color =======
    private void flujoRegistro() {
        if (miIdAsignado <= 0) return;

        // 1) Nombre
        System.out.print("\nEscribe tu nombre y presiona ENTER: ");
        String nombre = scanner.nextLine().trim();
        if (nombre.isEmpty()) nombre = "Jugador" + miIdAsignado;

        // 2) Colores disponibles (snapshot para evitar carreras)
        List<Color> snapshot;
        synchronized (coloresLibres) {
            snapshot = new ArrayList<>(coloresLibres);
        }
        if (snapshot.isEmpty()) {
            System.out.println("(Sin colores por ahora, espera un momento...)");
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            return;
        }

        Map<Integer, Color> mapa = new LinkedHashMap<>();
        System.out.println("\nColores disponibles:");
        for (int i = 0; i < snapshot.size(); i++) {
            System.out.println(" (" + (i + 1) + ") " + snapshot.get(i));
            mapa.put(i + 1, snapshot.get(i));
        }

        // 3) Selección
        Color elegido = null;
        while (elegido == null) {
            System.out.print("Selecciona una opción: ");
            String op = scanner.nextLine().trim();
            try {
                int sel = Integer.parseInt(op);
                elegido = mapa.get(sel);
                if (elegido == null) System.out.println("Opción inválida.");
            } catch (NumberFormatException ex) {
                System.out.println("Opción inválida.");
            }
        }

        // 4) Enviar solicitud de registro (respuesta llega por eventos)
        Jugador registro = new Jugador(miIdAsignado, nombre, "", elegido);
        proxy.enviarEvento(new EventoPartida(
                TipoEvento.SOLICITAR_REGISTRO, registro,
                "Registro: " + nombre + " (" + elegido + ")"));

        debeRegistrar = false;
        System.out.println("[Registro] Enviado. Esperando confirmación del broker...");
    }

    // ======= Utilidades =======
    private void actualizarColoresLibres(String mensaje) {
        try {
            // Formato esperado: "Disponibles: [ROJO, AZUL, ...]"
            int i = mensaje.indexOf('['), j = mensaje.indexOf(']');
            if (i >= 0 && j > i) {
                String inside = mensaje.substring(i + 1, j);
                Set<Color> nuevos = EnumSet.noneOf(Color.class);
                for (String tok : inside.split(",")) {
                    String t = tok.trim();
                    if (!t.isEmpty()) nuevos.add(Color.valueOf(t));
                }
                synchronized (coloresLibres) {
                    coloresLibres.clear();
                    coloresLibres.addAll(nuevos);
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
    //Mensaje nuevo
    
    private void imprimirEstadoGlobal() {
        System.out.println("\n-----"
                + ""
                + " Estado de la partida -----");
        if (estadoGlobal.isEmpty()) {
            System.out.println("(sin datos aún)");
        } else {
            estadoGlobal.values().forEach(e -> {
                System.out.print(e.nombre + " (" + e.color + "):");
                for (int i = 0; i < e.pos.length; i++) {
                    System.out.print(" f" + i + "=" + e.pos[i]);
                }
                System.out.println();
            });
        }
        System.out.println("--------------------------------\n");
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

    
    public static void main(String[] args) {
        // Color inicial es irrelevante; el broker asigna el elegido al aceptar el registro
        Jugador jugador = new Jugador(0, "SinNombre", "", Color.AZUL);
        Partida partida = new Partida("P001");
        partida.agregarJugador(jugador);

        VistaJugador vista = new VistaJugador(jugador);
        ControladorTurno ctrl = new ControladorTurno(partida, vista.proxy);
        vista.controlador = ctrl;

        // Autodescubrimiento
        int discoveryPort = 5001;
        DiscoveryClient.Result r = DiscoveryClient.discover(discoveryPort, 2500);
        if (r != null) {
            System.out.println("Broker descubierto en " + r.host + ":" + r.port);
            vista.conectar(r.host, r.port);
        } else {
            System.out.println("No se encontró broker. Intentando en 127.0.0.1:5000");
            vista.conectar("127.0.0.1", 5000);
        }

        System.out.println("\nCliente iniciado - Esperando conexión al broker...\n");
        vista.iniciarJuego();
    }
}
