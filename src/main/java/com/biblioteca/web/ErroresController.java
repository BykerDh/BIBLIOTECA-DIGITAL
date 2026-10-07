package com.biblioteca.web;

import java.io.IOException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(annotations=Controller.class)
public class ErroresController {
    @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class,DataIntegrityViolationException.class,IOException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejar(Exception error,Model model) {
        model.addAttribute("mensaje",error instanceof DataIntegrityViolationException ? "Los datos ya existen o tienen referencias relacionadas" : error.getMessage());
        return "error-app";
    }
}
