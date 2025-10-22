package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.EstadoJugador;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase Jugador.
 * 
 * @author Sergio
 */
public class JugadorTest {

    private Jugador jugador;

    @Before
    public void setUp() {
        jugador = new Jugador(1, "Sergio", "avatar.png", Color.ROJO);
    }

    @Test
    public void testTirarDadoDevuelveNumeroEntre1y6() {
        int resultado = jugador.tirarDado();
        assertTrue("El valor del dado debe estar entre 1 y 6", resultado >= 1 && resultado <= 6);
    }

    @Test
    public void testSeleccionarFichaPorIndiceValido() {
        Ficha f = jugador.seleccionarFicha(2);
        assertNotNull("Debe devolver una ficha válida si el índice existe", f);
        assertEquals("El id de la ficha debe ser 2", 2, f.getId());
        assertEquals("El color de la ficha debe coincidir con el jugador", Color.ROJO, f.getColor());
    }

    @Test
    public void testSeleccionarFichaPorIndiceInvalido() {
        Ficha f = jugador.seleccionarFicha(10);
        assertNull("Debe devolver null si el índice no existe", f);
    }

    @Test
    public void testGetFichasEnCasaInicialmenteDebeSer4() {
        assertEquals("Todas las fichas inician en casa", 4, jugador.getFichasEnCasa());
    }

    @Test
    public void testGetFichasEnMetaCuandoUnaLlega() {
        Ficha f = jugador.getFichas().get(0);
        f.setPosicion(70); 
        assertEquals("Debe tener una ficha en meta", 1, jugador.getFichasEnMeta());
    }

    @Test
    public void testGetFichasActivas() {
        Ficha f = jugador.getFichas().get(0);
        f.setPosicion(10); 
        List<Ficha> activas = jugador.getFichasActivas();
        assertEquals("Debe haber una ficha activa", 1, activas.size());
    }

    @Test
    public void testTieneFichasFueraDeCasa() {
        assertFalse("Al inicio no debe tener fichas fuera de casa", jugador.tieneFichasFueraDeCasa());
        jugador.getFichas().get(1).setPosicion(5);
        assertTrue("Debe tener fichas fuera de casa", jugador.tieneFichasFueraDeCasa());
    }

    @Test
    public void testActualizarPuntos() {
        jugador.actualizarPuntos(50);
        assertEquals("Los puntos deben actualizarse correctamente", 50, jugador.getPuntos());
    }

    @Test
    public void testPuedeJugarDependeDelEstado() {
        jugador.setEstadoJugador(EstadoJugador.ACTIVO);
        assertTrue("Debe poder jugar cuando está activo", jugador.puedeJugar());

        jugador.setEstadoJugador(EstadoJugador.CONECTADO);
        assertFalse("No debe poder jugar cuando no está activo", jugador.puedeJugar());
}

    @Test
    public void testGetYSetNombre() {
        jugador.setNombre("Dulce");
        assertEquals("Dulce", jugador.getNombre());
    }

    @Test
    public void testGetYSetAvatar() {
        jugador.setAvatar("nuevo.png");
        assertEquals("nuevo.png", jugador.getAvatar());
    }

    @Test
    public void testGetYSetPuntos() {
        jugador.setPuntos(120);
        assertEquals(120, jugador.getPuntos());
    }

    @Test
    public void testGetYSetColor() {
        jugador.setColor(Color.VERDE);
        assertEquals(Color.VERDE, jugador.getColor());
    }

    @Test
    public void testGetYSetEstadoJugador() {
        jugador.setEstadoJugador(EstadoJugador.CONECTADO);
        assertEquals(EstadoJugador.CONECTADO, jugador.getEstadoJugador());
    }

    @Test
    public void testGetYSetId() {
        jugador.setId(99);
        assertEquals(99, jugador.getId());
    }

    @Test
    public void testGetFichas() {
        assertEquals("Debe tener 4 fichas iniciales", 4, jugador.getFichas().size());
    }

    @Test
    public void testActualizarEventoNoLanzaExcepcion() {
        EventoPartida evento = new EventoPartida("Movimiento realizado");
        try {
            jugador.actualizar(evento);
        } catch (Exception e) {
            fail("El método actualizar no debería lanzar excepción");
        }
    }
}
