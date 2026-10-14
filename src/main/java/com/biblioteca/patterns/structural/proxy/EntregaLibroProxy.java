package com.biblioteca.patterns.structural.proxy;

import com.biblioteca.patterns.structural.adapter.ContenidoLibro;
import com.biblioteca.patterns.structural.decorator.EntregaContenido;
import java.io.IOException;
import java.util.Objects;


public final class EntregaLibroProxy implements EntregaContenido {
    private final CargadorContenido cargador;
    private EntregaBase entregaReal;

    public EntregaLibroProxy(CargadorContenido cargador) {
        this.cargador = Objects.requireNonNull(cargador);
    }

    @Override
    public synchronized ContenidoLibro entregar() throws IOException {
        
        if (entregaReal == null) {
            entregaReal = new EntregaBase(Objects.requireNonNull(cargador.cargar()));
        }
        return entregaReal.entregar();
    }
}
