package com.biblioteca.service;

import org.springframework.stereotype.Service;

import com.biblioteca.patterns.creational.prototype.PlantillaLibroDigital;

@Service
public class PrototypeService {

    public PlantillaLibroDigital clonarLibro(String tituloCopia) {
        PlantillaLibroDigital libroBase = new PlantillaLibroDigital(
                "Introduccion a Java",
                "Autor Biblioteca",
                "PDF",
                "Programacion",
                250
        );

        PlantillaLibroDigital copia = libroBase.clonar();

        if (tituloCopia != null && !tituloCopia.isBlank()) {
            copia.setTitulo(tituloCopia);
        }

        return copia;
    }
}