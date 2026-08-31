package com.biblioteca.controller;

import com.biblioteca.service.BibliotecaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservaController {

    private final BibliotecaService bibliotecaService;

    public ReservaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping("/reservas/crear")
    public String crearReserva(@RequestParam Long usuarioId, @RequestParam Long libroId) {
        return bibliotecaService.crearReserva(usuarioId, libroId);
    }
}