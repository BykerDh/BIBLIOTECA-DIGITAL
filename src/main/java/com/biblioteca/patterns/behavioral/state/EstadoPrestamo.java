package com.biblioteca.patterns.behavioral.state;

public interface EstadoPrestamo {
    String nombre();
    default boolean permiteLectura() { return false; }
    default boolean permiteDevolucion() { return false; }
    default void devolver(CicloPrestamo contexto) {
        throw new IllegalStateException("El prestamo " + nombre().toLowerCase() + " no admite devolucion");
    }
}
