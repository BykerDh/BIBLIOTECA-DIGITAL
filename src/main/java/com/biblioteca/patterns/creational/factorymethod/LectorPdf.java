package com.biblioteca.patterns.creational.factorymethod;

public class LectorPdf implements LectorDigital {

    @Override
    public String abrirLibro(String titulo) {
        return "Abriendo el libro '" + titulo + "' en formato PDF.";
    }

    @Override
    public String getFormato() {
        return "PDF";
    }
}