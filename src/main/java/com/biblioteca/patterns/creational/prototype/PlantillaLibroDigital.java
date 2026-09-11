package com.biblioteca.patterns.creational.prototype;

public class PlantillaLibroDigital implements LibroDigitalPrototype {

    private String titulo;
    private String autor;
    private String formato;
    private String categoria;
    private int paginas;

    public PlantillaLibroDigital(String titulo, String autor, String formato,
            String categoria, int paginas) {
        this.titulo = titulo;
        this.autor = autor;
        this.formato = formato;
        this.categoria = categoria;
        this.paginas = paginas;
    }

    @Override
    public PlantillaLibroDigital clonar() {
        return new PlantillaLibroDigital(
                this.titulo,
                this.autor,
                this.formato,
                this.categoria,
                this.paginas
        );
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

    public void setTitulo(String titulo) {
        this.titulo = titulo;
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

    public int getPaginas() {
        return paginas;
    }
}