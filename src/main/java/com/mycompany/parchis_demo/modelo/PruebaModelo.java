package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;

/**
 *
 * @author Sergio Aboytia
 */
public class PruebaModelo {

    public static void main(String[] args) {
        // Crear un tablero
        Tablero tablero = new Tablero();
        tablero.inicializarTablero();
        System.out.println("Tablero inicializado.");

        // Crear dos jugadores
        Jugador j1 = new Jugador(1, "Candy", "avatar1.png", Color.ROJO);
        Jugador j2 = new Jugador(2, "GalloNav", "avatar2.png", Color.AZUL);

        // Crear partida
        Partida partida = new Partida("P001");
        partida.agregarJugador(j1);
        partida.agregarJugador(j2);

        // Mostrar jugadores
        System.out.println("Jugadores conectados:");
        for (Jugador j : partida.getJugadores()) {
            System.out.println(" - " + j.getNombre() + " (" + j.getColor() + ")");
        }

        // Lanzar el dado
        int valorDado = j1.tirarDado();
        System.out.println("\n" + j1.getNombre() + " tiro el dado y obtuvo: " + valorDado);

        // Seleccionar una ficha
        Ficha ficha = j1.seleccionarFicha(0);
        System.out.println("Ficha seleccionada: " + ficha.getId());

        // Mover la ficha
        ficha.mover(valorDado);
        System.out.println("Nueva posicion de la ficha: " + ficha.getPosicion());

        // Actualizar puntos
        j1.actualizarPuntos(10);
        System.out.println("Puntos actuales de " + j1.getNombre() + ": " + j1.getPuntos());

        // Verificar condiciones de victoria (solo para probar)
        Jugador ganador = partida.verificarCondicionesVictoria();
        if (ganador != null) {
            System.out.println("El ganador es: " + ganador.getNombre());
        } else {
            System.out.println("La partida continua...");
        }

        System.out.println("\nPrueba del modelo finalizada correctamente.");
    }
}
