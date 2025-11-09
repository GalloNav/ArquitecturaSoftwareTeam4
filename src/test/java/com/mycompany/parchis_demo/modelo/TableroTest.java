package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoCasilla;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TableroTest {

    private Tablero tablero;

    @Before
    public void setUp() {
        tablero = new Tablero();
    }

    @Test
    public void testInicializarTablero() {
        assertNotNull("La lista de casillas no debe ser nula", tablero);
        Casilla c = tablero.getCasilla(0);
        assertNotNull("Debe existir la casilla 0", c);
        assertEquals("La primera casilla debe ser NORMAL", TipoCasilla.NORMAL, c.getTipo());
    }

    @Test
    public void testGetCasillaValida() {
        Casilla c = tablero.getCasilla(5);
        assertNotNull("Debe devolver una casilla válida", c);
        assertEquals(5, c.getNumero());
    }

    @Test
    public void testGetCasillaInvalida() {
        assertNull("Posición negativa debe devolver null", tablero.getCasilla(-1));
        assertNull("Posición fuera de rango debe devolver null", tablero.getCasilla(100));
    }

    @Test
    public void testConfigurarPosicionesEspeciales() {
        assertTrue("Debe contener zonas seguras rojas", tablero.esZonaSegura(5, Color.ROJO));
        assertTrue("Debe contener zonas seguras azules", tablero.esZonaSegura(22, Color.AZUL));
        assertFalse("Posición fuera de zona segura", tablero.esZonaSegura(10, Color.VERDE));
    }

    @Test
    public void testCalcularNuevaPosicionNormal() {
        Ficha ficha = new Ficha(1, Color.ROJO);
        ficha.setPosicion(10);
        int nuevaPos = tablero.calcularNuevaPosicion(ficha, 5);
        assertEquals("Debe sumar correctamente el movimiento", 15, nuevaPos);
    }

    @Test
    public void testCalcularNuevaPosicionMayorQueMeta() {
        Ficha ficha = new Ficha(1, Color.ROJO);
        ficha.setPosicion(67);
        int nuevaPos = tablero.calcularNuevaPosicion(ficha, 10);
        assertEquals("Debe limitar la posición máxima a 68", 68, nuevaPos);
    }

    @Test
    public void testHayFichaEnSinFicha() {
        assertFalse("No debe haber fichas en casillas vacías", tablero.hayFichaEn(5));
    }

    @Test
    public void testGetFichasEn() {
        List<Ficha> fichas = tablero.getFichasEn(5);
        assertNotNull("Debe devolver una lista válida", fichas);
        assertTrue("Debe estar vacía al inicio", fichas.isEmpty());
    }

    @Test
    public void testEsZonaSegura() {
        assertTrue(tablero.esZonaSegura(5, Color.ROJO));
        assertFalse(tablero.esZonaSegura(10, Color.ROJO));
    }

    @Test
    public void testEsCasillaInicio() {
        assertFalse(tablero.esCasillaInicio(0, Color.ROJO));
    }

    @Test
    public void testPuedeCapturar() {
    Ficha ficha = new Ficha(1, Color.ROJO);
    ficha.setPosicion(10);
    boolean puede = tablero.puedeCapturar(ficha);
    assertTrue("Debe poder capturar en una casilla normal", puede);
}

}
