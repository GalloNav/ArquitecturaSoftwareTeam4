/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.EstadoJugador;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sergio Aboytia
 */
public class Jugador implements ObservadorPartida{
    private int id;
    private String nombre;
    private String avatar;
    private Color color;
    private List<Ficha> fichas;
    private int puntos;
    private EstadoJugador estadoJugador;
    
    

    public Jugador(int id, String nombre, String avatar, Color color) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.avatar = "";
        this.fichas = new ArrayList<>();
        this.puntos = 0;
        this.estadoJugador = EstadoJugador.CONECTADO;

        fichas = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            fichas.add(new Ficha(i, color));
        }
    }
    
    

    //Wakala
    public int tirarDado() {
        Dado dado = new Dado();
        return dado.lanzar();
    }

    /*
    public Ficha seleccionarFicha(int idFicha) {
        return fichas.stream().filter(f -> f.getId() == idFicha).findFirst().orElse(null);
    }
    */
    public Ficha seleccionarFicha(int idFicha) {
        if (idFicha >= 0 && idFicha < fichas.size()) {
            return fichas.get(idFicha);
        }
        return null;
    }

    public List<Ficha> getFichasActivas() {
        return fichas.stream().filter(f -> !f.estaEnCasa() && !f.estaEnMeta()).toList();
    }

    public int getFichasEnCasa() {
        return (int) fichas.stream().filter(Ficha::estaEnCasa).count();
    }

    public int getFichasEnMeta() {
        return (int) fichas.stream().filter(Ficha::estaEnMeta).count();
    }

    public boolean puedeJugar() {
        return estadoJugador == EstadoJugador.ACTIVO;
    }

    public boolean tieneFichasFueraDeCasa() {
        return fichas.stream().anyMatch(f -> !f.estaEnCasa());
    }

    public void actualizarPuntos(int puntos) {
        this.puntos += puntos;
    }
    
    @Override
    public void actualizar(EventoPartida evento) {
        System.out.println("Jugador " + nombre + " recibió evento: " + evento.getMensaje());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<Ficha> getFichas() {
        return fichas;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public EstadoJugador getEstadoJugador() {
        return estadoJugador;
    }

    public void setEstadoJugador(EstadoJugador estadoJugador) {
        this.estadoJugador = estadoJugador;
    }
}
