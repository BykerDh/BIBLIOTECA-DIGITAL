package com.biblioteca.controller;

import com.biblioteca.service.BibliotecaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrestamoController {

    private final BibliotecaService bibliotecaService;

    public PrestamoController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping("/prestamos/crear")
    public String crearPrestamo(@RequestParam Long usuarioId, @RequestParam Long libroId) {
        return bibliotecaService.crearPrestamo(usuarioId, libroId);
    }
}