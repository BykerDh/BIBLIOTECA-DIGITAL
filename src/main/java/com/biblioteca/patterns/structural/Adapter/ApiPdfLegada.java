package com.biblioteca.patterns.structural.Adapter;



public class ApiPdfLegada {

    public InformacionLibro localizarDocumentoPdf(String textoBusqueda) {
        return new InformacionLibro(
                textoBusqueda,
                "Autor de catalogo PDF",
                "PDF",
                "Catalogo PDF legado"
        );
    }
}