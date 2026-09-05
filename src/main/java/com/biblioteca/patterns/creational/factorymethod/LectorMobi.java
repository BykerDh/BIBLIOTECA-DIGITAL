package com.biblioteca.patterns.creational.factorymethod;

public class LectorMobi implements LectorDigital {

    @Override
    public String abrirLibro(String titulo) {
        return "Abriendo el libro '" + titulo + "' en formato MOBI.";
    }

    @Override
    public String getFormato() {
        return "MOBI";
    }
}