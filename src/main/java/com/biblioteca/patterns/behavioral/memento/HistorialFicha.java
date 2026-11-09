package com.biblioteca.patterns.behavioral.memento;

import com.biblioteca.patterns.behavioral.memento.FichaEditable.Recuerdo;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// Caretaker persistente: conserva los ultimos diez estados por libro.
@Repository
public class HistorialFicha {
    private final JdbcTemplate jdbc;
    public HistorialFicha(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public record Entrada(long id, LocalDateTime fecha, String titulo) {}

    public long version(long libroId) {
        return jdbc.queryForObject("SELECT version_ficha FROM libros WHERE id=?", Long.class, libroId);
    }
    public void comprobarVersion(long libroId, long esperada) {
        if (version(libroId) != esperada) throw new IllegalStateException("La ficha cambio. Recarga la pagina antes de guardar o deshacer");
    }
    public void avanzarVersion(long libroId) {
        jdbc.update("UPDATE libros SET version_ficha=version_ficha+1 WHERE id=?", libroId);
    }
    public List<Entrada> entradas(long libroId) {
        return jdbc.query("SELECT id,creado_en,titulo FROM historial_fichas WHERE libro_id=? ORDER BY id DESC LIMIT 10",
                (r,n) -> new Entrada(r.getLong("id"),r.getTimestamp("creado_en").toLocalDateTime(),r.getString("titulo")), libroId);
    }
    public void guardar(Recuerdo r) {
        jdbc.update("INSERT INTO historial_fichas(libro_id,titulo,autor,genero,idioma,descripcion,editorial,paginas) VALUES(?,?,?,?,?,?,?,?)",
                r.libroId(),r.titulo(),r.autor(),r.genero(),r.idioma(),r.descripcion(),r.editorial(),r.paginas());
        var ids = jdbc.queryForList("SELECT id FROM historial_fichas WHERE libro_id=? ORDER BY id DESC",Long.class,r.libroId());
        for (int i=10;i<ids.size();i++) jdbc.update("DELETE FROM historial_fichas WHERE id=?",ids.get(i));
    }
    public Recuerdo extraerUltimo(long libroId) {
        var ids = entradas(libroId);
        if (ids.isEmpty()) throw new IllegalStateException("No hay cambios para deshacer");
        long id = ids.getFirst().id();
        Recuerdo recuerdo = jdbc.queryForObject("SELECT * FROM historial_fichas WHERE id=?",
                (r,n) -> new Recuerdo(r.getLong("libro_id"),r.getString("titulo"),r.getString("autor"),
                        r.getString("genero"),r.getString("idioma"),r.getString("descripcion"),
                        r.getString("editorial"),(Integer)r.getObject("paginas")), id);
        // Se consume dentro de la misma transaccion que restaura el libro.
        jdbc.update("DELETE FROM historial_fichas WHERE id=?",id);
        return recuerdo;
    }
}
