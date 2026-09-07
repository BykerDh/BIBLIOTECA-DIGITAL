package com.biblioteca.patterns.creational.abstractfactory;

public class PoliticaPrestamoBasica implements PoliticaPrestamo {

    @Override
    public int getDiasPrestamo() {
        return 7;
    }

    @Override
    public int getMaximoPrestamos() {
        return 2;
    }
}