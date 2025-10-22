/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.TipoEvento;
import java.time.Instant;

/**
 *
 * @author Sergio Aboytia
 */
public class EventoPartida {
    private TipoEvento tipoEvento;
    private Jugador jugadorAfectado;
    private Ficha fichaAfectada;
    private int valorDado;
    private int posicionAnterior;
    private int posicionNueva;
    private String mensaje;
    private long timestamp;

    public EventoPartida(TipoEvento tipoEvento, Jugador jugadorAfectado, Ficha fichaAfectada, 
                         int valorDado, int posicionAnterior, int posicionNueva, String mensaje) {
        this.tipoEvento = tipoEvento;
        this.jugadorAfectado = jugadorAfectado;
        this.fichaAfectada = fichaAfectada;
        this.valorDado = valorDado;
        this.posicionAnterior = posicionAnterior;
        this.posicionNueva = posicionNueva;
        this.mensaje = mensaje;
        this.timestamp = System.currentTimeMillis();
    }
    
    public EventoPartida(TipoEvento tipoEvento, Jugador jugadorAfectado, String mensaje) {
        this(tipoEvento, jugadorAfectado, null, 0, 0, 0, mensaje);
    }
    
    //Constructor para las pruebas
    public EventoPartida(String mensaje) {
    this.mensaje = mensaje;
}


    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public Jugador getJugadorAfectado() {
        return jugadorAfectado;
    }

    public void setJugadorAfectado(Jugador jugadorAfectado) {
        this.jugadorAfectado = jugadorAfectado;
    }

    public Ficha getFichaAfectada() {
        return fichaAfectada;
    }

    public void setFichaAfectada(Ficha fichaAfectada) {
        this.fichaAfectada = fichaAfectada;
    }

    public int getValorDado() {
        return valorDado;
    }

    public void setValorDado(int valorDado) {
        this.valorDado = valorDado;
    }

    public int getPosicionAnterior() {
        return posicionAnterior;
    }

    public void setPosicionAnterior(int posicionAnterior) {
        this.posicionAnterior = posicionAnterior;
    }

    public int getPosicionNueva() {
        return posicionNueva;
    }

    public void setPosicionNueva(int posicionNueva) {
        this.posicionNueva = posicionNueva;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    
}
