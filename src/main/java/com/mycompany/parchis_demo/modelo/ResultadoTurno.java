package com.mycompany.parchis_demo.modelo;

/**
 *
 * @author Sergio Aboytia
 */
public class ResultadoTurno {
    private boolean exito;
    private String mensaje;
    private boolean turnoExtra;
    private boolean capturaRealizada;

    public ResultadoTurno(boolean exito, String mensaje, boolean turnoExtra, boolean capturaRealizada) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.turnoExtra = turnoExtra;
        this.capturaRealizada = capturaRealizada;
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean tieneTurnoExtra() {
        return turnoExtra;
    }

    public boolean huboCapturaRealizada() {
        return capturaRealizada;
    }
    
    
}
