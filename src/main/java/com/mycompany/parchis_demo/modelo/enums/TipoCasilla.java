/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.mycompany.parchis_demo.modelo.enums;

/**
 *
 * @author Sergio Aboytia
 */
public enum TipoCasilla {
    NORMAL,          // casilla del anillo sin propiedades especiales
    SEGURA,          // no hay capturas aquí
    SALIDA,          // salida del color (desde base con 5)
    ENTRADA_PASILLO, // casilla del anillo donde entras al pasillo del color
    PASILLO_COLOR,   // casillas internas del pasillo (sin capturas ni barreras)
    META             // meta final del color
}
