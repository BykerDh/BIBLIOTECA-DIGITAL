package com.biblioteca.patterns.creational.abstractfactory;

public class PlanBasicoFactory implements BibliotecaAbstractFactory {

    @Override
    public CatalogoDigital crearCatalogo() {
        return new CatalogoBasico();
    }

    @Override
    public PoliticaPrestamo crearPoliticaPrestamo() {
        return new PoliticaPrestamoBasica();
    }
}