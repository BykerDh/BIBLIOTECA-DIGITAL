package com.biblioteca.patterns.behavioral.mediator;

import com.biblioteca.service.ColaReservasService;
import com.biblioteca.service.NotificacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoordinadorDisponibilidad implements MediadorCirculacion {
    private final ColaReservasService cola;
    private final NotificacionService avisos;

    public CoordinadorDisponibilidad(ColaReservasService cola, NotificacionService avisos) {
        this.cola = cola;
        this.avisos = avisos;
    }

    // Participa en la transaccion del prestamo o reserva; no crea prestamos automaticos.
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void disponibilidadCambiada(long libroId) {
        cola.turnoDisponible(libroId).ifPresent(turno ->
                avisos.disponibilidad(turno.usuarioId(), turno.titulo()));
    }
}
