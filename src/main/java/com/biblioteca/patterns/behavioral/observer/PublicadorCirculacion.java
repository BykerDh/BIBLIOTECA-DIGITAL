package com.biblioteca.patterns.behavioral.observer;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicadorCirculacion {
    private final List<ObservadorCirculacion> observadores;
    public PublicadorCirculacion(List<ObservadorCirculacion> observadores) {
        this.observadores = List.copyOf(observadores);
    }

    // Notifica sincronicamente: si un observador falla, se revierte toda la operacion SQL.
    @Transactional(propagation = Propagation.MANDATORY)
    public void publicar(EventoCirculacion evento) {
        observadores.forEach(observador -> observador.actualizar(evento));
    }
}
