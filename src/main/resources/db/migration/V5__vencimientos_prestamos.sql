ALTER TABLE prestamos ADD COLUMN vencimiento_procesado BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX ix_prestamo_vencimiento ON prestamos(vencimiento_procesado,devuelto_en,vence_en);
CREATE INDEX ix_prestamo_cupo_libro ON prestamos(libro_id,devuelto_en,vence_en);
CREATE INDEX ix_prestamo_cupo_usuario ON prestamos(usuario_id,devuelto_en,vence_en);
