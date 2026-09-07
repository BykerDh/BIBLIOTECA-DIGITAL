package com.biblioteca.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.biblioteca.service.AbstractFactoryService;

@Controller
public class AbstractFactoryVistaController {

    private final AbstractFactoryService abstractFactoryService;

    public AbstractFactoryVistaController(AbstractFactoryService abstractFactoryService) {
        this.abstractFactoryService = abstractFactoryService;
    }

    @GetMapping("/app/abstract-factory")
    public String mostrarPagina(Model model) {
        model.addAttribute("planes", abstractFactoryService.obtenerPlanes());
        return "abstract-factory";
    }

    @PostMapping("/app/abstract-factory")
    public String procesarPlan(@RequestParam String plan, Model model) {
        model.addAttribute("planes", abstractFactoryService.obtenerPlanes());
        model.addAttribute("resultado", abstractFactoryService.crearConfiguracion(plan));
        return "abstract-factory";
    }
}