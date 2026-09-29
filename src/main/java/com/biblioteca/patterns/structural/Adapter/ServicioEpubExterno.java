package com.biblioteca.patterns.structural.Adapter;



public class ServicioEpubExterno {

    public InformacionLibro consultarLibroEpub(String consulta) {
        return new InformacionLibro(
                consulta,
                "Autor de catalogo EPUB",
                "EPUB",
                "Servicio EPUB externo"
        );
    }
}