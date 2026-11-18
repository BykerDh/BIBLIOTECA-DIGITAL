package com.biblioteca.patterns.behavioral.observer;

public record EventoCirculacion(Tipo tipo, long usuarioId, long libroId, long referenciaId) {
    public enum Tipo { PRESTAMO, DEVOLUCION, VENCIMIENTO, CANCELACION_RESERVA }
}
