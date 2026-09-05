package com.biblioteca.patterns.creational.factorymethod;

public class MobiLectorFactory extends LectorFactory {

    @Override
    public LectorDigital crearLector() {
        return new LectorMobi();
    }
}