package com.mycompany.parchis_demo.vista;

import com.mycompany.parchis_demo.control.ControladorTurno;
import com.mycompany.parchis_demo.control.red.ProxyCliente;
import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.Jugador;
import com.mycompany.parchis_demo.modelo.Partida;
import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;
import java.util.Scanner;

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

        if (evento.getTipoEvento() == TipoEvento.JUGADOR_CONECTADO ||
            evento.getTipoEvento() == TipoEvento.JUGADOR_DESCONECTADO ||
            evento.getTipoEvento() == TipoEvento.FICHA_MOVIDA ||
            evento.getTipoEvento() == TipoEvento.CAPTURA) {
            System.out.println("[Evento] " + evento.getMensaje());
        }

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
    }
    
    /**
     * Inicia el bucle principal para esperar la entrada del usuario
     */
        public void iniciarJuego() {
            System.out.println("\nEsperando tu turno...");

            while (true) {
                if (esMiTurno) {
                    scanner.nextLine(); // Espera a que presiones Enter

                    System.out.println("\nLanzando dado...");
                    controlador.procesarTurno(jugador, 0);

                    esMiTurno = false; // Ya jugaste, esperar siguiente turno
                    System.out.println("\nEsperando a los demas jugadores...\n");
                }

                try {
                    Thread.sleep(500); // Evita consumir CPU
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
            vista.conectar("IP(aqui va su ip)", 5000); // Aqui hay que poner la IP de la pc BROKER si lo van a probar.

            System.out.println("\nCliente iniciado - Esperando conexion al broker...\n");
            
            // Iniciar el bucle de juego manual
            vista.iniciarJuego();
        }
    }