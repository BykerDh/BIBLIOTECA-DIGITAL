package com.biblioteca.patterns.creational.factorymethod;

import com.biblioteca.patterns.structural.flyweight.FabricaFormatosCompartidos;
import java.util.Locale;
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
            return FabricaFormatosCompartidos.obtener("PDF");
        }
    }
    public static class EpubLectorFactory extends LectorFactory {
        @Override public LectorDigital crearLector() {
            return FabricaFormatosCompartidos.obtener("EPUB");
        }
    }
    public static class MobiLectorFactory extends LectorFactory {
        @Override public LectorDigital crearLector() {
            return FabricaFormatosCompartidos.obtener("MOBI");
        }
    }
    public static LectorFactory para(String formato) {
        if (formato == null) throw new IllegalArgumentException("Formato obligatorio");
        return switch (formato.trim().toUpperCase(Locale.ROOT)) {
            case "PDF" -> new PdfLectorFactory();
            case "EPUB" -> new EpubLectorFactory();
            case "MOBI" -> new MobiLectorFactory();
            default -> throw new IllegalArgumentException("Formato no soportado");
        };
    }
}
