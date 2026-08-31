package com.biblioteca.controller;

import com.biblioteca.service.BibliotecaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BibliotecaController {

    private final BibliotecaService bibliotecaService;

    public BibliotecaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping("/")
    public String inicio() {
        return bibliotecaService.obtenerInicio();
    }

    @GetMapping("/info")
    public String informacion() {
        return bibliotecaService.obtenerInformacion();
    }

    @GetMapping("/configuracion")
    public String configuracion() {
        return bibliotecaService.obtenerConfiguracion();
    }
    @GetMapping("/singleton/prueba")
public String probarSingleton() {
    return bibliotecaService.probarSingleton();
}
}