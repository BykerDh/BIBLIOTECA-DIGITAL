package com.biblioteca.patterns.behavioral.observer;

import com.biblioteca.patterns.behavioral.mediator.MediadorCirculacion;
import com.biblioteca.repository.BibliotecaRepository;
import org.springframework.stereotype.Component;

@Component
public class ObservadorDisponibilidad implements ObservadorCirculacion {
    private final MediadorCirculacion mediador;
    private final BibliotecaRepository repo;
    public ObservadorDisponibilidad(MediadorCirculacion mediador, BibliotecaRepository repo) {
        this.mediador = mediador; this.repo = repo;
    }
    @Override public void actualizar(EventoCirculacion evento) {
        // Observer distribuye el evento; Mediator sigue decidiendo a quien corresponde el turno.
        if (repo.libroActivo(evento.libroId())) mediador.disponibilidadCambiada(evento.libroId());
    }
}
