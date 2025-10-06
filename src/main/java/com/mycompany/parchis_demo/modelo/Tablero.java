package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoCasilla;
import java.util.*;

/**
 *
 * @author Sergio Aboytia
 */
public class Tablero {
    private LinkedList<Casilla> casillas;
    private Map<Color, List<Integer>> zonasSeguras;
    private Map<Color, Integer> posicionesInicio;
    private Map<Color, List<Integer>> posicionesCasa;
    private Map<Color, List<Integer>> posicionesMeta;

    public Tablero() {
        casillas = new LinkedList<>();
        zonasSeguras = new HashMap<>();
        posicionesInicio = new HashMap<>();
        posicionesCasa = new HashMap<>();
        posicionesMeta = new HashMap<>();
        inicializarTablero();
    }

    public void inicializarTablero() {
        for (int i = 0; i < 68; i++) {
            casillas.add(new Casilla(i, TipoCasilla.NORMAL, null));
        }
        configurarPosicionesEspeciales();
    }

    public Casilla getCasilla(int posicion) {
        if (posicion < 0 || posicion >= casillas.size()) return null;
        return casillas.get(posicion);
    }

    public void configurarPosicionesEspeciales() {
        // Ejemplo x
        zonasSeguras.put(Color.ROJO, Arrays.asList(5, 12));
        zonasSeguras.put(Color.AZUL, Arrays.asList(22, 29));
        zonasSeguras.put(Color.VERDE, Arrays.asList(39, 46));
        zonasSeguras.put(Color.AMARILLO, Arrays.asList(56, 63));
    }

    public int calcularNuevaPosicion(Ficha ficha, int movimiento) {
        int nuevaPos = ficha.getPosicion() + movimiento;
        if (nuevaPos > 68) nuevaPos = 68;
        return nuevaPos;
    }

    public boolean hayFichaEn(int posicion) {
        Casilla c = getCasilla(posicion);
        return c != null && c.estaOcupada();
    }

    public List<Ficha> getFichasEn(int posicion) {
        Casilla c = getCasilla(posicion);
        return (c != null) ? c.getFichas() : new ArrayList<>();
    }

    public boolean esZonaSegura(int posicion, Color color) {
        return zonasSeguras.containsKey(color) && zonasSeguras.get(color).contains(posicion);
    }

    public boolean esCasillaInicio(int posicion, Color color) {
        return posicionesInicio.containsKey(color) && posicionesInicio.get(color) == posicion;
    }

    public boolean puedeCapturar(Ficha otra) {
        Casilla c = getCasilla(otra.getPosicion());
        return c != null && !c.esSeguraParaColor(otra.getColor());
    }
}
