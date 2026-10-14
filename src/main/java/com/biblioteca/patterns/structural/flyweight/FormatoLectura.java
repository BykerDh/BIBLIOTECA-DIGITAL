package com.biblioteca.patterns.structural.flyweight;

import com.biblioteca.patterns.creational.factorymethod.LectorFactory.LectorDigital;
import org.springframework.http.MediaType;

// Estado intrinseco inmutable: nunca guarda usuarios, prestamos ni rutas de archivos.
public final class FormatoLectura implements LectorDigital {
    private final String formato;
    private final MediaType tipoContenido;

    FormatoLectura(String formato, MediaType tipoContenido) {
        this.formato = formato;
        this.tipoContenido = tipoContenido;
    }

    @Override public String formato() { return formato; }
    @Override public MediaType tipoContenido() { return tipoContenido; }
}
