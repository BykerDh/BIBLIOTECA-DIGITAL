package com.biblioteca.patterns.behavioral.state;

import com.biblioteca.domain.Models.Prestamo;
import java.time.LocalDateTime;

public final class CicloPrestamo {
    private Prestamo prestamo;
    private EstadoPrestamo estado;
    private final LocalDateTime ahora;

    // Contexto por operacion; las fechas persistidas son la fuente del estado, no otra columna redundante.
    public CicloPrestamo(Prestamo prestamo, LocalDateTime ahora) {
        this.prestamo = prestamo; this.ahora = ahora;
        estado = prestamo.devueltoEn() != null ? new PrestamoDevuelto()
                : !prestamo.venceEn().isAfter(ahora) ? new PrestamoVencido() : new PrestamoVigente();
    }
    public String nombre() { return estado.nombre(); }
    public boolean permiteLectura() { return !prestamo.prestadoEn().isAfter(ahora) && estado.permiteLectura(); }
    public boolean permiteDevolucion() { return estado.permiteDevolucion(); }
    public Prestamo devolver() { estado.devolver(this); return prestamo; }

    void registrarDevolucion() {
        prestamo = new Prestamo(prestamo.id(),prestamo.usuarioId(),prestamo.libroId(),
                prestamo.prestadoEn(),prestamo.venceEn(),ahora);
        estado = new PrestamoDevuelto();
    }
}
