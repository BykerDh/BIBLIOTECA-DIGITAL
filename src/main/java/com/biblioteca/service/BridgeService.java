package com.biblioteca.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.biblioteca.patterns.structural.Bridge.LecturaEnLinea;
import com.biblioteca.patterns.structural.Bridge.LecturaSinConexion;
import com.biblioteca.patterns.structural.Bridge.MotorEpub;
import com.biblioteca.patterns.structural.Bridge.MotorLectura;
import com.biblioteca.patterns.structural.Bridge.MotorMobi;
import com.biblioteca.patterns.structural.Bridge.MotorPdf;
import com.biblioteca.patterns.structural.Bridge.SesionLectura;

@Service
public class BridgeService {

    public List<String> obtenerFormatos() {
        return List.of("PDF", "EPUB", "MOBI");
    }

    public List<String> obtenerModos() {
        return List.of("EN_LINEA", "SIN_CONEXION");
    }

    public String abrirLibro(String titulo, String formato, String modo) {
        MotorLectura motor = seleccionarMotor(formato);
        SesionLectura sesion = seleccionarModo(modo, motor);

        // Bridge permite combinar cualquier modo con cualquier formato.
        return sesion.iniciarLectura(titulo);
    }

    private MotorLectura seleccionarMotor(String formato) {
        if ("PDF".equalsIgnoreCase(formato)) {
            return new MotorPdf();
        }

        if ("EPUB".equalsIgnoreCase(formato)) {
            return new MotorEpub();
        }

        if ("MOBI".equalsIgnoreCase(formato)) {
            return new MotorMobi();
        }

        throw new IllegalArgumentException("Formato no disponible.");
    }

    private SesionLectura seleccionarModo(String modo, MotorLectura motor) {
        if ("EN_LINEA".equalsIgnoreCase(modo)) {
            return new LecturaEnLinea(motor);
        }

        if ("SIN_CONEXION".equalsIgnoreCase(modo)) {
            return new LecturaSinConexion(motor);
        }

        throw new IllegalArgumentException("Modo de lectura no disponible.");
    }
}