package com.biblioteca.patterns.structural.adapter;

import com.biblioteca.domain.Models.Archivo;
import org.springframework.core.io.Resource;

// Adapter traduce un Resource de Spring a la interfaz de entrega de la biblioteca.
public record ArchivoResourceAdapter(Archivo metadatos, Resource recurso) implements ContenidoLibro {}
