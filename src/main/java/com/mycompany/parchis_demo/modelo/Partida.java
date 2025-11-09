package com.mycompany.parchis_demo.modelo;

import com.mycompany.parchis_demo.modelo.enums.EstadoJugador;
import com.mycompany.parchis_demo.modelo.enums.EstadoPartida;
import com.mycompany.parchis_demo.modelo.enums.TipoEvento;
import java.util.*;

/**
 *
 * @author Sergio Aboytia
 */
public class Partida {
    private String id;
    private List<Jugador> jugadores;
    private Tablero tablero;
    private Dado dado;
    private Jugador jugadorActual;
    private EstadoPartida estadoPartida;
    private int turnos;
    private int maxJugadores = 4;
    private int minJugadores = 2;
    private int indiceJugadorActual;
    private List<ObservadorPartida> observadores;

    public Partida(String id) {
        this.id = id;
        this.jugadores = new ArrayList<>();
        this.tablero = new Tablero();
        this.dado = new Dado();
        this.estadoPartida = EstadoPartida.ESPERANDO;
        this.turnos = 0;
        this.maxJugadores = 4;
        this.minJugadores = 2;
        this.indiceJugadorActual = 0;
        this.observadores = new ArrayList<>();
    }
    
    public boolean agregarJugador(Jugador jugador) {
        if (jugadores.size() < maxJugadores) {
            jugadores.add(jugador);
            notificarObservadores(new EventoPartida(TipoEvento.JUGADOR_CONECTADO, jugador, null, 0, 0, 0, jugador.getNombre() + " se ha unido"));
            return true;
        }
        return false;
    }
    
    public void removerJugador(Jugador jugador) {
        jugadores.remove(jugador);
        notificarObservadores(new EventoPartida(TipoEvento.JUGADOR_DESCONECTADO, jugador, null, 0, 0, 0, jugador.getNombre() + " se ha desconectado"));
        
    // Si se va el ultimo == terminar
    if (jugadores.isEmpty()) {
        terminarPartida("no hay jugadores conectados");
        return;
    }

    // Si la partida estaba en curso y quedan < 2 == terminar
    if (estadoPartida == EstadoPartida.EN_CURSO && jugadores.size() < minJugadores) {
        terminarPartida("menos de 2 jugadores activos");
    }

    // Ajustar indice actual si se fue alguien antes del que jugaba
    if (indiceJugadorActual >= jugadores.size()) {
        indiceJugadorActual = indiceJugadorActual % jugadores.size();
        jugadorActual = jugadores.get(indiceJugadorActual);
    }
    }
    
    // NO arrancar sola: iniciar solo si se pide y hay >= 2
    public boolean iniciarPartidaBajoSolicitud() {
        if (estadoPartida != EstadoPartida.ESPERANDO) return false;
            if (!puedeIniciar()) {
                notificarObservadores(new EventoPartida(
                TipoEvento.MOVIMIENTO_IMPOSIBLE, null, null, 0, 0, 0,
                "No se puede iniciar: se requieren al menos " + minJugadores + " jugadores"
        ));
        return false;
    }
    return iniciarPartida(); 
}
    
    public boolean puedeIniciar() {
        return jugadores.size() >= minJugadores;
    }
    
    public boolean iniciarPartida() {
        if (puedeIniciar()) {
            estadoPartida = EstadoPartida.EN_CURSO;
            jugadorActual = jugadores.get(0);
            indiceJugadorActual = 0;
            notificarObservadores(new EventoPartida(TipoEvento.PARTIDA_INICIADA, jugadorActual, null, 0, 0, 0, "La partida ha comenzado"));
            return true;
        }
        return false;
    }
    
    public void terminarPartida(String motivo) {
        if (estadoPartida == EstadoPartida.FINALIZADA) return;
            estadoPartida = EstadoPartida.FINALIZADA;
        notificarObservadores(new EventoPartida(
        TipoEvento.PARTIDA_TERMINADA, jugadorActual, null, 0, 0, 0,
        "Partida finalizada: " + motivo
    ));
}
    
    public void siguienteTurno() {
        indiceJugadorActual = (indiceJugadorActual + 1) % jugadores.size();
        jugadorActual = jugadores.get(indiceJugadorActual);
        turnos++;
        notificarCambioTurno();
    }
    
    public void notificarCambioTurno() {
        notificarObservadores(new EventoPartida(TipoEvento.TURNO_CAMBIADO, jugadorActual, null, 0, 0, 0, "Turno de " + jugadorActual.getNombre()));
    }
    
    public void notificarObservadores(EventoPartida evento) {
        for (ObservadorPartida o : observadores)
            o.actualizar(evento);
    }
    
    public void agregarObservador(ObservadorPartida obs) {
        observadores.add(obs); 
    }
    
    public void removerObservador(ObservadorPartida obs) {
        observadores.remove(obs); 
    }
    
    public boolean todosTienenFichasEnMeta() {
        return jugadores.stream().allMatch(j -> j.getFichasEnMeta() == 4);
    }
    
    public Jugador verificarCondicionesVictoria() {
        for (Jugador j : jugadores) {
            if (j.getFichasEnMeta() == 4) {
                notificarObservadores(new EventoPartida(
                        TipoEvento.VICTORIA,
                        j,
                        j.getNombre() + " ha ganado la partida."
                ));
                estadoPartida = EstadoPartida.FINALIZADA;
                return j;
            }
        }
        return null;
    }
    
    public boolean estaLlena() {
        return jugadores.size() == maxJugadores;
    }
    
    public int getJugadoresConectados() {
        return jugadores.size();
    }

    public int getJugadoresListos() {
        return (int) jugadores.stream().filter(j -> j.getEstadoJugador()== EstadoJugador.LISTO).count();
    }
    
    public List<Jugador> getJugadores() {
        return jugadores;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public Dado getDado() {
        return dado;
    }

    public Jugador getJugadorActual() {
        return jugadorActual;
    }

    public EstadoPartida getEstadoPartida() {
        return estadoPartida;
    }
}
