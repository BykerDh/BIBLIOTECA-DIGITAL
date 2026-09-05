package com.biblioteca.patterns.creational.factorymethod;

public class LectorEpub implements LectorDigital {

    @Override
    public String abrirLibro(String titulo) {
        return "Abriendo el libro '" + titulo + "' en formato EPUB.";
    }

    @Override
    public String getFormato() {
        return "EPUB";
    }
}