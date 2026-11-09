ALTER TABLE libros ADD COLUMN version_ficha BIGINT NOT NULL DEFAULT 0;

CREATE TABLE historial_fichas (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    libro_id BIGINT NOT NULL,
    titulo VARCHAR(250) NOT NULL,
    autor VARCHAR(180) NOT NULL,
    genero VARCHAR(100) NOT NULL,
    idioma VARCHAR(30) NOT NULL,
    descripcion TEXT NOT NULL,
    editorial VARCHAR(150),
    paginas INT,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_libro FOREIGN KEY (libro_id) REFERENCES libros(id),
    INDEX ix_historial_libro (libro_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
