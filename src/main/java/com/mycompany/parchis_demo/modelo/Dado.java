/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchis_demo.modelo;

import java.util.Random;

/**
 *
 * @author Sergio Aboytia
 */
public class Dado {
    private int ultimoTiro;

    public int lanzar() {
        Random random = new Random();
        ultimoTiro = random.nextInt(6) + 1;
        return ultimoTiro;
    }

    public int getUltimoValor() {
        return ultimoTiro;
    }

    public boolean esTurnoExtra() {
        return ultimoTiro == 6;
    }
    
    
}
