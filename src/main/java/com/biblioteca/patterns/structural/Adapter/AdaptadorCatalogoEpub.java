package com.biblioteca.patterns.structural.Adapter;



// Adapta el servicio EPUB externo a la interfaz BuscadorCatalogo.
public class AdaptadorCatalogoEpub implements BuscadorCatalogo {

    private final ServicioEpubExterno servicioEpubExterno;

    public AdaptadorCatalogoEpub(ServicioEpubExterno servicioEpubExterno) {
        this.servicioEpubExterno = servicioEpubExterno;
    }

    @Override
    public InformacionLibro buscar(String titulo) {
        return servicioEpubExterno.consultarLibroEpub(titulo);
    }
}