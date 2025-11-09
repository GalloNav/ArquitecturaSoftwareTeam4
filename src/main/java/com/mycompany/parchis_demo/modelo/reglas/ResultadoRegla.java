/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.parchis_demo.modelo.reglas;

/**
 *
 * @author ernes
 */

/**
 * Motor de reglas del juego. Orquesta validaciones y efectos de movimiento.
 */
public class ResultadoRegla {
    public final boolean consumioMovimiento;
    
    private ResultadoRegla(boolean c){ 
        this.consumioMovimiento = c; 
    }
    
    public static ResultadoRegla ok() { 
        return new ResultadoRegla(true); 
    }
    
    public static ResultadoRegla seguir() { 
        return new ResultadoRegla(false); 
    }
}
