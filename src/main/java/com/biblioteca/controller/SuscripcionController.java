package com.biblioteca.controller;

import com.biblioteca.service.BibliotecaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SuscripcionController {

    private final BibliotecaService bibliotecaService;

    public SuscripcionController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping("/suscripciones/usuario/{usuarioId}")
    public String consultarSuscripcion(@PathVariable Long usuarioId) {
        return bibliotecaService.consultarSuscripcion(usuarioId);
    }
}
