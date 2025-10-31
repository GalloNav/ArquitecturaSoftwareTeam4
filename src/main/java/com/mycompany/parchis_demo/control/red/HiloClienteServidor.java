package com.mycompany.parchis_demo.control.red;
import com.google.gson.Gson;
import com.mycompany.parchis_demo.modelo.EventoPartida;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Hilo que maneja la comunicación con un cliente individual.
 * Escucha los mensajes JSON y los reenvía al Broker para su procesamiento.
 * 
 * @author Ana
 */
public class HiloClienteServidor implements Runnable {
    private Socket socket;
    private Broker broker;
    private PrintWriter out;
    private Gson gson = new Gson();
    
    
    public HiloClienteServidor(Socket socket, Broker broker, PrintWriter out) {
        this.socket = socket;
        this.broker = broker;
        this.out = out;
    }
    
    
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {
            String mensaje;
            while ((mensaje = in.readLine()) != null) {
                EventoPartida evento = gson.fromJson(mensaje, EventoPartida.class);
                broker.procesarEvento(evento, out); // pasa el emisor
            }
            
        } catch (IOException e) {
            System.out.println("Cliente desconectado: " + socket.getInetAddress());
            broker.eliminarCliente(out);
        }
    }
}