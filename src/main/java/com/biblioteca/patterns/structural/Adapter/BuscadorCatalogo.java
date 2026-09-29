package com.biblioteca.patterns.structural.Adapter;

// Interfaz comun que usara la Biblioteca Digital.
public interface BuscadorCatalogo {

    InformacionLibro buscar(String titulo);
}