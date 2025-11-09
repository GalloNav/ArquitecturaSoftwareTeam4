package com.mycompany.parchis_demo.vista;

import java.util.Scanner;

import com.mycompany.parchis_demo.control.ControladorTurno;
import com.mycompany.parchis_demo.control.red.ProxyCliente;
import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.Jugador;
import com.mycompany.parchis_demo.modelo.Partida;
import com.mycompany.parchis_demo.modelo.ResultadoTurno;
import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;

/**
 * Clase que representa la vista del jugador en el juego Parchis.
 */
public class VistaJugador {
    
    private Jugador jugador;
    private ProxyCliente proxy;
    private ControladorTurno controlador;
    private int miIdAsignado = -1;
    private boolean partidaIniciada = false;
    private boolean esMiTurno = false;
    private Scanner scanner;

    private volatile boolean seguir = true; // para salir cuando termine la partida
    
    public VistaJugador(Jugador jugador) {
        this.jugador = jugador;
        this.proxy = new ProxyCliente();
        this.scanner = new Scanner(System.in);
    }

    public void conectar(String host, int puerto) {
        proxy.conectar(host, puerto);
        proxy.setManejadorEventos(this::actualizar);
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println("[" + jugador.getNombre() + "] " + mensaje);
    }

    /**
     * Recibe los eventos desde el broker y los procesa.
     */
    public void actualizar(EventoPartida evento) {
        if (evento.getTipoEvento() == TipoEvento.JUGADOR_CONECTADO && 
            evento.getMensaje().startsWith("Tu ID es:")) {
            
            String[] partes = evento.getMensaje().split(":");
            miIdAsignado = Integer.parseInt(partes[1].trim());
            jugador.setId(miIdAsignado);
            
            System.out.println("\n___________________________________________");
            System.out.println("  | CONECTADO | Tu ID es: " + miIdAsignado);
            System.out.println("  Nombre: " + jugador.getNombre());
            System.out.println("  Color: " + jugador.getColor());
            System.out.println("  Esperando a que inicie la partida...");
            System.out.println("______________________________________________\n");
            return;
        }

        if (evento.getTipoEvento() == TipoEvento.PARTIDA_INICIADA) {
            partidaIniciada = true;
            System.out.println("\n>>> " + evento.getMensaje() + " <<<\n");
            return;
        }

        if (evento.getTipoEvento() == TipoEvento.FICHA_MOVIDA ||
            evento.getTipoEvento() == TipoEvento.FICHA_SALE_BASE ||
            evento.getTipoEvento() == TipoEvento.CAPTURA ||
            evento.getTipoEvento() == TipoEvento.ENTRA_PASILLO_COLOR ||
            evento.getTipoEvento() == TipoEvento.FICHA_EN_META ||
            evento.getTipoEvento() == TipoEvento.SEIS_REPITE_TURNO ||
            evento.getTipoEvento() == TipoEvento.TRES_SEISES_PIERDE_TURNO ||
            evento.getTipoEvento() == TipoEvento.MOVIMIENTO_IMPOSIBLE ||
            evento.getTipoEvento() == TipoEvento.JUGADOR_CONECTADO ||
            evento.getTipoEvento() == TipoEvento.JUGADOR_DESCONECTADO) {
            System.out.println("[Evento] " + evento.getMensaje());
        }
        //Turno
        if (evento.getTipoEvento() == TipoEvento.TURNO_CAMBIADO && partidaIniciada) {
            if (evento.getMensaje().contains("jugador " + miIdAsignado)) {
                esMiTurno = true;
                System.out.println("\n________________________________________");
                System.out.println("***| ES TU TURNO! |***");
                System.out.println("Presiona ENTER para lanzar el dado...");
                System.out.println("__________________________________________");
            } else {
                esMiTurno = false;
                System.out.println("\n>>> " + evento.getMensaje() + " - Esperando...\n");
            }
        }
        
        if (evento.getTipoEvento() == TipoEvento.PARTIDA_TERMINADA ||
            evento.getTipoEvento() == TipoEvento.VICTORIA) {
            System.out.println("[Evento] " + evento.getMensaje());
            seguir = false; // detener bucle
            return;
        }
    }
    
