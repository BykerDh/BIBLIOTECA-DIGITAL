CREATE TABLE fuentes_libro (
  libro_id BIGINT NOT NULL PRIMARY KEY,
  proveedor VARCHAR(40) NOT NULL,
  identificador_externo VARCHAR(40) NOT NULL,
  fuente_url VARCHAR(500) NOT NULL,
  derechos VARCHAR(250) NOT NULL,
  sha256 CHAR(64) NOT NULL,
  importado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_fuente_libro FOREIGN KEY (libro_id) REFERENCES libros(id),
  UNIQUE KEY uq_fuente_externa (proveedor,identificador_externo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
