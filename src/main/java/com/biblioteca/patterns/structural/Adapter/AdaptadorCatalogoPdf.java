package com.biblioteca.patterns.structural.Adapter;



// Adapta la API PDF antigua a la interfaz BuscadorCatalogo.
public class AdaptadorCatalogoPdf implements BuscadorCatalogo {

    private final ApiPdfLegada apiPdfLegada;

    public AdaptadorCatalogoPdf(ApiPdfLegada apiPdfLegada) {
        this.apiPdfLegada = apiPdfLegada;
    }

    @Override
    public InformacionLibro buscar(String titulo) {
        return apiPdfLegada.localizarDocumentoPdf(titulo);
    }
}