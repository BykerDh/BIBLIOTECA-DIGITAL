CREATE TABLE recomendaciones_usuario (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  libro_id BIGINT NOT NULL,
  posicion INT NOT NULL,
  generada_en DATETIME NOT NULL,
  CONSTRAINT fk_recomendacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  CONSTRAINT fk_recomendacion_libro FOREIGN KEY (libro_id) REFERENCES libros(id),
  UNIQUE KEY uq_recomendacion_usuario_libro(usuario_id,libro_id),
  INDEX ix_recomendacion_usuario_orden(usuario_id,posicion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX ix_valoracion_libro ON valoraciones(libro_id,puntos);
CREATE INDEX ix_prestamo_usuario_libro ON prestamos(usuario_id,libro_id);
CREATE INDEX ix_busqueda_usuario_fecha ON busquedas(usuario_id,creada_en,id);
