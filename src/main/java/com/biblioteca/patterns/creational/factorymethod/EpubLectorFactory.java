package com.biblioteca.patterns.creational.factorymethod;

public class EpubLectorFactory extends LectorFactory {

    @Override
    public LectorDigital crearLector() {
        return new LectorEpub();
    }
}