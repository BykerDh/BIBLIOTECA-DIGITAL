package com.biblioteca.service;

import com.biblioteca.patterns.creational.singleton.ConfiguracionBiblioteca;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BibliotecaService {

    private final ConfiguracionBiblioteca configuracion = ConfiguracionBiblioteca.getInstancia();

    public String obtenerInicio() {
        return configuracion.getNombreSistema() + " funcionando correctamente";
    }

    public String obtenerInformacion() {
        return "Proyecto MVC para implementar patrones de software";
    }

    public String obtenerConfiguracion() {
        return "Sistema: " + configuracion.getNombreSistema()
                + " | Dias de prestamo: " + configuracion.getDiasPrestamo()
                + " | Maximo de prestamos: " + configuracion.getMaxPrestamosPorUsuario()
                + " | Formatos: " + configuracion.getFormatosPermitidos();
    }

    public List<String> listarLibros() {
        return List.of(
                "Libro 1: Programacion en Java - PDF",
                "Libro 2: Patrones de Software - EPUB",
                "Libro 3: Arquitectura de Software - MOBI"
        );
    }

    public List<String> listarUsuarios() {
        return List.of(
                "Usuario 1: Luis Caicedo",
                "Usuario 2: Beyker Bohorquez"
        );
    }

    public String crearPrestamo(Long usuarioId, Long libroId) {
        return "Prestamo creado para el usuario " + usuarioId
                + " con el libro " + libroId
                + ". Duracion: " + configuracion.getDiasPrestamo() + " dias.";
    }

    public String crearReserva(Long usuarioId, Long libroId) {
        return "Reserva creada para el usuario " + usuarioId
                + " con el libro " + libroId;
    }

    public String consultarSuscripcion(Long usuarioId) {
        return "El usuario " + usuarioId + " tiene una suscripcion activa";
    }

    public List<String> generarRecomendaciones(Long usuarioId) {
        return List.of(
                "Recomendacion para usuario " + usuarioId + ": Patrones de Software",
                "Recomendacion para usuario " + usuarioId + ": Spring Boot Basico"
        );
    }
    public String probarSingleton() {
    ConfiguracionBiblioteca instancia1 = ConfiguracionBiblioteca.getInstancia();
    ConfiguracionBiblioteca instancia2 = ConfiguracionBiblioteca.getInstancia();

    boolean mismaInstancia = instancia1 == instancia2;

    return "Prueba Singleton\n"
            + "Instancia 1: " + System.identityHashCode(instancia1) + "\n"
            + "Instancia 2: " + System.identityHashCode(instancia2) + "\n"
            + "Misma instancia: " + mismaInstancia;
}
}