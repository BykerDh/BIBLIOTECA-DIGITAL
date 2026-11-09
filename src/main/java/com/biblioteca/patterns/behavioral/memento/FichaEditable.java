package com.biblioteca.patterns.behavioral.memento;

import com.biblioteca.domain.Models.Libro;

// Originator: decide que datos se recuerdan y cuales nunca se revierten.
public final class FichaEditable {
    private Libro estado;
    public FichaEditable(Libro estado) { this.estado = estado; }
    public Libro estado() { return estado; }

    public Recuerdo guardar() {
        return new Recuerdo(estado.id(), estado.titulo(), estado.autor(), estado.genero(),
                estado.idioma(), estado.descripcion(), estado.editorial(), estado.paginas());
    }

    public void restaurar(Recuerdo recuerdo) {
        if (recuerdo.libroId() != estado.id()) throw new IllegalArgumentException("El recuerdo pertenece a otro libro");
        estado = new Libro(estado.id(), recuerdo.titulo(), recuerdo.autor(), recuerdo.genero(),
                recuerdo.idioma(), recuerdo.descripcion(), estado.isbn(), recuerdo.editorial(),
                recuerdo.paginas(), estado.accesoMinimo(), estado.licencias(), estado.activo());
    }

    // Snapshot inmutable: no contiene archivos, prestamos, ISBN ni politicas de acceso.
    public record Recuerdo(long libroId, String titulo, String autor, String genero, String idioma,
                           String descripcion, String editorial, Integer paginas) {}
}