    /**
     * Inicia el bucle principal para esperar la entrada del usuario
     */
    public void iniciarJuego() {
    System.out.println("\nEsperando tu turno...");
    while (seguir) {
        if (esMiTurno) {
            // Mostrar menú de acciones
            System.out.println("\nAcciones: [ENTER] lanzar dado | [s] estado global | [i] iniciar partida");
            System.out.print("> ");
            String cmd = scanner.nextLine(); // solo ENTER produce ""

            // ---- SOLO ENTER VACÍO lanza el dado ----
            if (cmd.isEmpty()) {
                int idFicha = leerIdFicha(); // 0..3
                ResultadoTurno res = controlador.procesarTurno(jugador, idFicha);

                if (res.getValorDado() != null) {
                    System.out.println("[" + jugador.getNombre() + "] lanzó: " + res.getValorDado());
                }

                if (!res.exito()) {
                    System.out.println("[Movimiento inválido] " + res.mensaje());
                    esMiTurno = true; // reintentar
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

            // ---- s: estado global (No consume turno) ----
            if (cmd.equalsIgnoreCase("s")) {
                System.out.println(controlador.snapshotEstadoGlobal());
                esMiTurno = true;
                continue;
            }

            // ---- i: iniciar partida bajo solicitud (NO consume turno) ----
            if (cmd.equalsIgnoreCase("i")) {
                controlador.iniciarPartidaSiPosible();
                esMiTurno = true;
                continue;
            }

            // ---- cualquier otra tecla: NO hacer nada, no consume turno ----
            System.out.println("(Comando no reconocido. Usa ENTER, 's' o 'i')");
            esMiTurno = true;
        }

        try { Thread.sleep(350); } catch (InterruptedException ignored) {}
    }

    System.out.println("\nLa partida ha terminado. Cerrando cliente...\n");
}
    
    private void imprimirEstadoJugador() {
        System.out.println("\n===== Estado de " + jugador.getNombre() + " (" + jugador.getColor() + ") =====");
        jugador.getFichas().forEach(f -> {
            String pos;
            if (f.estaEnBase()) pos = "BASE";
            else if (f.estaEnMeta()) pos = "META";
            else if (f.estaEnPasillo()) pos = "PAS(" + f.getIndicePasillo() + ")";
            else pos = "A" + f.getPosicion();
            System.out.println(" - Ficha " + f.getId() + ": " + pos);
        });
        System.out.println("=============================================\n");
    }
        
    private int leerIdFicha() {
        while (true) {
            System.out.print("Elige ficha a mover (0..3): ");
            String entrada = scanner.nextLine().trim();
            try {
                int id = Integer.parseInt(entrada);
                if (id >= 0 && id <= 3) return id;
            } catch (NumberFormatException ignored) {}
            System.out.println("Entrada invalida. Debe ser 0, 1, 2 o 3.");
        }
    }

    public static void main(String[] args) {
            // IMPORTANTE: Cambiar el nombre y color para cada PC
            // PC 1:
            //Jugador jugador = new Jugador(0, "Candy", "avatar1.png", Color.ROJO);

            // PC 2: Este lo descomentamos en la otra pc para hacer la prueba:
            Jugador jugador = new Jugador(0, "GalloNav", "avatar2.png", Color.AZUL);

            Partida partida = new Partida("P001");
            partida.agregarJugador(jugador);

            VistaJugador vista = new VistaJugador(jugador);
    
            ControladorTurno ctrl = new ControladorTurno(partida, vista.proxy);
            vista.controlador = ctrl;

            // cmd ipconfig para localizar su ip:
            vista.conectar("192.168.100.10", 5000); // Aqui hay que poner la IP de la pc BROKER si lo van a probar.

            System.out.println("\nCliente iniciado - Esperando conexion al broker...\n");
            
            // Iniciar el bucle de juego manual
            vista.iniciarJuego();
        }
    }