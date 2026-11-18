package com.biblioteca.patterns.structural.composite;

import java.util.ArrayList;
import java.util.List;
import com.biblioteca.patterns.behavioral.visitor.VisitanteCatalogo;

// Composite permite recorrer libros y subcolecciones mediante el mismo contrato.
public interface ComponenteCatalogo {
    long id();
    String nombre();
    void aceptar(VisitanteCatalogo visitante);
    default boolean esColeccion() { return false; }
    default List<ComponenteCatalogo> hijos() { return List.of(); }

    record LibroHoja(long id,String nombre) implements ComponenteCatalogo {
        public void aceptar(VisitanteCatalogo visitante) { visitante.visitar(this); }
    }

    final class ColeccionNodo implements ComponenteCatalogo {
        private final long id;
        private final String nombre;
        private final List<ComponenteCatalogo> hijos = new ArrayList<>();
        public ColeccionNodo(long id,String nombre) { this.id=id; this.nombre=nombre; }
        public long id() { return id; }
        public String nombre() { return nombre; }
        public List<ComponenteCatalogo> hijos() { return List.copyOf(hijos); }
        public boolean esColeccion() { return true; }
        public void agregar(ComponenteCatalogo hijo) { hijos.add(hijo); }
        public void aceptar(VisitanteCatalogo visitante) { visitante.visitar(this); }
    }
}
