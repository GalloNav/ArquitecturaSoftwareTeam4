package com.mycompany.parchis_demo.modelo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResultadoTurnoTest {

    private ResultadoTurno resultado;

    @Before
    public void setUp() {
        resultado = new ResultadoTurno(true, "Movimiento exitoso", true, false);
    }

    @Test
    public void testIsExito() {
        assertTrue("Debe indicar éxito", resultado.isExito());
    }

    @Test
    public void testGetMensaje() {
        assertEquals("Mensaje debe coincidir", "Movimiento exitoso", resultado.getMensaje());
    }

    @Test
    public void testTieneTurnoExtra() {
        assertTrue("Debe indicar turno extra", resultado.tieneTurnoExtra());
    }

    @Test
    public void testHuboCapturaRealizada() {
        assertFalse("No debe indicar captura realizada", resultado.huboCapturaRealizada());
    }

    @Test
    public void testValoresDiferentes() {
        ResultadoTurno otro = new ResultadoTurno(false, "Error", false, true);
        assertFalse(otro.isExito());
        assertEquals("Error", otro.getMensaje());
        assertFalse(otro.tieneTurnoExtra());
        assertTrue(otro.huboCapturaRealizada());
    }
}
