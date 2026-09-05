package com.biblioteca.patterns.creational.factorymethod;

public class PdfLectorFactory extends LectorFactory {

    @Override
    public LectorDigital crearLector() {
        return new LectorPdf();
    }
}