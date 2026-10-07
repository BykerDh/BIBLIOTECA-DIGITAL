package com.biblioteca.patterns.creational.builder;

import com.biblioteca.domain.Models.Libro;

// Builder concentra las validaciones y opciones de una ficha de libro.
public final class FichaLibro {
    private FichaLibro() {}

    public static class Builder {
        private final String titulo;
        private final String autor;
        private String genero = "General";
        private String idioma = "es";
        private String descripcion = "";
        private String isbn;
        private String editorial;
        private Integer paginas;
        private String accesoMinimo = "BASICO";
        private int licencias = 1;

        public Builder(String titulo,String autor) { this.titulo=titulo; this.autor=autor; }
        public Builder conGenero(String v) { genero=v; return this; }
        public Builder conIdioma(String v) { idioma=v; return this; }
        public Builder conDescripcion(String v) { descripcion=v; return this; }
        public Builder conIsbn(String v) { isbn=v; return this; }
        public Builder conEditorial(String v) { editorial=v; return this; }
        public Builder conPaginas(Integer v) { paginas=v; return this; }
        public Builder conAccesoMinimo(String v) { accesoMinimo=v; return this; }
        public Builder conLicencias(int v) { licencias=v; return this; }
        public Libro construir() {
            if (titulo==null || titulo.isBlank() || autor==null || autor.isBlank()) throw new IllegalArgumentException("Titulo y autor son obligatorios");
            if (licencias<1 || paginas!=null && paginas<1) throw new IllegalArgumentException("Licencias y paginas deben ser positivas");
            if (!"BASICO".equals(accesoMinimo) && !"PREMIUM".equals(accesoMinimo)) throw new IllegalArgumentException("Plan de acceso invalido");
            return new Libro(0,titulo.trim(),autor.trim(),genero.trim(),idioma.trim(),descripcion.trim(),
                    isbn==null || isbn.isBlank()?null:isbn.trim(),editorial,paginas,accesoMinimo,licencias,true);
        }
    }
}
