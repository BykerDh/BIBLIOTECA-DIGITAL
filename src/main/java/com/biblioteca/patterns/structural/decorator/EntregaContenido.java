package com.biblioteca.patterns.structural.decorator;

import com.biblioteca.patterns.structural.adapter.ContenidoLibro;

// Decorator añade acceso y auditoria sin modificar la entrega base.
public interface EntregaContenido {
    ContenidoLibro entregar();

    record EntregaBase(ContenidoLibro contenido) implements EntregaContenido {
        public ContenidoLibro entregar() { return contenido; }
    }
    record EntregaProtegida(EntregaContenido siguiente, Runnable validar) implements EntregaContenido {
        public ContenidoLibro entregar() { validar.run(); return siguiente.entregar(); }
    }
    record EntregaAuditada(EntregaContenido siguiente, Runnable registrar) implements EntregaContenido {
        public ContenidoLibro entregar() { ContenidoLibro contenido=siguiente.entregar(); registrar.run(); return contenido; }
    }
}
