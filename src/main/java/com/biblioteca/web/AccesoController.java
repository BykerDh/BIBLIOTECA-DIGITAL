package com.biblioteca.web;

import com.biblioteca.service.AutenticacionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccesoController {
    private final AutenticacionService acceso;
    public AccesoController(AutenticacionService acceso) { this.acceso=acceso; }
    @GetMapping("/login") public String login() { return "login"; }
    @GetMapping("/registrarse") public String registro() { return "registro"; }
    @PostMapping("/registrarse")
    public String registrar(@RequestParam String nombre,@RequestParam String email,@RequestParam String password,Model model) {
        try {
            acceso.registrar(nombre,email,password);
            return "redirect:/login?registrado";
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            model.addAttribute("error",e instanceof DataIntegrityViolationException ? "Ese correo ya esta registrado" : e.getMessage());
            model.addAttribute("nombre",nombre);
            model.addAttribute("email",email);
            return "registro";
        }
    }
}
