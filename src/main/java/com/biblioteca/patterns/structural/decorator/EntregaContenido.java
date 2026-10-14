package com.biblioteca.patterns.structural.decorator;

import com.biblioteca.patterns.structural.adapter.ContenidoLibro;
import java.io.IOException;

// Decorator añade acceso y auditoria sin modificar la entrega base.
public interface EntregaContenido {
    ContenidoLibro entregar() throws IOException;

    record EntregaBase(ContenidoLibro contenido) implements EntregaContenido {
        public ContenidoLibro entregar() { return contenido; }
    }
    record EntregaProtegida(EntregaContenido siguiente, Runnable validar) implements EntregaContenido {
        public ContenidoLibro entregar() throws IOException { validar.run(); return siguiente.entregar(); }
    }
    record EntregaAuditada(EntregaContenido siguiente, Runnable registrar) implements EntregaContenido {
        public ContenidoLibro entregar() throws IOException { ContenidoLibro contenido=siguiente.entregar(); registrar.run(); return contenido; }
    }
}
