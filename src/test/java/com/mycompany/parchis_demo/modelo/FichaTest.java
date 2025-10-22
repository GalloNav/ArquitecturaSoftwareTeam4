package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase Ficha.
 * 
 * @author Sergio Aboytia
 */
public class FichaTest {

    private Ficha ficha;

    @Before
    public void setUp() {
        ficha = new Ficha(1, Color.ROJO);
    }

    @Test
    public void testMover_AvanzarNormal() {
        ficha.setPosicion(5);
        ficha.mover(3);
        assertEquals("La ficha debe moverse 3 casillas", 8, ficha.getPosicion());
    }

    @Test
    public void testMover_NoSobrepasaMeta() {
        ficha.setPosicion(67);
        ficha.mover(5);
        assertEquals("La ficha no debe sobrepasar la meta (68)", 68, ficha.getPosicion());
    }

    @Test
    public void testVolverACasa() {
        ficha.setPosicion(30);
        ficha.volverACasa();
        assertEquals("La ficha debe regresar a casa (posición 0)", 0, ficha.getPosicion());
    }

    @Test
    public void testEstaEnCasa_True() {
        ficha.setPosicion(0);
        assertTrue("Debe indicar que está en casa", ficha.estaEnCasa());
    }

    @Test
    public void testEstaEnCasa_False() {
        ficha.setPosicion(5);
        assertFalse("No debe indicar que está en casa", ficha.estaEnCasa());
    }

    @Test
    public void testEstaEnMeta_True() {
        ficha.setPosicion(68);
        assertTrue("Debe indicar que está en meta", ficha.estaEnMeta());
    }

    @Test
    public void testEstaEnMeta_False() {
        ficha.setPosicion(10);
        assertFalse("No debe indicar que está en meta", ficha.estaEnMeta());
    }

    @Test
    public void testGettersYSetters() {
        ficha.setId(99);
        ficha.setColor(Color.AZUL);
        ficha.setPosicion(25);

        assertEquals(99, ficha.getId());
        assertEquals(Color.AZUL, ficha.getColor());
        assertEquals(25, ficha.getPosicion());
    }
}
