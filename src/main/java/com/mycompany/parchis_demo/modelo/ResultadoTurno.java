package com.mycompany.parchis_demo.modelo;

public class ResultadoTurno {
    private final boolean exito;
    private final String  mensaje;
    private final boolean turnoExtra;
    private final boolean capturaRealizada;
    private final Integer valorDado; // tiro del dado

    public ResultadoTurno(boolean exito, String mensaje, boolean turnoExtra,
                          boolean capturaRealizada, Integer valorDado) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.turnoExtra = turnoExtra;
        this.capturaRealizada = capturaRealizada;
        this.valorDado = valorDado;
    }

    // Getters (mantén solo uno por atributo para evitar confusión)
    public boolean isExito()          { return exito; }
    public String  getMensaje()       { return mensaje; }
    public boolean isTurnoExtra()     { return turnoExtra; }
    public boolean isCapturaRealizada(){ return capturaRealizada; }
    public Integer getValorDado()     { return valorDado; }

    // Si quieres conservar los alias usados en tu vista:
    public boolean exito()        { return exito; }
    public String  mensaje()      { return mensaje; }
    public boolean turnoExtra()   { return turnoExtra; }
}
