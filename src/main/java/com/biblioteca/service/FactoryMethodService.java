package com.biblioteca.service;

import com.biblioteca.patterns.creational.factorymethod.EpubLectorFactory;
import com.biblioteca.patterns.creational.factorymethod.LectorDigital;
import com.biblioteca.patterns.creational.factorymethod.LectorFactory;
import com.biblioteca.patterns.creational.factorymethod.MobiLectorFactory;
import com.biblioteca.patterns.creational.factorymethod.PdfLectorFactory;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FactoryMethodService {

    public List<String> obtenerFormatosDisponibles() {
        return List.of("PDF", "EPUB", "MOBI");
    }

    public String abrirLibro(String titulo, String formato) {
        LectorFactory factory = seleccionarFactory(formato);
        LectorDigital lector = factory.crearLector();

        return lector.abrirLibro(titulo)
                + " Lector creado mediante Factory Method: "
                + lector.getClass().getSimpleName();
    }

    private LectorFactory seleccionarFactory(String formato) {
        if (formato == null) {
            throw new IllegalArgumentException("Debe seleccionar un formato.");
        }

        return switch (formato.toUpperCase()) {
            case "PDF" -> new PdfLectorFactory();
            case "EPUB" -> new EpubLectorFactory();
            case "MOBI" -> new MobiLectorFactory();
            default -> throw new IllegalArgumentException("Formato no soportado: " + formato);
        };
    }
}