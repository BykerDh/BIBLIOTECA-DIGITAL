package com.biblioteca.patterns.structural.flyweight;

import java.util.Locale;
import java.util.Map;
import org.springframework.http.MediaType;

public final class FabricaFormatosCompartidos {
    // El conjunto es finito y se publica de forma segura al inicializar la clase.
    private static final Map<String, FormatoLectura> FORMATOS = Map.of(
            "PDF", new FormatoLectura("PDF", MediaType.APPLICATION_PDF),
            "EPUB", new FormatoLectura("EPUB", MediaType.parseMediaType("application/epub+zip")),
            "MOBI", new FormatoLectura("MOBI", MediaType.parseMediaType("application/x-mobipocket-ebook")));

    private FabricaFormatosCompartidos() {}

    public static FormatoLectura obtener(String formato) {
        if (formato == null) throw new IllegalArgumentException("Formato obligatorio");
        FormatoLectura compartido = FORMATOS.get(formato.trim().toUpperCase(Locale.ROOT));
        if (compartido == null) throw new IllegalArgumentException("Formato no soportado");
        return compartido;
    }
}
