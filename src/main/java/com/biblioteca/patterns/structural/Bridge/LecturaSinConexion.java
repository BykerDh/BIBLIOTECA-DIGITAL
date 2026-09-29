package com.biblioteca.patterns.structural.Bridge;

public class LecturaSinConexion extends SesionLectura {

    public LecturaSinConexion(MotorLectura motorLectura) {
        super(motorLectura);
    }

    @Override
    public String iniciarLectura(String titulo) {
        return "Modo sin conexion | "
                + motorLectura.abrirLibro(titulo)
                + " El contenido se lee desde el dispositivo.";
    }
}