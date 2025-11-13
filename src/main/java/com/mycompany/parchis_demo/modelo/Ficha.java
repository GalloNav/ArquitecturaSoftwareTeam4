
package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;

/**
 *
 * @author Sergio Aboytia
 */
public class Ficha {
    private int id;
    private Color color;
    private int posicion;
    private boolean enPasillo;
    private boolean enMeta;
    private int indicePasillo;
    
    public static final int POS_BASE = -1;   // base real
    public static final int POS_META = 68;   // meta

    public Ficha(int id, Color color) {
        this.id = id;
        this.color = color;
        this.posicion = POS_BASE;
    }

    public void mover(int casillas) {
        posicion += casillas;
        if (posicion > 68) {
            posicion = 68; //Se supone que está es la meta, pero no, lo dejaremos para después xd
        }
    }
    
     /** ¿La ficha esta en base (aun no ha salido)? */
    public boolean estaEnBase() {
        return posicion == POS_BASE;
    }

    /** ¿Está recorriendo el anillo principal (68 casillas)? */
    public boolean estaEnAnillo() {
        return posicion >= 0 && posicion < 68 && !enPasillo && !enMeta;
    }

    /** ¿Está dentro del pasillo de su color? */
    public boolean estaEnPasillo() {
        return enPasillo;
    }

    /** ¿Llegó a la meta final? */
    public boolean estaEnMeta() {
        return posicion >= POS_META;
    }
    
    /** Marca que la ficha entró a su pasillo (índice 0). */
    public void entrarPasillo() {
        this.enPasillo = true;
        this.indicePasillo = 0;
        this.posicion = -1; // fuera del anillo
    }
    
    /** Avanza dentro del pasillo. */
    public void avanzarEnPasillo(int pasos) {
        this.indicePasillo += pasos;
        if (this.indicePasillo >= 7) { // llegó a meta exacta
            this.enPasillo = false;
            this.enMeta = true;
        }
    }

    public void volverACasa() {
        this.posicion = POS_BASE;
        this.enPasillo = false;
        this.enMeta = false;
        this.indicePasillo = -1;
    }
    
    /** Coloca la ficha directamente en meta (usado cuando gana). */
    public void llegarMeta() {
        this.enPasillo = false;
        this.enMeta = true;
        this.indicePasillo = 7;
        this.posicion = -1;
    }

    public boolean estaEnCasa() {
        return posicion == 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public int getIndicePasillo() {
        return indicePasillo;
    }

    public void setIndicePasillo(int indicePasillo) {
        this.indicePasillo = indicePasillo;
    }

    public boolean isEnPasillo() {
        return enPasillo;
    }

    public boolean isEnMeta() {
        return enMeta;
    }

    @Override
    public String toString() {
        String estado;
        if (estaEnBase()) estado = "En base";
        else if (enPasillo) estado = "En pasillo (" + indicePasillo + ")";
        else if (enMeta) estado = "En meta";
        else estado = "En anillo (pos " + posicion + ")";
        return "Ficha " + id + " [" + color + "] - " + estado;
    }

    
}