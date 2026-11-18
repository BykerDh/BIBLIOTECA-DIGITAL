package com.biblioteca.patterns.behavioral.observer;

import com.biblioteca.repository.BibliotecaRepository;
import org.springframework.stereotype.Component;

@Component
public class ObservadorAuditoria implements ObservadorCirculacion {
    private final BibliotecaRepository repo;
    public ObservadorAuditoria(BibliotecaRepository repo) { this.repo = repo; }
    @Override public void actualizar(EventoCirculacion evento) {
        boolean reserva = evento.tipo() == EventoCirculacion.Tipo.CANCELACION_RESERVA;
        repo.auditar(evento.usuarioId(),evento.tipo().name(),reserva ? "RESERVA" : "PRESTAMO",evento.referenciaId());
    }
}
