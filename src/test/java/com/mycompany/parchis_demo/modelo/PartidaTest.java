package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.EstadoJugador;
import com.mycompany.parchis_demo.modelo.enums.EstadoPartida;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase Partida.
 * Todas las pruebas usan el código real sin agregar métodos extras.
 */
public class PartidaTest {

    private Partida partida;
    private Jugador jugador1;
    private Jugador jugador2;

    @Before
    public void setUp() {
        partida = new Partida("P001");
        jugador1 = new Jugador(1, "Sergio", "avatar1.png", Color.ROJO);
        jugador2 = new Jugador(2, "Copesan", "avatar2.png", Color.AZUL);
    }

    @Test
    public void testAgregarJugador() {
        assertTrue(partida.agregarJugador(jugador1));
        assertTrue(partida.getJugadores().contains(jugador1));
    }

    @Test
    public void testRemoverJugador() {
        partida.agregarJugador(jugador1);
        partida.removerJugador(jugador1);
        assertFalse(partida.getJugadores().contains(jugador1));
    }

    @Test
    public void testPuedeIniciar() {
        assertFalse(partida.puedeIniciar());
        partida.agregarJugador(jugador1);
        partida.agregarJugador(jugador2);
        assertTrue(partida.puedeIniciar());
    }

    @Test
    public void testIniciarPartida() {
        partida.agregarJugador(jugador1);
        partida.agregarJugador(jugador2);
        assertTrue(partida.iniciarPartida());
        assertEquals(EstadoPartida.EN_CURSO, partida.getEstadoPartida());
        assertNotNull(partida.getJugadorActual());
    }

    @Test
    public void testSiguienteTurno() {
        partida.agregarJugador(jugador1);
        partida.agregarJugador(jugador2);
        partida.iniciarPartida();
        Jugador actualAntes = partida.getJugadorActual();
        partida.siguienteTurno();
        assertNotEquals(actualAntes, partida.getJugadorActual());
    }

    @Test
    public void testTodosTienenFichasEnMeta() {
        partida.agregarJugador(jugador1);
        partida.agregarJugador(jugador2);

        assertFalse(partida.todosTienenFichasEnMeta());

        jugador1.getFichas().forEach(f -> f.mover(68));
        jugador2.getFichas().forEach(f -> f.mover(68));

        assertTrue(partida.todosTienenFichasEnMeta());
    }

    @Test
    public void testVerificarCondicionesVictoria() {
        partida.agregarJugador(jugador1);
        partida.agregarJugador(jugador2);

        assertNull(partida.verificarCondicionesVictoria());

        jugador1.getFichas().forEach(f -> f.mover(68));

        Jugador ganador = partida.verificarCondicionesVictoria();
        assertEquals(jugador1, ganador);
        assertEquals(EstadoPartida.FINALIZADA, partida.getEstadoPartida());
    }

    @Test
    public void testEstaLlena() {
        partida.agregarJugador(jugador1);
        partida.agregarJugador(jugador2);
        partida.agregarJugador(new Jugador(3, "Lucía", "avatar3.png", Color.VERDE));
        partida.agregarJugador(new Jugador(4, "GalloNav", "avatar4.png", Color.AMARILLO));

        assertTrue(partida.estaLlena());
    }

    @Test
    public void testGetJugadoresConectadosYListos() {
        partida.agregarJugador(jugador1);
        partida.agregarJugador(jugador2);

        assertEquals(2, partida.getJugadoresConectados());

        jugador1.setEstadoJugador(EstadoJugador.LISTO);
        jugador2.setEstadoJugador(EstadoJugador.LISTO);

        assertEquals(2, partida.getJugadoresListos());
    }

    @Test
    public void testGettersBasicos() {
        assertNotNull(partida.getTablero());
        assertNotNull(partida.getDado());
        assertEquals(EstadoPartida.ESPERANDO, partida.getEstadoPartida());
    }
}
