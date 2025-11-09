/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.parchis_demo.modelo.reglas;

import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.Ficha;
import com.mycompany.parchis_demo.modelo.Jugador;
import com.mycompany.parchis_demo.modelo.Partida;
import java.util.function.Consumer;

/**
 *
 * @author ernes
 */
public interface Regla {
    int prioridad(); // orden de evaluación
    
    boolean aplica(Partida p, Jugador j, Ficha f, int pasos);
    
    /**
     * Ejecuta la regla. Debe:
     * - Modificar el estado (tablero/ficha/partida) si consume el movimiento
     * - Emitir eventos usando el emisor (Consumer<EventoPartida>)
     * - Devolver ResultadoRegla.ok() si consumio el movimiento, o seguir() si no
     */
    
    ResultadoRegla ejecutar(Partida p, Jugador j, Ficha f, int pasos,
                            Consumer<EventoPartida> emitir);
}
