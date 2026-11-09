package com.biblioteca.patterns.behavioral.iterator;

import com.biblioteca.patterns.structural.composite.ComponenteCatalogo;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.LibroHoja;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public final class IteradorLibros implements Iterator<LibroHoja> {
    private final Deque<Iterator<ComponenteCatalogo>> pendientes = new ArrayDeque<>();
    private final Set<Long> coleccionesVisitadas = new HashSet<>();
    private final Set<Long> librosVisitados = new HashSet<>();
    private LibroHoja siguiente;

    public IteradorLibros(List<ComponenteCatalogo> raices) {
        pendientes.push(raices.iterator());
    }

    @Override public boolean hasNext() {
        // Profundidad sin recursion: omite duplicados y corta ciclos de colecciones.
        while (siguiente == null && !pendientes.isEmpty()) {
            Iterator<ComponenteCatalogo> cursor = pendientes.peek();
            if (!cursor.hasNext()) { pendientes.pop(); continue; }
            ComponenteCatalogo nodo = cursor.next();
            if (nodo.esColeccion()) {
                if (coleccionesVisitadas.add(nodo.id())) pendientes.push(nodo.hijos().iterator());
            } else if (nodo instanceof LibroHoja libro && librosVisitados.add(libro.id())) {
                siguiente = libro;
            }
        }
        return siguiente != null;
    }

    @Override public LibroHoja next() {
        if (!hasNext()) throw new NoSuchElementException("No hay mas libros");
        LibroHoja resultado = siguiente;
        siguiente = null;
        return resultado;
    }
}
