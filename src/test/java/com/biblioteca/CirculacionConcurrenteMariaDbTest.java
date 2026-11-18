package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import com.biblioteca.patterns.behavioral.observer.*;
import com.biblioteca.repository.BibliotecaRepository;
import com.biblioteca.service.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties="app.vencimientos.habilitados=false")
@EnabledIfEnvironmentVariable(named="BIBLIOTECA_DB_TEST",matches="true")
class CirculacionConcurrenteMariaDbTest {
    @Autowired BibliotecaRepository repo;
    @Autowired CatalogoService catalogo;
    @Autowired PrestamoReservaService circulacion;
    @Autowired SuscripcionService suscripciones;
    @Autowired PlatformTransactionManager manager;
    @Autowired JdbcTemplate jdbc;
    private final List<Long> usuarios=new ArrayList<>();
    private Long libro;

    private void preparar() {
        new TransactionTemplate(manager).executeWithoutResult(s -> {
            libro=catalogo.crear("Prueba concurrente "+UUID.randomUUID(),"Autor","Prueba","es","Temporal",null,null,90,"BASICO",1);
            for (int i=0;i<2;i++) {
                long id=repo.crearUsuario("Prueba concurrente",UUID.randomUUID()+"@example.invalid","no-login","USER");
                usuarios.add(id);repo.guardarSuscripcion(id,"BASICO",LocalDateTime.now().minusDays(1),LocalDateTime.now().plusDays(30));
            }
        });
    }
    @AfterEach void retirarSoloDatosCreadosPorEstaPrueba() {
        new TransactionTemplate(manager).executeWithoutResult(s -> {
            for (long id:usuarios) {
                jdbc.update("DELETE FROM auditoria WHERE usuario_id=?",id);
                jdbc.update("DELETE FROM notificaciones WHERE usuario_id=?",id);
                jdbc.update("DELETE FROM reservas WHERE usuario_id=?",id);
                jdbc.update("DELETE FROM prestamos WHERE usuario_id=?",id);
                jdbc.update("DELETE FROM suscripciones WHERE usuario_id=?",id);
                jdbc.update("DELETE FROM usuarios WHERE id=?",id);
            }
            if (libro!=null) {
                jdbc.update("DELETE FROM auditoria WHERE entidad='LIBRO' AND entidad_id=?",libro);
                jdbc.update("DELETE FROM libros WHERE id=?",libro);
            }
        });
    }
    @Test void unaLicenciaNoSePrestaDosVecesNiUnaDevolucionSePublicaDosVeces() throws Exception {
        preparar();var barrera=new CyclicBarrier(2);
        try (var executor=Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> resultados=new ArrayList<>();
            for (long id:usuarios) resultados.add(executor.submit(() -> {
                barrera.await(10,TimeUnit.SECONDS);
                try { circulacion.prestar(id,libro);return true; }
                catch (IllegalStateException sinCupo) { return false; }
            }));
            int exitos=0;for (var resultado:resultados) if (resultado.get(20,TimeUnit.SECONDS)) exitos++;
            assertEquals(1,exitos);assertEquals(1,repo.prestamosActivosLibro(libro));
            var prestamo=usuarios.stream().flatMap(id->repo.prestamosUsuario(id).stream()).findFirst().orElseThrow();
            resultados.clear();
            for (int i=0;i<2;i++) resultados.add(executor.submit(() -> {
                barrera.await(10,TimeUnit.SECONDS);
                try { circulacion.devolver(prestamo.usuarioId(),prestamo.id());return true; }
                catch (IllegalArgumentException yaDevuelto) { return false; }
            }));
            exitos=0;for (var resultado:resultados) if (resultado.get(20,TimeUnit.SECONDS)) exitos++;
            assertEquals(1,exitos);assertEquals(0,repo.prestamosActivosLibro(libro));
            assertEquals(2,repo.notificaciones(prestamo.usuarioId()).size());
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM auditoria WHERE accion='DEVOLUCION' AND entidad='PRESTAMO' AND entidad_id=?",Integer.class,prestamo.id()));
        }
    }
    @Test void fallaDeObserverReviertePrestamoAuditoriaYAvisoEnMariaDb() {
        preparar();long u=usuarios.getFirst();
        ObservadorCirculacion falla=evento -> { throw new IllegalStateException("Fallo controlado del observador"); };
        var publicador=new PublicadorCirculacion(List.of(new ObservadorAuditoria(repo),new ObservadorAvisos(repo),falla));
        var servicio=new PrestamoReservaService(repo,suscripciones,publicador);
        assertThrows(IllegalStateException.class,()->new TransactionTemplate(manager).execute(s->servicio.prestar(u,libro)));
        assertTrue(repo.prestamosUsuario(u).isEmpty());assertTrue(repo.notificaciones(u).isEmpty());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM auditoria WHERE usuario_id=?",Integer.class,u));
        assertEquals(1,circulacion.disponibles(repo.libro(libro)));
    }
}
