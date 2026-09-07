package com.biblioteca.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.biblioteca.patterns.creational.abstractfactory.ConfiguracionPlan;
import com.biblioteca.service.AbstractFactoryService;

@RestController
@RequestMapping("/abstract-factory")
public class AbstractFactoryController {

    private final AbstractFactoryService abstractFactoryService;

    public AbstractFactoryController(AbstractFactoryService abstractFactoryService) {
        this.abstractFactoryService = abstractFactoryService;
    }

    @GetMapping("/probar")
    public ConfiguracionPlan probar(
            @RequestParam(defaultValue = "BASICO") String plan) {
        return abstractFactoryService.crearConfiguracion(plan);
    }
}