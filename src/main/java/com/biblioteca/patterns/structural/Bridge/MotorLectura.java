package com.biblioteca.patterns.structural.Bridge;

// Implementador: representa la tecnologia de lectura por formato.
public interface MotorLectura {

    String abrirLibro(String titulo);

    String getFormato();
}