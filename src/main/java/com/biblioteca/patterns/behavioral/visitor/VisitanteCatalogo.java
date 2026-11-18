package com.biblioteca.patterns.behavioral.visitor;

import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.ColeccionNodo;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.LibroHoja;

public interface VisitanteCatalogo {
    void visitar(ColeccionNodo coleccion);
    void visitar(LibroHoja libro);
}
