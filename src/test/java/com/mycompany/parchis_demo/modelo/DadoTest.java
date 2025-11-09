package com.mycompany.parchis_demo.modelo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase Dado.
 * 
 * @author Sergio Aboytia
 */
public class DadoTest {

    private Dado dado;

    @Before
    public void setUp() {
        dado = new Dado();
    }

    @Test
    public void testLanzar_DevuelveNumeroEntre1y6() {
        for (int i = 0; i < 100; i++) { 
            int resultado = dado.lanzar();
            assertTrue("El valor debe ser >= 1", resultado >= 1);
            assertTrue("El valor debe ser <= 6", resultado <= 6);
        }
    }

    @Test
    public void testGetUltimoValor_CoincideConUltimoTiro() {
        int valorLanzado = dado.lanzar();
        int ultimo = dado.getUltimoValor();
        assertEquals("El valor devuelto debe coincidir con el último lanzamiento", valorLanzado, ultimo);
    }

    @Test
    public void testEsTurnoExtra_CuandoSaca6() {
        for (int i = 0; i < 50; i++) {
            int valor = dado.lanzar();
            if (valor == 6) {
                assertTrue("Debe otorgar turno extra cuando saca 6", dado.esTurnoExtra());
            } else {
                assertFalse("No debe otorgar turno extra cuando no saca 6", dado.esTurnoExtra());
            }
        }
    }
}
