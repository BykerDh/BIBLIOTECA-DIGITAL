package com.biblioteca.service;

import org.springframework.stereotype.Service;

import com.biblioteca.patterns.creational.builder.FichaLibro;

@Service
public class BuilderService {

    public FichaLibro crearFichaLibro(String titulo, String autor,
            String formato, String categoria, Integer paginas) {

        FichaLibro.Builder builder = new FichaLibro.Builder(titulo, autor);

        if (formato != null && !formato.isBlank()) {
            builder.conFormato(formato);
        }

        if (categoria != null && !categoria.isBlank()) {
            builder.conCategoria(categoria);
        }

        if (paginas != null) {
            builder.conPaginas(paginas);
        }

        return builder.conEditorial("Biblioteca Digital")
                .conIsbn("Pendiente de registro")
                .construir();
    }
}