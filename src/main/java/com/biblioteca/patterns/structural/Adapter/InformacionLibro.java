package com.biblioteca.patterns.structural.Adapter;


public class InformacionLibro {

    private final String titulo;
    private final String autor;
    private final String formato;
    private final String origen;

    public InformacionLibro(String titulo, String autor,
            String formato, String origen) {
        this.titulo = titulo;
        this.autor = autor;
        this.formato = formato;
        this.origen = origen;
    }

    public String getResumen() {
        return "Titulo: " + titulo
                + " | Autor: " + autor
                + " | Formato: " + formato
                + " | Fuente: " + origen;
    }
}
