package com.biblioteca.controller;

import com.biblioteca.service.BibliotecaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LibroController {

    private final BibliotecaService bibliotecaService;

    public LibroController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping("/libros")
    public List<String> listarLibros() {
        return bibliotecaService.listarLibros();
    }
}