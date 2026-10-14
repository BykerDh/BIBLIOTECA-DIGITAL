package com.biblioteca.patterns.structural.proxy;

import com.biblioteca.patterns.structural.adapter.ContenidoLibro;
import java.io.IOException;

@FunctionalInterface
public interface CargadorContenido {
    ContenidoLibro cargar() throws IOException;
}
