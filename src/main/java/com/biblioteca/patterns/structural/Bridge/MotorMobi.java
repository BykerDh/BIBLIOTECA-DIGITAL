package com.biblioteca.patterns.structural.Bridge;

public class MotorMobi implements MotorLectura {

    @Override
    public String abrirLibro(String titulo) {
        return "El libro \"" + titulo + "\" fue abierto con el motor MOBI.";
    }

    @Override
    public String getFormato() {
        return "MOBI";
    }
}