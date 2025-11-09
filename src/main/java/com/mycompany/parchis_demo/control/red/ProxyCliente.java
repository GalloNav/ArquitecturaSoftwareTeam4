package com.mycompany.parchis_demo.control.red;

import com.google.gson.Gson;
import com.mycompany.parchis_demo.modelo.EventoPartida;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Cliente que envia y recibe eventos de partida al servidor.
 */
public class ProxyCliente {
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();
    private Consumer<EventoPartida> manejadorEventos;

    /**
     * Metodo para conectar a los jugadores a traves de un puerto con el broker.
     */
    public void conectar(String host, int puerto) {
        try {
            socket = new Socket(host, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Conectado al servidor " + host + ":" + puerto);
            
            new Thread(this::escucharEventos).start();
        } catch (IOException e) {
            System.err.println("Error al conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Establece el callback para manejar eventos recibidos, este se reemplaza o
     * bien, cumple la función del Observer pattern.
     */
    public void setManejadorEventos(Consumer<EventoPartida> manejador) {
        this.manejadorEventos = manejador;
    }
    
    /**
     * Hilo que escucha los mensajes JSON del broker y los procesa.
     */
    private void escucharEventos() {
        try {
            String json;
            while ((json = in.readLine()) != null) {
                EventoPartida evento = gson.fromJson(json, EventoPartida.class);
                System.out.println("[Cliente] Mensaje recibido: " + evento.getMensaje());
                
                if (manejadorEventos != null) {
                    manejadorEventos.accept(evento);
                }
            }
        } catch (IOException e) {
            System.out.println("Conexion cerrada con el broker.");
        }
    }

    /**
     * Metodo para enviar un evento a traves del flujo.
     */
    public void enviarEvento(EventoPartida evento) {
        if (out != null) {
            String json = gson.toJson(evento);
            out.println(json);
        }
    }
}