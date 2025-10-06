/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoCasilla;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sergio Aboytia
 */
public class Casilla {
    private int numero;
    private TipoCasilla tipo;
    private List<Ficha> fichas;
    private Color colorAsociado;

    public Casilla(int numero, TipoCasilla tipo, Color colorAsociado) {
        this.numero = numero;
        this.tipo = tipo;
        this.colorAsociado = colorAsociado;
        this.fichas = new ArrayList<>();
    }

    public void agregarFicha(Ficha ficha) {
        fichas.add(ficha);
    }

    public void removerFicha(Ficha ficha) {
        fichas.remove(ficha);
    }

    public boolean estaOcupada() {
        return !fichas.isEmpty();
    }

    public boolean esSeguraParaColor(Color color) {
        return tipo == TipoCasilla.SEGURA || color == colorAsociado;
    }

    public List<Ficha> getFichas() {
        return fichas;
    }

    public int getNumero() {
        return numero;
    }

    public TipoCasilla getTipo() {
        return tipo;
    }

    public Color getColorAsociado() {
        return colorAsociado;
    }
}