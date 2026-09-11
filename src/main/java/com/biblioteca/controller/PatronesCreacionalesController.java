package com.biblioteca.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.biblioteca.service.BuilderService;
import com.biblioteca.service.PrototypeService;

@Controller
public class PatronesCreacionalesController {

    private final PrototypeService prototypeService;
    private final BuilderService builderService;

    public PatronesCreacionalesController(
            PrototypeService prototypeService,
            BuilderService builderService) {
        this.prototypeService = prototypeService;
        this.builderService = builderService;
    }

    @GetMapping("/app/creacionales")
    public String mostrarPagina() {
        return "patrones-creacionales";
    }

    @PostMapping("/app/creacionales/prototype")
    public String probarPrototype(
            @RequestParam(required = false) String tituloCopia,
            Model model) {

        model.addAttribute(
                "resultadoPrototype",
                prototypeService.clonarLibro(tituloCopia)
        );

        return "patrones-creacionales";
    }

    @PostMapping("/app/creacionales/builder")
    public String probarBuilder(
            @RequestParam String titulo,
            @RequestParam String autor,
            @RequestParam(required = false) String formato,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Integer paginas,
            Model model) {

        model.addAttribute(
                "resultadoBuilder",
                builderService.crearFichaLibro(
                        titulo, autor, formato, categoria, paginas
                )
        );

        return "patrones-creacionales";
    }
}