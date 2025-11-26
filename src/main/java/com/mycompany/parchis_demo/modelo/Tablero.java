package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.Color;
import com.mycompany.parchis_demo.modelo.enums.TipoCasilla;
import java.util.*;

/**
 *
 * @author Sergio Aboytia
 */
public class Tablero {

    // ------------------ Estructura base del tablero ------------------
    private final List<Casilla> casillas;                 // 0..67 (anillo)
    private final Map<Color, Integer> posicionesInicio;   // salida por color
    private final Map<Color, Integer> entradaPasillo;     // casilla del anillo donde se entra al pasillo
    private final Map<Color, List<Integer>> zonasSeguras; // seguras asociadas a color (opcional)
    private final Set<Integer> segurasGlobales;           // seguras comunes (estrellas/refugios)

    // (Opcionales si luego modelas casas/meta por lista de casillas)
    private final Map<Color, List<Integer>> posicionesCasa = new HashMap<>();
    private final Map<Color, List<Integer>> posicionesMeta = new HashMap<>();

    // Longitud típica del pasillo: 7 casillas por color (índices 0..6; 7 = meta)
    private static final int LONGITUD_PASILLO = 7;

    public Tablero() {
        this.casillas = new ArrayList<>(68);
        this.posicionesInicio = new HashMap<>();
        this.entradaPasillo = new HashMap<>();
        this.zonasSeguras = new HashMap<>();
        this.segurasGlobales = new HashSet<>();
        inicializarTablero();
    }

    // ========================================================================
    // Inicialización
    // ========================================================================

    public final void inicializarTablero() {
        casillas.clear();
        for (int i = 0; i < 68; i++) {
            casillas.add(new Casilla(i, TipoCasilla.NORMAL, null));
        }
        configurarPosicionesEspeciales();
    }

    /** Ajusta estas posiciones a tu plantilla real del tablero si difiere. */
    public void configurarPosicionesEspeciales() {
        // SALIDAS DE CADA COLOR
        posicionesInicio.put(Color.ROJO, 0);
        posicionesInicio.put(Color.AZUL, 17);
        posicionesInicio.put(Color.AMARILLO, 34);
        posicionesInicio.put(Color.VERDE, 51);

        // ENTRADAS A PASILLO DE CADA COLOR 
        //(La segunda vez que el jugador pase por esa posicion, entra automaticamente al pasillo)
        entradaPasillo.put(Color.ROJO, 4);
        entradaPasillo.put(Color.AZUL, 21);
        entradaPasillo.put(Color.AMARILLO, 38);
        entradaPasillo.put(Color.VERDE, 55);

        // SEGURAS (Las mismas que las salidas)
        segurasGlobales.clear();
        segurasGlobales.addAll(Arrays.asList(0,17,34,51));

        // SEGURAS POR COLOR
        zonasSeguras.put(Color.ROJO, Arrays.asList(0));
        zonasSeguras.put(Color.AZUL, Arrays.asList(17));
        zonasSeguras.put(Color.AMARILLO, Arrays.asList(34));
        zonasSeguras.put(Color.VERDE, Arrays.asList(51));

        // (Opcional) marca tipos en casillas
        casillas.get(0).setTipo(TipoCasilla.SALIDA);
        casillas.get(17).setTipo(TipoCasilla.SALIDA);
        casillas.get(34). setTipo(TipoCasilla.SALIDA);
        casillas.get(51).setTipo(TipoCasilla.SALIDA);

        for (int s : segurasGlobales) {
            casillas.get(s).setTipo(TipoCasilla.SEGURA);
        }
    }

    // ========================================================================
    // Acceso básico a casillas/fichas
    // ========================================================================

    public Casilla getCasilla(int posicion) {
        if (posicion < 0 || posicion >= casillas.size()) return null;
        return casillas.get(posicion);
    }

    public boolean hayFichaEn(int posicion) {
        Casilla c = getCasilla(posicion);
        return c != null && c.estaOcupada();
    }

    public List<Ficha> getFichasEn(int posicion) {
        Casilla c = getCasilla(posicion);
        return (c != null) ? c.getFichas() : Collections.emptyList();
    }

    // ========================================================================
    // Movimiento en el anillo (0..67 con wrap)
    // ========================================================================

    /** Devuelve la nueva posición en el anillo aplicando wrap. Si la ficha está en base devuelve su valor actual (<0). */
    public int calcularNuevaPosicion(Ficha ficha, int pasos) {
        int actual = ficha.getPosicion();
        if (actual < 0) return actual; // en base/pasillo/meta (no anillo)
        return (actual + pasos) % casillas.size();
    }

    /**
     * Chequeo de movimiento legal para anillo (sin resolver pasillo/meta).
     * - Salida desde base: sólo con 5 y validando destino
     * - Respeta barreras (propias o ajenas) en el trayecto
     * - Valida caída en casilla destino (seguras/barreras/captura)
     */
    public boolean movimientoLegal(Jugador j, Ficha f, int pasos) {
        if (f.estaEnMeta()) return false;

        // Salida desde base
        if (f.estaEnBase()) {
            int salida = casillaSalida(j.getColor());
            return pasos == 5 && puedeOcuparCasillaDeLlegada(j, salida, /*desdeBase*/ true);
        }

        int destino = calcularNuevaPosicion(f, pasos);

        // ¿entra justo a la casilla de entrada del pasillo?
        if (esEntradaPasillo(j.getColor(), destino)) {
            if (hayBarreraEntre(f.getPosicion(), destino)) return false;
            return true;
        }

        // Movimiento normal por anillo
        if (hayBarreraEntre(f.getPosicion(), destino)) return false;
        return puedeOcuparCasillaDeLlegada(j, destino, /*desdeBase*/ false);
    }

