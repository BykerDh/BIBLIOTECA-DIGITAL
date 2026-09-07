package com.biblioteca.patterns.creational.abstractfactory;

public class CatalogoBasico implements CatalogoDigital {

    @Override
    public String getNombre() {
        return "Catalogo Basico";
    }

    @Override
    public String getDescripcion() {
        return "Acceso a libros generales y recursos de consulta.";
    }
}