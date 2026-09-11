package com.biblioteca.patterns.creational.builder;

public class FichaLibro {

    private final String titulo;
    private final String autor;
    private final String formato;
    private final String categoria;
    private final String editorial;
    private final String isbn;
    private final Integer paginas;

    private FichaLibro(Builder builder) {
        this.titulo = builder.titulo;
        this.autor = builder.autor;
        this.formato = builder.formato;
        this.categoria = builder.categoria;
        this.editorial = builder.editorial;
        this.isbn = builder.isbn;
        this.paginas = builder.paginas;
    }

    public String getResumen() {
        return "Titulo: " + titulo
                + " | Autor: " + autor
                + " | Formato: " + formato
                + " | Categoria: " + categoria
                + " | Paginas: " + paginas;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getFormato() {
        return formato;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getEditorial() {
        return editorial;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getPaginas() {
        return paginas;
    }

    public static class Builder {

        private final String titulo;
        private final String autor;
        private String formato = "PDF";
        private String categoria = "General";
        private String editorial = "No especificada";
        private String isbn = "No especificado";
        private Integer paginas = 0;

        public Builder(String titulo, String autor) {
            this.titulo = titulo;
            this.autor = autor;
        }

        public Builder conFormato(String formato) {
            this.formato = formato;
            return this;
        }

        public Builder conCategoria(String categoria) {
            this.categoria = categoria;
            return this;
        }

        public Builder conEditorial(String editorial) {
            this.editorial = editorial;
            return this;
        }

        public Builder conIsbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Builder conPaginas(Integer paginas) {
            this.paginas = paginas;
            return this;
        }

        public FichaLibro construir() {
            return new FichaLibro(this);
        }
    }
}