package com.biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BibliotecaProyectoApplication {
    public static void main(String[] args) {
        var contexto=SpringApplication.run(BibliotecaProyectoApplication.class, args);
        // El comando de importacion termina solo despues de completar todos los runners.
        if (contexto.getEnvironment().matchesProfiles("importar-catalogo")) contexto.close();
    }
}
