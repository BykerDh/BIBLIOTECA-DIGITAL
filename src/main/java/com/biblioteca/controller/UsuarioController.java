package com.biblioteca.controller;

import com.biblioteca.service.BibliotecaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioController {

    private final BibliotecaService bibliotecaService;

    public UsuarioController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping("/usuarios")
    public List<String> listarUsuarios() {
        return bibliotecaService.listarUsuarios();
    }
}