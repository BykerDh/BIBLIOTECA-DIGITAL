package com.biblioteca.controller;

import com.biblioteca.service.BibliotecaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecomendacionController {

    private final BibliotecaService bibliotecaService;

    public RecomendacionController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping("/recomendaciones/usuario/{usuarioId}")
    public List<String> generarRecomendaciones(@PathVariable Long usuarioId) {
        return bibliotecaService.generarRecomendaciones(usuarioId);
    }
}