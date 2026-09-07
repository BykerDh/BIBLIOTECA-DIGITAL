package com.biblioteca.patterns.creational.abstractfactory;

public class PoliticaPrestamoPremium implements PoliticaPrestamo {

    @Override
    public int getDiasPrestamo() {
        return 21;
    }

    @Override
    public int getMaximoPrestamos() {
        return 5;
    }
}