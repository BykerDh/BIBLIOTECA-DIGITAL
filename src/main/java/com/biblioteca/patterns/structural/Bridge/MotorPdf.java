package com.biblioteca.patterns.structural.Bridge;

public class MotorPdf implements MotorLectura {

    @Override
    public String abrirLibro(String titulo) {
        return "El libro \"" + titulo + "\" fue abierto con el motor PDF.";
    }

    @Override
    public String getFormato() {
        return "PDF";
    }
}