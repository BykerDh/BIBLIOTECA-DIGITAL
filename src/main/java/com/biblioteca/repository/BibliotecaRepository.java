package com.biblioteca.repository;

import com.biblioteca.domain.Models.*;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class BibliotecaRepository {
    private final JdbcTemplate jdbc;

    public BibliotecaRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final RowMapper<Usuario> USUARIO = (r,n) -> new Usuario(r.getLong("id"),r.getString("nombre"),r.getString("email"),r.getString("password_hash"),r.getString("rol"),r.getBoolean("activo"));
    private static final RowMapper<Plan> PLAN = (r,n) -> new Plan(r.getString("codigo"),r.getString("nombre"),r.getInt("dias_prestamo"),r.getInt("max_prestamos"),r.getInt("max_reservas"),r.getBoolean("catalogo_completo"));
    private static final RowMapper<Suscripcion> SUSCRIPCION = (r,n) -> new Suscripcion(r.getLong("id"),r.getLong("usuario_id"),r.getString("plan_codigo"),r.getTimestamp("inicia_en").toLocalDateTime(),r.getTimestamp("vence_en").toLocalDateTime());
    private static final RowMapper<Libro> LIBRO = (r,n) -> new Libro(r.getLong("id"),r.getString("titulo"),r.getString("autor"),r.getString("genero"),r.getString("idioma"),r.getString("descripcion"),r.getString("isbn"),r.getString("editorial"),(Integer)r.getObject("paginas"),r.getString("acceso_minimo"),r.getInt("licencias"),r.getBoolean("activo"));
    private static final RowMapper<Archivo> ARCHIVO = (r,n) -> new Archivo(r.getLong("id"),r.getLong("libro_id"),r.getString("formato"),r.getString("clave_archivo"),r.getString("nombre_original"),r.getLong("tamano"));
    private static final RowMapper<Prestamo> PRESTAMO = (r,n) -> new Prestamo(r.getLong("id"),r.getLong("usuario_id"),r.getLong("libro_id"),r.getTimestamp("prestado_en").toLocalDateTime(),r.getTimestamp("vence_en").toLocalDateTime(),r.getTimestamp("devuelto_en") == null ? null : r.getTimestamp("devuelto_en").toLocalDateTime());
    private static final RowMapper<Reserva> RESERVA = (r,n) -> new Reserva(r.getLong("id"),r.getLong("usuario_id"),r.getLong("libro_id"),r.getString("estado"),r.getTimestamp("creada_en").toLocalDateTime());
    private static final RowMapper<Aviso> AVISO = (r,n) -> new Aviso(r.getLong("id"),r.getString("mensaje"),r.getBoolean("leida"),r.getTimestamp("creada_en").toLocalDateTime());
    private static final RowMapper<Coleccion> COLECCION = (r,n) -> new Coleccion(r.getLong("id"),r.getString("nombre"),(Long)r.getObject("padre_id"));

    private long insert(String sql, Object... args) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i=0; i<args.length; i++) statement.setObject(i+1, args[i]);
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }

    public Optional<Usuario> usuarioPorEmail(String email) {
        return jdbc.query("SELECT * FROM usuarios WHERE email=?", USUARIO, email).stream().findFirst();
    }
    public Usuario usuarioPorId(long id) {
        return jdbc.query("SELECT * FROM usuarios WHERE id=?", USUARIO, id).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
    public void bloquearUsuario(long id) {
        if (jdbc.queryForList("SELECT id FROM usuarios WHERE id=? AND activo=TRUE FOR UPDATE",Long.class,id).isEmpty()) throw new IllegalArgumentException("Usuario no encontrado");
    }
    public long crearUsuario(String nombre,String email,String hash,String rol) {
        return insert("INSERT INTO usuarios(nombre,email,password_hash,rol) VALUES(?,?,?,?)", nombre,email,hash,rol);
    }
    public List<Plan> planes() { return jdbc.query("SELECT * FROM planes ORDER BY codigo", PLAN); }
    public Plan plan(String codigo) {
        return jdbc.query("SELECT * FROM planes WHERE codigo=?", PLAN, codigo).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Plan desconocido"));
    }
    public Suscripcion suscripcion(long usuarioId) {
        return jdbc.query("SELECT * FROM suscripciones WHERE usuario_id=?", SUSCRIPCION, usuarioId).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Suscripcion no encontrada"));
    }
    public void guardarSuscripcion(long usuarioId,String codigo,LocalDateTime inicio,LocalDateTime fin) {
        jdbc.update("INSERT INTO suscripciones(usuario_id,plan_codigo,inicia_en,vence_en) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE plan_codigo=VALUES(plan_codigo),inicia_en=VALUES(inicia_en),vence_en=VALUES(vence_en)",usuarioId,codigo,Timestamp.valueOf(inicio),Timestamp.valueOf(fin));
    }
    public List<Libro> libros(String termino,int limite,int desplazamiento) {
        String q="%"+termino.trim().toLowerCase()+"%";
        return jdbc.query("SELECT * FROM libros WHERE activo=TRUE AND (LOWER(titulo) LIKE ? OR LOWER(autor) LIKE ? OR LOWER(genero) LIKE ?) ORDER BY titulo,id LIMIT ? OFFSET ?", LIBRO,q,q,q,limite,desplazamiento);
    }
    public int totalLibros(String termino) {
        String q="%"+termino.trim().toLowerCase()+"%";
        return jdbc.queryForObject("SELECT COUNT(*) FROM libros WHERE activo=TRUE AND (LOWER(titulo) LIKE ? OR LOWER(autor) LIKE ? OR LOWER(genero) LIKE ?)",Integer.class,q,q,q);
    }
    public Libro libro(long id) {
        return jdbc.query("SELECT * FROM libros WHERE id=? AND activo=TRUE", LIBRO,id).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));
    }
    public java.util.Map<Long,String> titulosDelUsuario(long usuarioId) {
        return jdbc.query("SELECT DISTINCT l.id,l.titulo FROM libros l WHERE EXISTS "
                + "(SELECT 1 FROM prestamos p WHERE p.libro_id=l.id AND p.usuario_id=?) OR EXISTS "
                + "(SELECT 1 FROM reservas r WHERE r.libro_id=l.id AND r.usuario_id=?)", r -> {
            java.util.Map<Long,String> titulos = new java.util.HashMap<>();
            while (r.next()) titulos.put(r.getLong("id"), r.getString("titulo"));
            return titulos;
        },usuarioId,usuarioId);
    }
    public void bloquearLibro(long id) {
        if (jdbc.queryForList("SELECT id FROM libros WHERE id=? AND activo=TRUE FOR UPDATE",Long.class,id).isEmpty()) throw new IllegalArgumentException("Libro no encontrado");
    }
    public long crearLibro(Libro b) {
        return insert("INSERT INTO libros(titulo,autor,genero,idioma,descripcion,isbn,editorial,paginas,acceso_minimo,licencias) VALUES(?,?,?,?,?,?,?,?,?,?)",b.titulo(),b.autor(),b.genero(),b.idioma(),b.descripcion(),b.isbn(),b.editorial(),b.paginas(),b.accesoMinimo(),b.licencias());
    }
    public void actualizarLibro(Libro b) {
        jdbc.update("UPDATE libros SET titulo=?,autor=?,genero=?,idioma=?,descripcion=?,isbn=?,editorial=?,paginas=?,acceso_minimo=?,licencias=? WHERE id=?",b.titulo(),b.autor(),b.genero(),b.idioma(),b.descripcion(),b.isbn(),b.editorial(),b.paginas(),b.accesoMinimo(),b.licencias(),b.id());
    }
    public void desactivarLibro(long id) { jdbc.update("UPDATE libros SET activo=FALSE WHERE id=?",id); }
    public void crearArchivo(long libroId,String formato,String clave,String nombre,long tamano) {
        jdbc.update("INSERT INTO archivos_libro(libro_id,formato,clave_archivo,nombre_original,tamano) VALUES(?,?,?,?,?)",libroId,formato,clave,nombre,tamano);
    }
    public Archivo archivo(long libroId,String formato) {
        return jdbc.query("SELECT * FROM archivos_libro WHERE libro_id=? AND formato=?",ARCHIVO,libroId,formato).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Formato no disponible"));
    }
    public List<Archivo> archivos(long libroId) { return jdbc.query("SELECT * FROM archivos_libro WHERE libro_id=? ORDER BY formato",ARCHIVO,libroId); }
    public int prestamosActivosLibro(long libroId) { return jdbc.queryForObject("SELECT COUNT(*) FROM prestamos WHERE libro_id=? AND devuelto_en IS NULL",Integer.class,libroId); }
    public int prestamosActivosUsuario(long usuarioId) { return jdbc.queryForObject("SELECT COUNT(*) FROM prestamos WHERE usuario_id=? AND devuelto_en IS NULL",Integer.class,usuarioId); }
    public boolean yaPrestado(long usuarioId,long libroId) { return jdbc.queryForObject("SELECT COUNT(*) FROM prestamos WHERE usuario_id=? AND libro_id=? AND devuelto_en IS NULL",Integer.class,usuarioId,libroId)>0; }
    public long crearPrestamo(long usuarioId,long libroId,LocalDateTime inicio,LocalDateTime fin) {
        return insert("INSERT INTO prestamos(usuario_id,libro_id,prestado_en,vence_en) VALUES(?,?,?,?)",usuarioId,libroId,Timestamp.valueOf(inicio),Timestamp.valueOf(fin));
    }
    public Prestamo prestamo(long id) {
        return jdbc.query("SELECT * FROM prestamos WHERE id=?",PRESTAMO,id).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Prestamo no encontrado"));
    }
    public List<Prestamo> prestamosUsuario(long userId) { return jdbc.query("SELECT * FROM prestamos WHERE usuario_id=? ORDER BY prestado_en DESC LIMIT 100",PRESTAMO,userId); }
    public void devolver(long id) { jdbc.update("UPDATE prestamos SET devuelto_en=? WHERE id=? AND devuelto_en IS NULL",Timestamp.valueOf(LocalDateTime.now()),id); }
    public boolean tienePrestamo(long userId,long bookId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM prestamos WHERE usuario_id=? AND libro_id=? AND devuelto_en IS NULL AND vence_en>?",Integer.class,userId,bookId,Timestamp.valueOf(LocalDateTime.now()))>0;
    }
    public int reservasPendientesUsuario(long id) { return jdbc.queryForObject("SELECT COUNT(*) FROM reservas WHERE usuario_id=? AND estado='PENDIENTE'",Integer.class,id); }
    public boolean yaReservado(long userId,long bookId) { return jdbc.queryForObject("SELECT COUNT(*) FROM reservas WHERE usuario_id=? AND libro_id=? AND estado='PENDIENTE'",Integer.class,userId,bookId)>0; }
    public long crearReserva(long userId,long bookId) { return insert("INSERT INTO reservas(usuario_id,libro_id,creada_en) VALUES(?,?,?)",userId,bookId,Timestamp.valueOf(LocalDateTime.now())); }
    public List<Reserva> reservasUsuario(long userId) { return jdbc.query("SELECT * FROM reservas WHERE usuario_id=? ORDER BY creada_en DESC LIMIT 100",RESERVA,userId); }
    public Optional<Reserva> primeraReserva(long bookId) { return jdbc.query("SELECT * FROM reservas WHERE libro_id=? AND estado='PENDIENTE' ORDER BY creada_en,id LIMIT 1",RESERVA,bookId).stream().findFirst(); }
    public void cambiarReserva(long id,String estado) { jdbc.update("UPDATE reservas SET estado=? WHERE id=?",estado,id); }
    public void cancelarReserva(long id,long userId) { jdbc.update("UPDATE reservas SET estado='CANCELADA' WHERE id=? AND usuario_id=? AND estado='PENDIENTE'",id,userId); }
    public void notificar(long userId,String mensaje) { jdbc.update("INSERT INTO notificaciones(usuario_id,mensaje,creada_en) VALUES(?,?,?)",userId,mensaje,Timestamp.valueOf(LocalDateTime.now())); }
    public List<Aviso> notificaciones(long userId) { return jdbc.query("SELECT * FROM notificaciones WHERE usuario_id=? ORDER BY creada_en DESC LIMIT 50",AVISO,userId); }
    public void marcarLeida(long id,long userId) { jdbc.update("UPDATE notificaciones SET leida=TRUE WHERE id=? AND usuario_id=?",id,userId); }
    public void guardarBusqueda(long userId,String termino) { jdbc.update("INSERT INTO busquedas(usuario_id,termino,creada_en) VALUES(?,?,?)",userId,termino,Timestamp.valueOf(LocalDateTime.now())); }
    public Optional<String> preferencia(long userId) { return jdbc.queryForList("SELECT genero FROM preferencias WHERE usuario_id=?",String.class,userId).stream().findFirst(); }
    public void guardarPreferencia(long userId,String genero) { jdbc.update("INSERT INTO preferencias(usuario_id,genero) VALUES(?,?) ON DUPLICATE KEY UPDATE genero=VALUES(genero)",userId,genero); }
    public void valorar(long userId,long bookId,int puntos) { jdbc.update("INSERT INTO valoraciones(usuario_id,libro_id,puntos,creada_en) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE puntos=VALUES(puntos),creada_en=VALUES(creada_en)",userId,bookId,puntos,Timestamp.valueOf(LocalDateTime.now())); }
    public Optional<String> generoFavorito(long userId) { return jdbc.queryForList("SELECT l.genero FROM libros l JOIN prestamos p ON p.libro_id=l.id WHERE p.usuario_id=? GROUP BY l.genero ORDER BY COUNT(*) DESC LIMIT 1",String.class,userId).stream().findFirst(); }
    public Optional<String> generoMejorValorado(long userId) { return jdbc.queryForList("SELECT l.genero FROM libros l JOIN valoraciones v ON v.libro_id=l.id WHERE v.usuario_id=? GROUP BY l.genero ORDER BY SUM(v.puntos) DESC LIMIT 1",String.class,userId).stream().findFirst(); }
    public Optional<String> ultimaBusqueda(long userId) { return jdbc.queryForList("SELECT termino FROM busquedas WHERE usuario_id=? ORDER BY creada_en DESC LIMIT 1",String.class,userId).stream().findFirst(); }
    public List<Libro> recomendar(long userId,String genero,String termino,boolean completo) {
        return jdbc.query("SELECT l.* FROM libros l WHERE l.activo=TRUE AND (? OR l.acceso_minimo='BASICO') AND NOT EXISTS (SELECT 1 FROM prestamos p WHERE p.usuario_id=? AND p.libro_id=l.id) ORDER BY (CASE WHEN l.genero=? THEN 4 ELSE 0 END + CASE WHEN LOWER(l.titulo) LIKE ? OR LOWER(l.autor) LIKE ? THEN 2 ELSE 0 END + COALESCE((SELECT AVG(v.puntos) FROM valoraciones v WHERE v.libro_id=l.id),0)) DESC,l.creado_en DESC LIMIT 12",LIBRO,completo,userId,genero,"%"+termino.toLowerCase()+"%","%"+termino.toLowerCase()+"%");
    }
    public List<Coleccion> colecciones() { return jdbc.query("SELECT * FROM colecciones ORDER BY nombre",COLECCION); }
    public long crearColeccion(String nombre,Long padreId) { return insert("INSERT INTO colecciones(nombre,padre_id) VALUES(?,?)",nombre,padreId); }
    public void agregarColeccionLibro(long collectionId,long bookId) { jdbc.update("INSERT IGNORE INTO coleccion_libros(coleccion_id,libro_id) VALUES(?,?)",collectionId,bookId); }
    public List<Long> idsLibrosColeccion(long collectionId) { return jdbc.queryForList("SELECT libro_id FROM coleccion_libros WHERE coleccion_id=?",Long.class,collectionId); }
    public void auditar(Long userId,String accion,String entidad,Long entityId) { jdbc.update("INSERT INTO auditoria(usuario_id,accion,entidad,entidad_id,fecha) VALUES(?,?,?,?,?)",userId,accion,entidad,entityId,Timestamp.valueOf(LocalDateTime.now())); }
}
