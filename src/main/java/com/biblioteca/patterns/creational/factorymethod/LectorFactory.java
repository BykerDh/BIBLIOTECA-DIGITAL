package com.biblioteca.patterns.creational.factorymethod;

import org.springframework.http.MediaType;

// Factory Method: cada fabrica decide que lector de formato instanciar.
public abstract class LectorFactory {
    public interface LectorDigital {
        String formato();
        MediaType tipoContenido();
    }

    public abstract LectorDigital crearLector();

    public static class PdfLectorFactory extends LectorFactory {
        @Override public LectorDigital crearLector() {
            return new LectorDigital() {
                public String formato() { return "PDF"; }
                public MediaType tipoContenido() { return MediaType.APPLICATION_PDF; }
            };
        }
    }
    public static class EpubLectorFactory extends LectorFactory {
        @Override public LectorDigital crearLector() {
            return new LectorDigital() {
                public String formato() { return "EPUB"; }
                public MediaType tipoContenido() { return MediaType.parseMediaType("application/epub+zip"); }
            };
        }
    }
    public static class MobiLectorFactory extends LectorFactory {
        @Override public LectorDigital crearLector() {
            return new LectorDigital() {
                public String formato() { return "MOBI"; }
                public MediaType tipoContenido() { return MediaType.parseMediaType("application/x-mobipocket-ebook"); }
            };
        }
    }
    public static LectorFactory para(String formato) {
        return switch (formato.toUpperCase()) {
            case "PDF" -> new PdfLectorFactory();
            case "EPUB" -> new EpubLectorFactory();
            case "MOBI" -> new MobiLectorFactory();
            default -> throw new IllegalArgumentException("Formato no soportado");
        };
    }
}
