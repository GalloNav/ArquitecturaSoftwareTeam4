package com.mycompany.parchis_demo.control;

import com.mycompany.parchis_demo.control.red.ProxyCliente;
import com.mycompany.parchis_demo.modelo.EventoPartida;
import com.mycompany.parchis_demo.modelo.Ficha;
import com.mycompany.parchis_demo.modelo.Jugador;
import com.mycompany.parchis_demo.modelo.Partida;
import com.mycompany.parchis_demo.modelo.ResultadoTurno;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;

public class ControladorTurno {
    
    private Partida partida;
    private ProxyCliente proxy;
    private int tirosConsecutivos;
    
    public ControladorTurno(Partida partida, ProxyCliente proxy) {
        this.partida = partida;
        this.proxy = proxy;
        this.tirosConsecutivos = 0;
    }
    
    public ResultadoTurno procesarTurno(Jugador jugador, int idFicha) {
    int valorDado = partida.getDado().lanzar();
    Ficha ficha = jugador.seleccionarFicha(idFicha);

    if (ficha == null) {
        return new ResultadoTurno(false, "Ficha no encontrada", false, false, valorDado);
    }

    if (!validarMovimiento(ficha, valorDado)) {
        return new ResultadoTurno(false, "Movimiento invalido", false, false, valorDado);
    }

    aplicarMovimiento(ficha, valorDado, jugador);
    verificarCaptura(ficha, ficha.getPosicion());

    boolean turnoExtra = partida.getDado().esTurnoExtra();
    boolean captura = false; // ajusta si detectas captura

    return new ResultadoTurno(true, "Ficha movida", turnoExtra, captura, valorDado);
}

    public boolean validarMovimiento(Ficha ficha, int casillas) {
        return !ficha.estaEnMeta();
    }
    
    public void iniciarPartidaSiPosible() {
    if (partida.iniciarPartidaBajoSolicitud()) {
        // ya se emite PARTIDA_INICIADA desde Partida.iniciarPartida()
    }
}

    // Texto con el estado de todos los jugadores (para imprimir en consola)
    public String snapshotEstadoGlobal() {
    StringBuilder sb = new StringBuilder();
    sb.append("===== Estado de la partida =====\n");
    for (Jugador j : partida.getJugadores()) {
        sb.append(j.getNombre()).append(" (").append(j.getColor()).append("): ");
        for (Ficha f : j.getFichas()) {
            String pos = f.estaEnBase() ? "BASE" :
                         f.estaEnMeta() ? "META" :
                         f.estaEnPasillo() ? "PAS(" + f.getIndicePasillo() + ")" :
                         "A" + f.getPosicion();
            sb.append(" f").append(f.getId()).append("=").append(pos).append("  ");
        }
        sb.append("\n");
    }
    sb.append("===============================\n");
    return sb.toString();
}
    
    /**
     * Aplica el movimiento y envia un solo evento al broker
     */
    public void aplicarMovimiento(Ficha ficha, int casillas, Jugador jugador) {
        int desde = ficha.getPosicion();
        int hasta = partida.getTablero().calcularNuevaPosicion(ficha, casillas);
        ficha.setPosicion(hasta);
        
        // Crear evento completo con informacion del jugador
        EventoPartida evento = new EventoPartida(
            TipoEvento.FICHA_MOVIDA, 
            jugador, 
            ficha, 
            casillas, 
            desde, 
            hasta,
            jugador.getNombre() + " (J" + jugador.getId() + ") lanzo " + casillas + 
            " y movio su ficha de " + desde + " a " + hasta
        );
        
        // Enviar al broker
        if (proxy != null) {
            proxy.enviarEvento(evento);
        }
        
        // Notificar localmente
        registrarMovimiento(evento);
    }
    
    /**
     * Registra el movimiento localmente (patrón Observer)
     */
    private void registrarMovimiento(EventoPartida evento) {
        partida.notificarObservadores(evento);
    }
    
    public void verificarCaptura(Ficha ficha, int nuevaPosicion) {
        // Si en la casilla hay exactamente un rival y NO es segura → capturar
    if (partida.getTablero().hayRivalSolo(nuevaPosicion, ficha.getColor())) {
        Ficha victima = partida.getTablero().buscarRivalEn(nuevaPosicion, ficha.getColor());
        if (victima != null) {
            procesarCaptura(ficha, victima);
        }
    }
    }
    
    public void procesarCaptura(Ficha atacante, Ficha victima) {
        victima.volverACasa();
        EventoPartida evento = new EventoPartida(
            TipoEvento.CAPTURA, null, atacante, 0, 0, atacante.getPosicion(),
            atacante.getColor() + " capturo una ficha de " + victima.getColor()
        );
        
        if (proxy != null) {
            proxy.enviarEvento(evento);
        }
        
        partida.notificarObservadores(evento);
    }
}