package com.biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BibliotecaProyectoApplication {
    public static void main(String[] args) {
        var contexto=SpringApplication.run(BibliotecaProyectoApplication.class, args);
        // El comando de importacion termina solo despues de completar todos los runners.
        if (contexto.getEnvironment().matchesProfiles("importar-catalogo","importar-formatos")) contexto.close();
    }
}
