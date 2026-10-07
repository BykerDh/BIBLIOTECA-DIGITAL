CREATE TABLE usuarios (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  rol VARCHAR(15) NOT NULL DEFAULT 'USER',
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE planes (
  codigo VARCHAR(20) NOT NULL PRIMARY KEY,
  nombre VARCHAR(60) NOT NULL,
  dias_prestamo INT NOT NULL,
  max_prestamos INT NOT NULL,
  max_reservas INT NOT NULL,
  catalogo_completo BOOLEAN NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO planes (codigo,nombre,dias_prestamo,max_prestamos,max_reservas,catalogo_completo) VALUES
('BASICO','Basico',7,2,2,FALSE),
('PREMIUM','Premium',21,5,5,TRUE);

CREATE TABLE suscripciones (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL UNIQUE,
  plan_codigo VARCHAR(20) NOT NULL,
  inicia_en DATETIME NOT NULL,
  vence_en DATETIME NOT NULL,
  CONSTRAINT fk_sus_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  CONSTRAINT fk_sus_plan FOREIGN KEY (plan_codigo) REFERENCES planes(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE libros (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(250) NOT NULL,
  autor VARCHAR(180) NOT NULL,
  genero VARCHAR(100) NOT NULL,
  idioma VARCHAR(30) NOT NULL,
  descripcion TEXT NOT NULL,
  isbn VARCHAR(20) UNIQUE,
  editorial VARCHAR(150),
  paginas INT,
  acceso_minimo VARCHAR(20) NOT NULL DEFAULT 'BASICO',
  licencias INT NOT NULL DEFAULT 1,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX ix_libros_titulo (titulo),
  INDEX ix_libros_autor (autor),
  INDEX ix_libros_genero (genero)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE archivos_libro (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  libro_id BIGINT NOT NULL,
  formato VARCHAR(10) NOT NULL,
  clave_archivo CHAR(36) NOT NULL UNIQUE,
  nombre_original VARCHAR(250) NOT NULL,
  tamano BIGINT NOT NULL,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_archivo_libro FOREIGN KEY (libro_id) REFERENCES libros(id),
  UNIQUE KEY uq_libro_formato (libro_id,formato)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE colecciones (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  padre_id BIGINT NULL,
  CONSTRAINT fk_coleccion_padre FOREIGN KEY (padre_id) REFERENCES colecciones(id),
  INDEX ix_coleccion_padre (padre_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE coleccion_libros (
  coleccion_id BIGINT NOT NULL,
  libro_id BIGINT NOT NULL,
  PRIMARY KEY (coleccion_id,libro_id),
  CONSTRAINT fk_cl_coleccion FOREIGN KEY (coleccion_id) REFERENCES colecciones(id),
  CONSTRAINT fk_cl_libro FOREIGN KEY (libro_id) REFERENCES libros(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prestamos (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  libro_id BIGINT NOT NULL,
  prestado_en DATETIME NOT NULL,
  vence_en DATETIME NOT NULL,
  devuelto_en DATETIME NULL,
  CONSTRAINT fk_prestamo_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  CONSTRAINT fk_prestamo_libro FOREIGN KEY (libro_id) REFERENCES libros(id),
  INDEX ix_prestamo_libro_activo (libro_id,devuelto_en),
  INDEX ix_prestamo_usuario_activo (usuario_id,devuelto_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reservas (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  libro_id BIGINT NOT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  creada_en DATETIME NOT NULL,
  CONSTRAINT fk_reserva_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  CONSTRAINT fk_reserva_libro FOREIGN KEY (libro_id) REFERENCES libros(id),
  INDEX ix_reserva_cola (libro_id,estado,creada_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notificaciones (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  mensaje VARCHAR(500) NOT NULL,
  leida BOOLEAN NOT NULL DEFAULT FALSE,
  creada_en DATETIME NOT NULL,
  CONSTRAINT fk_notificacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  INDEX ix_notificacion_usuario (usuario_id,leida,creada_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE busquedas (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  termino VARCHAR(200) NOT NULL,
  creada_en DATETIME NOT NULL,
  CONSTRAINT fk_busqueda_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  INDEX ix_busqueda_usuario (usuario_id,creada_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE preferencias (
  usuario_id BIGINT NOT NULL PRIMARY KEY,
  genero VARCHAR(100) NOT NULL,
  CONSTRAINT fk_preferencia_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE valoraciones (
  usuario_id BIGINT NOT NULL,
  libro_id BIGINT NOT NULL,
  puntos TINYINT NOT NULL,
  creada_en DATETIME NOT NULL,
  PRIMARY KEY (usuario_id,libro_id),
  CONSTRAINT fk_valoracion_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  CONSTRAINT fk_valoracion_libro FOREIGN KEY (libro_id) REFERENCES libros(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE auditoria (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NULL,
  accion VARCHAR(70) NOT NULL,
  entidad VARCHAR(70) NOT NULL,
  entidad_id BIGINT NULL,
  fecha DATETIME NOT NULL,
  CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
  INDEX ix_auditoria_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
