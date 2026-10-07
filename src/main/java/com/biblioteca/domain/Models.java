package com.biblioteca.domain;

import java.time.LocalDateTime;

public final class Models {
    private Models() {}

    public record Usuario(long id, String nombre, String email, String passwordHash, String rol, boolean activo) {}
    public record Plan(String codigo, String nombre, int diasPrestamo, int maxPrestamos, int maxReservas, boolean catalogoCompleto) {}
    public record Suscripcion(long id, long usuarioId, String planCodigo, LocalDateTime iniciaEn, LocalDateTime venceEn) {
        public boolean vigente() { return !iniciaEn.isAfter(LocalDateTime.now()) && venceEn.isAfter(LocalDateTime.now()); }
    }
    public record Libro(long id, String titulo, String autor, String genero, String idioma, String descripcion,
                        String isbn, String editorial, Integer paginas, String accesoMinimo, int licencias, boolean activo) {}
    public record Archivo(long id, long libroId, String formato, String claveArchivo, String nombreOriginal, long tamano) {}
    public record Prestamo(long id, long usuarioId, long libroId, LocalDateTime prestadoEn,
                           LocalDateTime venceEn, LocalDateTime devueltoEn) {}
    public record Reserva(long id, long usuarioId, long libroId, String estado, LocalDateTime creadaEn) {}
    public record Aviso(long id, String mensaje, boolean leida, LocalDateTime creadaEn) {}
    public record Coleccion(long id, String nombre, Long padreId) {}
}
