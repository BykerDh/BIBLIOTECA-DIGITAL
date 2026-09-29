package com.biblioteca.patterns.structural.Bridge;

public class LecturaEnLinea extends SesionLectura {

    public LecturaEnLinea(MotorLectura motorLectura) {
        super(motorLectura);
    }

    @Override
    public String iniciarLectura(String titulo) {
        return "Modo en linea | "
                + motorLectura.abrirLibro(titulo)
                + " El contenido se lee desde la nube.";
    }
}