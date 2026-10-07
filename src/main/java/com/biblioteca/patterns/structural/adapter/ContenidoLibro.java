package com.biblioteca.patterns.structural.adapter;

import com.biblioteca.domain.Models.Archivo;
import org.springframework.core.io.Resource;

public interface ContenidoLibro {
    Archivo metadatos();
    Resource recurso();
}
