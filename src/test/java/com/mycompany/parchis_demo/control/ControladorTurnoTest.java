package com.mycompany.parchis_demo.control;

import com.mycompany.parchis_demo.control.red.ProxyCliente;
import com.mycompany.parchis_demo.modelo.*;
import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ControladorTurnoTest {

    private ControladorTurno controlador;
    private Partida partida;
    private Jugador jugador;
    private Ficha ficha;

    class DadoMock extends Dado {
        private int valor = 3;
        @Override
        public int lanzar() { return valor; }
        @Override
        public boolean esTurnoExtra() { return false; }
    }

    class TableroMock extends Tablero {
        @Override
        public int calcularNuevaPosicion(Ficha f, int mov) { return f.getPosicion() + mov; }
        @Override
        public boolean puedeCapturar(Ficha f) { return true; }
    }

    class PartidaMock extends Partida {
        private Dado dado = new DadoMock();
        private Tablero tablero = new TableroMock();
        private List<Jugador> jugadores = new ArrayList<>();
        private EventoPartida ultimoEvento;

        public PartidaMock() {
            super("Partida de prueba"); 
        }
    
        @Override
        public Dado getDado() { return dado; }

        @Override
        public Tablero getTablero() { return tablero; }
    
        @Override
        public List<Jugador> getJugadores() { return jugadores; }

        @Override
        public void notificarObservadores(EventoPartida e) {
            this.ultimoEvento = e;
            System.out.println("Evento notificado: " + e.getMensaje());
        }   

        public EventoPartida getUltimoEvento() {
            return ultimoEvento;
        }
    }

    @Before
    public void setUp() {
        partida = new PartidaMock();
        ProxyCliente proxyMock = new ProxyCliente() {
            @Override
            public void enviarEvento(EventoPartida evento) {
            }
        };        
        jugador = new Jugador(1, "copesito", "Jugador1", Color.ROJO);
        ficha = new Ficha(0, Color.ROJO);
        jugador.getFichas().clear();
        jugador.getFichas().add(ficha);
        partida.getJugadores().add(jugador);
        controlador = new ControladorTurno(partida, proxyMock);
    }

    @Test
    public void testValidarMovimiento_FichaEnMeta() {
        ficha.setPosicion(100); //meta
        boolean resultado = controlador.validarMovimiento(ficha, 3);
        assertFalse("Una ficha en meta no debería poder moverse", resultado);
    }

    @Test
    public void testAplicarMovimiento_CambiaPosicion() {
        ficha.setPosicion(5);
        controlador.aplicarMovimiento(ficha, 3, jugador);
        assertEquals("La ficha debe moverse a 8", 8, ficha.getPosicion());
    }

    @Test
    public void testProcesarTurno_Exito() {
        ficha.setPosicion(0);
        ResultadoTurno resultado = controlador.procesarTurno(jugador, 0);
        assertTrue("El turno debería ser exitoso", resultado.isExito());
        assertEquals("Ficha movida", resultado.getMensaje());
        assertTrue("La posición debió cambiar", ficha.getPosicion() > 0);
    }

    @Test
    public void testProcesarCaptura_VictimaVuelveACasa() {
        Ficha atacante = new Ficha(1, Color.ROJO);
        atacante.setPosicion(10);

        Ficha victima = new Ficha(2, Color.AZUL);
        victima.setPosicion(10);

        controlador.procesarCaptura(atacante, victima);
        assertTrue("La ficha víctima debería volver a casa", victima.estaEnCasa());
    }
}
