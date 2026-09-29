package com.biblioteca.patterns.structural.Bridge;

public class MotorEpub implements MotorLectura {

    @Override
    public String abrirLibro(String titulo) {
        return "El libro \"" + titulo + "\" fue abierto con el motor EPUB.";
    }

    @Override
    public String getFormato() {
        return "EPUB";
    }
}