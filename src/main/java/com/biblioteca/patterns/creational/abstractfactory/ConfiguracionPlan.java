package com.biblioteca.patterns.creational.abstractfactory;

public class ConfiguracionPlan {

    private final String plan;
    private final String nombreCatalogo;
    private final String descripcionCatalogo;
    private final int diasPrestamo;
    private final int maximoPrestamos;

    public ConfiguracionPlan(String plan, String nombreCatalogo,
            String descripcionCatalogo, int diasPrestamo, int maximoPrestamos) {
        this.plan = plan;
        this.nombreCatalogo = nombreCatalogo;
        this.descripcionCatalogo = descripcionCatalogo;
        this.diasPrestamo = diasPrestamo;
        this.maximoPrestamos = maximoPrestamos;
    }

    public String getPlan() {
        return plan;
    }

    public String getNombreCatalogo() {
        return nombreCatalogo;
    }

    public String getDescripcionCatalogo() {
        return descripcionCatalogo;
    }

    public int getDiasPrestamo() {
        return diasPrestamo;
    }

    public int getMaximoPrestamos() {
        return maximoPrestamos;
    }
}