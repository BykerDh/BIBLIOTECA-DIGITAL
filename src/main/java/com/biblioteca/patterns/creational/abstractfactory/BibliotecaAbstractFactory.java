package com.biblioteca.patterns.creational.abstractfactory;

public interface BibliotecaAbstractFactory {

    CatalogoDigital crearCatalogo();

    PoliticaPrestamo crearPoliticaPrestamo();
}