    /** ¿Hay una barrera en casillas intermedias entre 'desde' y 'hasta'? (no incluye el origen) */
    public boolean hayBarreraEntre(int desde, int hasta) {
        int size = casillas.size();
        int dist = ((hasta - desde) % size + size) % size; // distancia positiva
        for (int i = 1; i < dist; i++) {
            int pos = (desde + i) % size;
            if (esBarrera(pos)) return true;
        }
        return false;
    }

    // ========================================================================
    // Seguras, barreras y capturas
    // ========================================================================

    /** ¿La casilla es segura (global o asociada a algun color)? */
    public boolean esSegura(int pos) {
        if (segurasGlobales.contains(pos)) return true;
        for (var kv : zonasSeguras.entrySet()) {
            if (kv.getValue().contains(pos)) return true;
        }
        return false;
    }

    /** Dos fichas del mismo color en una casilla forman barrera. */
    public boolean esBarrera(int pos) {
        List<Ficha> fs = getFichasEn(pos);
        if (fs.size() != 2) return false;
        return fs.get(0).getColor() == fs.get(1).getColor();
    }

    /**
     * ¿Se puede ocupar la casilla destino?
     * - Prohíbe caer en barrera propia
     * - En segura: no puedes capturar (si hay rivales, inválido)
     * - En casilla normal: puedes capturar 1 rival; si hay 2 rivales (barrera ajena), inválido
     * - En salida con 5: se permite capturar si hay 1 rival (variante clásica)
     */
    private boolean puedeOcuparCasillaDeLlegada(Jugador j, int posDestino, boolean desdeBase) {
        List<Ficha> fichas = getFichasEn(posDestino);
        if (fichas.isEmpty()) return true;

        long propias = fichas.stream().filter(x -> x.getColor() == j.getColor()).count();
        if (propias >= 2) return false; // ya hay barrera propia

        long rivales = fichas.size() - propias;

        if (esSegura(posDestino) && rivales > 0) return false; // segura no admite captura

        // Salida con 5 captura en salida (si tu variante lo permite)
        if (desdeBase && esCasillaInicio(posDestino, j.getColor())) return true;

        // En casilla normal: 1 rival -> capturable; 2 rivales -> barrera ajena (bloquea)
        return rivales <= 1;
    }

    /** ¿Hay exactamente un rival (y ningún propio) en una casilla NO segura? */
    public boolean hayRivalSolo(int pos, Color delQueLlega) {
        if (esSegura(pos)) return false;
        List<Ficha> fs = getFichasEn(pos);
        long rivales = fs.stream().filter(f -> f.getColor() != delQueLlega).count();
        long propias = fs.size() - rivales;
        return rivales == 1 && propias == 0;
    }

    public Ficha buscarRivalEn(int pos, Color delQueLlega) {
        return getFichasEn(pos).stream()
                .filter(f -> f.getColor() != delQueLlega)
                .findFirst().orElse(null);
    }

    // ========================================================================
    // Salidas, pasillo y meta
    // ========================================================================

    public int casillaSalida(Color color) {
        return posicionesInicio.get(color);
    }

    public boolean esCasillaInicio(int pos, Color color) {
        Integer s = posicionesInicio.get(color);
        return s != null && s == pos;
    }

    /** ¿La posición del anillo corresponde a la entrada al pasillo del color? */
    public boolean esEntradaPasillo(Color color, int posAnillo) {
        Integer e = entradaPasillo.get(color);
        return e != null && e == posAnillo;
    }

    /**
     * Calcula avance dentro del pasillo (0..LONGITUD_PASILLO).
     * Devuelve -1 si no cabe el avance.
     */
    public int avanzarEnPasillo(Ficha f, int pasos) {
        int idx = f.getIndicePasillo();
        if (!f.estaEnPasillo() || idx < 0) return -1;
        int destino = idx + pasos;
        if (destino > LONGITUD_PASILLO) return -1; // exactitud requerida
        return destino;
    }

    /** ¿Con estos pasos entra exacto a meta dentro del pasillo? */
    public boolean puedeEntrarMeta(Ficha f, int pasos) {
        if (!f.estaEnPasillo()) return false;
        int idx = f.getIndicePasillo();
        return idx + pasos == LONGITUD_PASILLO;
    }

    // ========================================================================
    // Helpers de ocupación en anillo (útiles en reglas)
    // ========================================================================

    /** Coloca la ficha en una casilla del anillo (actualiza lista de la casilla y la posición de la ficha). */
    public void colocarFichaEnAnillo(Ficha f, int pos) {
        Casilla c = getCasilla(pos);
        if (c != null) {
            c.agregarFicha(f);
            f.setPosicion(pos);
        }
    }

    /** Quita la ficha de su casilla del anillo y la marca "fuera del anillo" (por ejemplo, base o pasillo). */
    public void quitarFichaDeAnillo(Ficha f) {
        if (f.getPosicion() >= 0) {
            Casilla c = getCasilla(f.getPosicion());
            if (c != null) c.removerFicha(f);
        }
        f.setPosicion(-1); // -1 = fuera del anillo (base/pasillo/meta)
    }
}