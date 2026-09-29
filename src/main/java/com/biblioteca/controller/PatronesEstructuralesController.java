package com.biblioteca.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.biblioteca.service.AdapterService;
import com.biblioteca.service.BridgeService;

@Controller
public class PatronesEstructuralesController {

    private final AdapterService adapterService;
    private final BridgeService bridgeService;

    public PatronesEstructuralesController(
            AdapterService adapterService,
            BridgeService bridgeService) {
        this.adapterService = adapterService;
        this.bridgeService = bridgeService;
    }

    @GetMapping("/app/estructurales")
    public String mostrarPagina(Model model) {
        cargarOpciones(model);
        return "patrones-estructurales";
    }

    @PostMapping("/app/estructurales/adapter")
    public String probarAdapter(
            @RequestParam String fuente,
            @RequestParam String titulo,
            Model model) {

        cargarOpciones(model);
        model.addAttribute(
                "resultadoAdapter",
                adapterService.buscarLibro(fuente, titulo)
        );

        return "patrones-estructurales";
    }

    @PostMapping("/app/estructurales/bridge")
    public String probarBridge(
            @RequestParam String titulo,
            @RequestParam String formato,
            @RequestParam String modo,
            Model model) {

        cargarOpciones(model);
        model.addAttribute(
                "resultadoBridge",
                bridgeService.abrirLibro(titulo, formato, modo)
        );

        return "patrones-estructurales";
    }

    private void cargarOpciones(Model model) {
        model.addAttribute("fuentes", adapterService.obtenerFuentes());
        model.addAttribute("formatos", bridgeService.obtenerFormatos());
        model.addAttribute("modos", bridgeService.obtenerModos());
    }
}