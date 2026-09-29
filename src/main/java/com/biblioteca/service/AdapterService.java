package com.biblioteca.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.biblioteca.patterns.structural.Adapter.AdaptadorCatalogoEpub;
import com.biblioteca.patterns.structural.Adapter.AdaptadorCatalogoPdf;
import com.biblioteca.patterns.structural.Adapter.ApiPdfLegada;
import com.biblioteca.patterns.structural.Adapter.BuscadorCatalogo;
import com.biblioteca.patterns.structural.Adapter.InformacionLibro;
import com.biblioteca.patterns.structural.Adapter.ServicioEpubExterno;

@Service
public class AdapterService {

    public List<String> obtenerFuentes() {
        return List.of("PDF_LEGADO", "EPUB_EXTERNO");
    }

    public InformacionLibro buscarLibro(String fuente, String titulo) {
        BuscadorCatalogo buscador = seleccionarAdaptador(fuente);

        // El servicio usa una sola interfaz aunque las fuentes sean distintas.
        return buscador.buscar(titulo);
    }

    private BuscadorCatalogo seleccionarAdaptador(String fuente) {
        if ("PDF_LEGADO".equalsIgnoreCase(fuente)) {
            return new AdaptadorCatalogoPdf(new ApiPdfLegada());
        }

        if ("EPUB_EXTERNO".equalsIgnoreCase(fuente)) {
            return new AdaptadorCatalogoEpub(new ServicioEpubExterno());
        }

        throw new IllegalArgumentException("Fuente no disponible.");
    }
}