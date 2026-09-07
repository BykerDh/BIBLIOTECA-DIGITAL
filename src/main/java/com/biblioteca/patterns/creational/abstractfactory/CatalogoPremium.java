package com.biblioteca.patterns.creational.abstractfactory;

public class CatalogoPremium implements CatalogoDigital {

    @Override
    public String getNombre() {
        return "Catalogo Premium";
    }

    @Override
    public String getDescripcion() {
        return "Acceso a catalogo completo, novedades y libros especializados.";
    }
}