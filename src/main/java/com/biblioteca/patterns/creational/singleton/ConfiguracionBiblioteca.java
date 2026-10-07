package com.biblioteca.patterns.creational.singleton;

import java.util.List;

// Configuracion inmutable compartida por toda la aplicacion.
public final class ConfiguracionBiblioteca {
    private static final ConfiguracionBiblioteca INSTANCIA = new ConfiguracionBiblioteca();
    private final List<String> formatos = List.of("PDF", "EPUB", "MOBI");

    private ConfiguracionBiblioteca() {}

    public static ConfiguracionBiblioteca getInstancia() { return INSTANCIA; }
    public List<String> getFormatos() { return formatos; }
    public String getNombre() { return "Biblioteca Digital"; }
}
