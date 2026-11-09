package com.biblioteca.patterns.behavioral.iterator;

import com.biblioteca.patterns.structural.composite.ComponenteCatalogo;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.LibroHoja;
import java.util.Iterator;
import java.util.List;

// Cada recorrido obtiene su propio cursor; no se comparte estado entre usuarios.
public final class RecorridoColeccion implements Iterable<LibroHoja> {
    private final List<ComponenteCatalogo> raices;

    public RecorridoColeccion(List<? extends ComponenteCatalogo> raices) {
        this.raices = List.copyOf(raices);
    }

    @Override public Iterator<LibroHoja> iterator() {
        return new IteradorLibros(raices);
    }
}
