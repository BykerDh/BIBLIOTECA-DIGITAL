package com.biblioteca.patterns.creational.abstractfactory;

import com.biblioteca.domain.Models.Libro;
import com.biblioteca.domain.Models.Plan;

// Abstract Factory crea catalogo y politica compatibles con el mismo plan.
public interface FabricaPlan {
    Catalogo crearCatalogo();
    PoliticaPrestamo crearPolitica(Plan plan);

    interface Catalogo { boolean permite(Libro libro); }
    record PoliticaPrestamo(int diasPrestamo, int maxPrestamos, int maxReservas) {}

    final class PlanBasicoFactory implements FabricaPlan {
        public Catalogo crearCatalogo() { return libro -> "BASICO".equals(libro.accesoMinimo()); }
        public PoliticaPrestamo crearPolitica(Plan plan) {
            return new PoliticaPrestamo(plan.diasPrestamo(),plan.maxPrestamos(),plan.maxReservas());
        }
    }
    final class PlanPremiumFactory implements FabricaPlan {
        public Catalogo crearCatalogo() { return libro -> true; }
        public PoliticaPrestamo crearPolitica(Plan plan) {
            return new PoliticaPrestamo(plan.diasPrestamo(),plan.maxPrestamos(),plan.maxReservas());
        }
    }
    static FabricaPlan para(String codigo) {
        return switch (codigo) {
            case "BASICO" -> new PlanBasicoFactory();
            case "PREMIUM" -> new PlanPremiumFactory();
            default -> throw new IllegalArgumentException("Plan desconocido");
        };
    }
}
