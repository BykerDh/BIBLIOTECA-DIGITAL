package com.biblioteca.web;

import java.io.IOException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.DisconnectedClientHelper;

@ControllerAdvice(annotations=Controller.class)
public class ErroresController {
    @ModelAttribute("administrador")
    public boolean administrador(Authentication auth) {
        return auth!=null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    @ExceptionHandler(IOException.class)
    public ModelAndView archivoInterrumpido(IOException error,HttpServletResponse respuesta) {
        // Al cerrar un lector el navegador cancela la descarga; no se debe escribir HTML sobre sus bytes.
        if (DisconnectedClientHelper.isClientDisconnectedException(error) || respuesta.isCommitted()) return null;
        respuesta.reset();respuesta.setStatus(HttpStatus.BAD_REQUEST.value());
        return new ModelAndView("error-app","mensaje","No se pudo entregar el archivo. Intenta abrirlo de nuevo.");
    }
    @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class,DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejar(Exception error,Model model) {
        model.addAttribute("mensaje",error instanceof DataIntegrityViolationException ? "Los datos ya existen o tienen referencias relacionadas" : error.getMessage());
        return "error-app";
    }
}
