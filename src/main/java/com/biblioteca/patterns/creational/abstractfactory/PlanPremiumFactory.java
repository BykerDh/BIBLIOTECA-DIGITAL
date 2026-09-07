package com.biblioteca.patterns.creational.abstractfactory;

public class PlanPremiumFactory implements BibliotecaAbstractFactory {

    @Override
    public CatalogoDigital crearCatalogo() {
        return new CatalogoPremium();
    }

    @Override
    public PoliticaPrestamo crearPoliticaPrestamo() {
        return new PoliticaPrestamoPremium();
    }
}