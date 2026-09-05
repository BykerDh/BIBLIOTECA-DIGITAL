package com.biblioteca.controller;

import com.biblioteca.service.FactoryMethodService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VistaController {

    private final FactoryMethodService factoryMethodService;

    public VistaController(FactoryMethodService factoryMethodService) {
        this.factoryMethodService = factoryMethodService;
    }

    @GetMapping("/app")
    public String inicio() {
        return "index";
    }

    @GetMapping("/app/factory")
    public String mostrarFactory(Model model) {
        model.addAttribute("formatos", factoryMethodService.obtenerFormatosDisponibles());
        return "factory-method";
    }

    @PostMapping("/app/factory")
    public String probarFactory(
            @RequestParam String titulo,
            @RequestParam String formato,
            Model model) {

        String resultado = factoryMethodService.abrirLibro(titulo, formato);

        model.addAttribute("formatos", factoryMethodService.obtenerFormatosDisponibles());
        model.addAttribute("resultado", resultado);
        model.addAttribute("titulo", titulo);
        model.addAttribute("formatoSeleccionado", formato);

        return "factory-method";
    }
}