package com.biblioteca.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.biblioteca.patterns.creational.abstractfactory.BibliotecaAbstractFactory;
import com.biblioteca.patterns.creational.abstractfactory.CatalogoDigital;
import com.biblioteca.patterns.creational.abstractfactory.ConfiguracionPlan;
import com.biblioteca.patterns.creational.abstractfactory.PlanBasicoFactory;
import com.biblioteca.patterns.creational.abstractfactory.PlanPremiumFactory;
import com.biblioteca.patterns.creational.abstractfactory.PoliticaPrestamo;

@Service
public class AbstractFactoryService {

    public List<String> obtenerPlanes() {
        return List.of("BASICO", "PREMIUM");
    }

    public ConfiguracionPlan crearConfiguracion(String plan) {
        String planNormalizado = plan == null ? "BASICO" : plan.trim().toUpperCase();

        BibliotecaAbstractFactory fabrica = seleccionarFabrica(planNormalizado);

        CatalogoDigital catalogo = fabrica.crearCatalogo();
        PoliticaPrestamo politica = fabrica.crearPoliticaPrestamo();

        return new ConfiguracionPlan(
                planNormalizado,
                catalogo.getNombre(),
                catalogo.getDescripcion(),
                politica.getDiasPrestamo(),
                politica.getMaximoPrestamos()
        );
    }

    private BibliotecaAbstractFactory seleccionarFabrica(String plan) {
        if ("BASICO".equals(plan)) {
            return new PlanBasicoFactory();
        }

        if ("PREMIUM".equals(plan)) {
            return new PlanPremiumFactory();
        }

        throw new IllegalArgumentException("El plan debe ser BASICO o PREMIUM.");
    }
}