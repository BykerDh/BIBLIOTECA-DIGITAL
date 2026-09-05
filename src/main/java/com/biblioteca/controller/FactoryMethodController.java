package com.biblioteca.controller;

import com.biblioteca.service.FactoryMethodService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FactoryMethodController {

    private final FactoryMethodService factoryMethodService;

    public FactoryMethodController(FactoryMethodService factoryMethodService) {
        this.factoryMethodService = factoryMethodService;
    }

    @GetMapping("/factory/abrir")
    public String abrirLibro(
            @RequestParam String titulo,
            @RequestParam String formato) {
        return factoryMethodService.abrirLibro(titulo, formato);
    }
}