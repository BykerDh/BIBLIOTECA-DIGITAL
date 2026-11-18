package com.biblioteca.patterns.behavioral.observer;

import com.biblioteca.repository.BibliotecaRepository;
import org.springframework.stereotype.Component;

@Component
public class ObservadorAvisos implements ObservadorCirculacion {
    private final BibliotecaRepository repo;
    public ObservadorAvisos(BibliotecaRepository repo) { this.repo = repo; }
    @Override public void actualizar(EventoCirculacion evento) {
        String mensaje = switch (evento.tipo()) {
            case PRESTAMO -> "Prestamo #" + evento.referenciaId() + " confirmado. Consulta su vencimiento en Mis prestamos.";
            case DEVOLUCION -> "Devolucion del prestamo #" + evento.referenciaId() + " registrada.";
            case VENCIMIENTO -> "El prestamo #" + evento.referenciaId() + " ha vencido. Su acceso digital finalizo.";
            case CANCELACION_RESERVA -> null;
        };
        if (mensaje != null) repo.notificar(evento.usuarioId(),mensaje);
    }
}
