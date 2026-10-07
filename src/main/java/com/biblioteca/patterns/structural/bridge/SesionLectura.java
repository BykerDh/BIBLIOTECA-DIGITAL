package com.biblioteca.patterns.structural.bridge;

import com.biblioteca.patterns.creational.factorymethod.LectorFactory.LectorDigital;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;

// Bridge separa el modo de entrega del motor de formato.
public abstract class SesionLectura {
    protected final MotorLectura motor;
    protected SesionLectura(MotorLectura motor) { this.motor=motor; }
    public abstract ContentDisposition disposicion(String nombre);
    public MediaType tipoContenido() { return motor.tipoContenido(); }

    public interface MotorLectura { MediaType tipoContenido(); }
    public record MotorPdf(LectorDigital lector) implements MotorLectura {
        public MediaType tipoContenido() { return lector.tipoContenido(); }
    }
    public record MotorEpub(LectorDigital lector) implements MotorLectura {
        public MediaType tipoContenido() { return lector.tipoContenido(); }
    }
    public record MotorMobi(LectorDigital lector) implements MotorLectura {
        public MediaType tipoContenido() { return lector.tipoContenido(); }
    }
    public static class LecturaEnLinea extends SesionLectura {
        public LecturaEnLinea(MotorLectura motor) { super(motor); }
        public ContentDisposition disposicion(String nombre) { return ContentDisposition.inline().filename(nombre).build(); }
    }
    public static class LecturaSinConexion extends SesionLectura {
        public LecturaSinConexion(MotorLectura motor) { super(motor); }
        public ContentDisposition disposicion(String nombre) { return ContentDisposition.attachment().filename(nombre).build(); }
    }
    public static SesionLectura crear(String modo,String formato,LectorDigital lector) {
        MotorLectura motor = switch (formato) {
            case "PDF" -> new MotorPdf(lector);
            case "EPUB" -> new MotorEpub(lector);
            case "MOBI" -> new MotorMobi(lector);
            default -> throw new IllegalArgumentException("Formato no soportado");
        };
        return switch (modo) {
            case "ONLINE" -> new LecturaEnLinea(motor);
            case "DESCARGA" -> new LecturaSinConexion(motor);
            default -> throw new IllegalArgumentException("Modo no soportado");
        };
    }
}
