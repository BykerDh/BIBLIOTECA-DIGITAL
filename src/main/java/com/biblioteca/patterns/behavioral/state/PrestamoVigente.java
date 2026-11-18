package com.biblioteca.patterns.behavioral.state;

public final class PrestamoVigente implements EstadoPrestamo {
    public String nombre() { return "VIGENTE"; }
    public boolean permiteLectura() { return true; }
    public boolean permiteDevolucion() { return true; }
    public void devolver(CicloPrestamo contexto) { contexto.registrarDevolucion(); }
}
