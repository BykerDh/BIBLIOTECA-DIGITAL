package com.biblioteca.patterns.creational.singleton;

import java.util.List;

public class ConfiguracionBiblioteca {

    private static final ConfiguracionBiblioteca INSTANCIA = new ConfiguracionBiblioteca();

    private final String nombreSistema;
    private final int diasPrestamo;
    private final int maxPrestamosPorUsuario;
    private final List<String> formatosPermitidos;

    private ConfiguracionBiblioteca() {
        this.nombreSistema = "Sistema de Biblioteca Digital";
        this.diasPrestamo = 15;
        this.maxPrestamosPorUsuario = 3;
        this.formatosPermitidos = List.of("PDF", "EPUB", "MOBI");
    }

    public static ConfiguracionBiblioteca getInstancia() {
        return INSTANCIA;
    }

    public String getNombreSistema() {
        return nombreSistema;
    }

    public int getDiasPrestamo() {
        return diasPrestamo;
    }

    public int getMaxPrestamosPorUsuario() {
        return maxPrestamosPorUsuario;
    }

    public List<String> getFormatosPermitidos() {
        return formatosPermitidos;
    }
}