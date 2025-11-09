package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.TipoEvento;

public class EventoPartida {

    private TipoEvento tipoEvento;
    private Jugador jugadorAfectado;
    private Ficha fichaAfectada;
    private int valorDado;
    private int posicionAnterior; // origen (desde)
    private int posicionNueva;    // destino (hasta)
    private String mensaje;
    private boolean turnoExtra;     // true si repite turno (saco 6 y NO es el 3ro)
    private boolean pierdeTurno;    // true si saco 3 seises == pierde el turno

    private long timestamp;

    public EventoPartida(TipoEvento tipoEvento, Jugador jugadorAfectado, Ficha fichaAfectada,
                         int valorDado, int posicionAnterior, int posicionNueva, String mensaje) {
        this.tipoEvento = tipoEvento;
        this.jugadorAfectado = jugadorAfectado;
        this.fichaAfectada = fichaAfectada;
        this.valorDado = valorDado;
        this.posicionAnterior = posicionAnterior;
        this.posicionNueva = posicionNueva;
        this.mensaje = mensaje;
        this.timestamp = System.currentTimeMillis();
    }

    public EventoPartida(TipoEvento tipoEvento, Jugador jugadorAfectado, String mensaje) {
        this(tipoEvento, jugadorAfectado, null, 0, 0, 0, mensaje);
    }

    // Getters originales 
    public TipoEvento getTipoEvento() { return tipoEvento; }
    public Jugador getJugadorAfectado() { return jugadorAfectado; }
    public Ficha getFichaAfectada() { return fichaAfectada; }
    public int getValorDado() { return valorDado; }
    public int getPosicionAnterior() { return posicionAnterior; }
    public int getPosicionNueva() { return posicionNueva; }
    public String getMensaje() { return mensaje; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setTipoEvento(TipoEvento tipoEvento) { 
        this.tipoEvento = tipoEvento; 
    }
    public void setJugadorAfectado(Jugador jugadorAfectado) { 
        this.jugadorAfectado = jugadorAfectado; 
    }
    public void setFichaAfectada(Ficha fichaAfectada) { 
        this.fichaAfectada = fichaAfectada; 
    }
    public void setValorDado(int valorDado) { 
        this.valorDado = valorDado; 
    }
    public void setPosicionAnterior(int posicionAnterior) { 
        this.posicionAnterior = posicionAnterior; 
    }
    public void setPosicionNueva(int posicionNueva) { 
        this.posicionNueva = posicionNueva; 
    }
    public void setMensaje(String mensaje) { 
        this.mensaje = mensaje; 
    }
    public void setTimestamp(long timestamp) { 
        this.timestamp = timestamp; 
    }
    public boolean isTurnoExtra() { 
        return turnoExtra; 
    }
    public void setTurnoExtra(boolean turnoExtra) { 
        this.turnoExtra = turnoExtra; 
    }
    public boolean isPierdeTurno() { 
        return pierdeTurno; 
    }
    public void setPierdeTurno(boolean pierdeTurno) { 
        this.pierdeTurno = pierdeTurno; 
    }

    // Aliases que usa tu VistaJugador 
    public Jugador getJugador() { 
        return jugadorAfectado; 
    }
    public Ficha getFicha() { 
        return fichaAfectada; 
    }
    public int getDesde() { 
        return posicionAnterior; 
    }
    public int getHasta() { 
        return posicionNueva; 
    }
}
