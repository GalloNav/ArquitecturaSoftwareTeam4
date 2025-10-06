/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    public Ficha(int id, Color color) {
        this.id = id;
        this.color = color;
        this.posicion = 0;
    }

    public void mover(int casillas) {
        posicion += casillas;
        if (posicion > 68) {
            posicion = 68; //Se supone que está es la meta, pero no, lo dejaremos para después xd
        }
    }

    public void volverACasa() {
        this.posicion = 0;
    }

    public boolean estaEnCasa() {
        return posicion == 0;
    }

    public boolean estaEnMeta() {
        return posicion >= 68; //Podemos cambiar el valor depués
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

    
}