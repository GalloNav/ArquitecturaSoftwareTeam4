package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoCasilla;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase Casilla.
 * 
 * @author Sergio
 */
public class CasillaTest {

    private Casilla casilla;
    private Ficha fichaRoja;
    private Ficha fichaVerde;

    @Before
    public void setUp() {
        casilla = new Casilla(5, TipoCasilla.NORMAL, Color.ROJO);
        fichaRoja = new Ficha(1, Color.ROJO);
        fichaVerde = new Ficha(2, Color.VERDE);
    }

    @Test
    public void testAgregarFicha() {
        casilla.agregarFicha(fichaRoja);
        assertTrue("La casilla debe estar ocupada tras agregar una ficha", casilla.estaOcupada());
        assertEquals("Debe contener exactamente una ficha", 1, casilla.getFichas().size());
        assertEquals("La ficha agregada debe ser la fichaRoja", fichaRoja, casilla.getFichas().get(0));
    }

    @Test
    public void testRemoverFicha() {
        casilla.agregarFicha(fichaRoja);
        casilla.removerFicha(fichaRoja);
        assertFalse("La casilla no debe estar ocupada tras remover la ficha", casilla.estaOcupada());
    }

    @Test
    public void testEstaOcupada() {
        assertFalse("Inicialmente no debe estar ocupada", casilla.estaOcupada());
        casilla.agregarFicha(fichaRoja);
        assertTrue("Debe estar ocupada después de agregar una ficha", casilla.estaOcupada());
    }

    @Test
    public void testEsSeguraParaColor_TipoSegura() {
        Casilla casillaSegura = new Casilla(10, TipoCasilla.SEGURA, Color.AMARILLO);
        assertTrue("Las casillas seguras deben ser seguras para cualquier color",
                casillaSegura.esSeguraParaColor(Color.ROJO));
        assertTrue("Las casillas seguras deben ser seguras para cualquier color",
                casillaSegura.esSeguraParaColor(Color.AZUL));
    }

    @Test
    public void testEsSeguraParaColor_ColorPropio() {
        Casilla casillaColorPropio = new Casilla(15, TipoCasilla.NORMAL, Color.ROJO);
        assertTrue("Debe ser segura para el color asociado", casillaColorPropio.esSeguraParaColor(Color.ROJO));
        assertFalse("No debe ser segura para otros colores", casillaColorPropio.esSeguraParaColor(Color.VERDE));
    }

    @Test
    public void testGetFichas() {
        casilla.agregarFicha(fichaRoja);
        casilla.agregarFicha(fichaVerde);
        List<Ficha> fichas = casilla.getFichas();
        assertEquals("Debe haber dos fichas en la casilla", 2, fichas.size());
        assertTrue("Debe contener ficha roja", fichas.contains(fichaRoja));
        assertTrue("Debe contener ficha verde", fichas.contains(fichaVerde));
    }

    @Test
    public void testGetNumero() {
        assertEquals("El número de la casilla debe ser 5", 5, casilla.getNumero());
    }

    @Test
    public void testGetTipo() {
        assertEquals("El tipo de casilla debe ser NORMAL", TipoCasilla.NORMAL, casilla.getTipo());
    }

    @Test
    public void testGetColorAsociado() {
        assertEquals("El color asociado debe ser ROJO", Color.ROJO, casilla.getColorAsociado());
    }
}
