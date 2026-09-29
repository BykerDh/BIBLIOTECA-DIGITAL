package com.biblioteca.patterns.structural.Bridge;

// Abstraccion: separa el modo de lectura del formato del libro.
public abstract class SesionLectura {

    protected final MotorLectura motorLectura;

    protected SesionLectura(MotorLectura motorLectura) {
        this.motorLectura = motorLectura;
    }

    public abstract String iniciarLectura(String titulo);
}