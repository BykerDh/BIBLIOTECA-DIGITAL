package com.biblioteca.patterns.creational.prototype;

import com.biblioteca.domain.Models.Libro;

// Prototype: copia los metadatos sin copiar la identidad ni el ISBN.
public class PlantillaLibroDigital {
    private final Libro original;
    public PlantillaLibroDigital(Libro original) { this.original = original; }
    public Libro clonar(String tituloNuevo) {
        return new Libro(0,tituloNuevo,original.autor(),original.genero(),original.idioma(),
                original.descripcion(),null,original.editorial(),original.paginas(),original.accesoMinimo(),
                original.licencias(),true);
    }
}
