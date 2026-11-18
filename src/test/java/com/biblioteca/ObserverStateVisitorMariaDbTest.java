package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.biblioteca.patterns.behavioral.memento.HistorialFicha;
import com.biblioteca.repository.BibliotecaRepository;
import com.biblioteca.service.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

// Pruebas sobre MariaDB: cada caso revierte sus datos; los archivos usan una carpeta temporal.
@SpringBootTest(properties="app.vencimientos.habilitados=false")
@Transactional
@EnabledIfEnvironmentVariable(named="BIBLIOTECA_DB_TEST",matches="true")
class ObserverStateVisitorMariaDbTest {
    @Autowired BibliotecaRepository repo;
    @Autowired CatalogoService catalogo;
    @Autowired PrestamoReservaService circulacion;
    @Autowired VencimientoService vencimientos;
    @Autowired SuscripcionService suscripciones;
    @Autowired AutenticacionService autenticacion;
    @Autowired RecomendacionService recomendaciones;
    @Autowired HistorialFicha historial;
    @Autowired JdbcTemplate jdbc;
    @Autowired WebApplicationContext context;
    @TempDir Path temporal;

    private long libro() { return catalogo.crear("Prueba semana 14","Autor","Prueba","es","Descripcion",null,null,90,"BASICO",1); }
    private long usuario() {
        long id=repo.crearUsuario("Prueba",UUID.randomUUID()+"@example.invalid","no-login","USER");
        repo.guardarSuscripcion(id,"BASICO",LocalDateTime.now().minusDays(1),LocalDateTime.now().plusDays(30));
        return id;
    }
    private void vencer(long id) {
        jdbc.update("UPDATE prestamos SET prestado_en=DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 8 DAY),vence_en=DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 1 DAY) WHERE id=?",id);
    }
    private int auditorias(String accion,long id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM auditoria WHERE accion=? AND entidad='PRESTAMO' AND entidad_id=?",Integer.class,accion,id);
    }
    @Test void vencidoLiberaLicenciaYCupoDelPlanSinEsperarAlProgramador() {
        long u=usuario(),a=libro(),b=libro();
        long p=circulacion.prestar(u,a),q=circulacion.prestar(u,b);vencer(p);vencer(q);
        assertEquals("VENCIDO",repo.prestamo(p).estado());assertFalse(repo.tienePrestamo(u,a));
        assertEquals(0,repo.prestamosActivosUsuario(u));assertEquals(1,circulacion.disponibles(repo.libro(a)));
        assertFalse(repo.yaPrestado(u,a));assertTrue(circulacion.prestar(u,a)>p);
        assertEquals(1,repo.prestamosActivosUsuario(u));
    }
    @Test void observerVencimientoPersisteAvisoAuditoriaYTurnoUnaSolaVez() {
        long a=usuario(),b=usuario(),libro=libro();long p=circulacion.prestar(a,libro);
        circulacion.reservar(b,libro);vencer(p);
        vencimientos.procesar();
        assertEquals(1,auditorias("VENCIMIENTO",p));assertEquals(2,repo.notificaciones(a).size());
        assertEquals(1,repo.notificaciones(b).size());
        assertTrue(jdbc.queryForObject("SELECT vencimiento_procesado FROM prestamos WHERE id=?",Boolean.class,p));
        vencimientos.procesar();
        assertEquals(1,auditorias("VENCIMIENTO",p));assertEquals(1,repo.notificaciones(b).size());
        assertTrue(circulacion.prestar(b,libro)>0);
        assertEquals("COMPLETADA",repo.reservasUsuario(b).getFirst().estado());
    }
    @Test void observerDevolucionNotificaAmbasPartesYNoDuplicaAuditoria() {
        long a=usuario(),b=usuario(),libro=libro();long p=circulacion.prestar(a,libro);
        circulacion.reservar(b,libro);circulacion.devolver(a,p);
        assertEquals("DEVUELTO",repo.prestamo(p).estado());assertFalse(repo.tienePrestamo(a,libro));
        assertEquals(2,repo.notificaciones(a).size());assertEquals(1,repo.notificaciones(b).size());
        assertEquals(1,auditorias("PRESTAMO",p));assertEquals(1,auditorias("DEVOLUCION",p));
        assertThrows(IllegalArgumentException.class,()->circulacion.devolver(a,p));
        assertEquals(1,auditorias("DEVOLUCION",p));
    }
    @Test void stateImpideDevolverVencidoYLeerSinLicenciaVigente() throws Exception {
        long u=usuario(),id=libro();long p=circulacion.prestar(u,id);vencer(p);
        var local=new CatalogoService(repo,temporal.toString(),historial);
        var lectura=new LecturaService(repo,local,suscripciones);
        assertThrows(IllegalStateException.class,()->lectura.entregar(u,id,"PDF","ONLINE"));
        assertThrows(IllegalStateException.class,()->circulacion.devolver(u,p));
        assertNull(repo.prestamo(p).devueltoEn());assertEquals(0,auditorias("DEVOLUCION",p));
    }
    @Test void visitorCompruebaArchivoFisicoYNoDuplicaLibroEnSubcoleccion() throws Exception {
        var local=new CatalogoService(repo,temporal.toString(),historial);var antes=local.resumenCatalogo();
        long a=libro(),b=libro(),raiz=catalogo.crearColeccion("Raiz de prueba",null),hija=catalogo.crearColeccion("Hija de prueba",raiz);
        catalogo.agregarLibro(raiz,a);catalogo.agregarLibro(hija,a);
        String clave=UUID.randomUUID().toString();Files.writeString(temporal.resolve(clave),"%PDF-1.4\n%%EOF");
        repo.crearArchivo(a,"PDF",clave,"prueba.pdf",Files.size(temporal.resolve(clave)));
        repo.crearArchivo(b,"EPUB",UUID.randomUUID().toString(),"ausente.epub",100);
        var despues=local.resumenCatalogo();
        assertEquals(antes.libros()+2,despues.libros());assertEquals(antes.colecciones()+2,despues.colecciones());
        assertEquals(antes.licencias()+2,despues.licencias());assertEquals(antes.sinColeccion()+1,despues.sinColeccion());
        assertEquals(antes.formatos().get("PDF")+1,despues.formatos().get("PDF"));
        assertTrue(despues.sinArchivo().stream().anyMatch(l->l.id()==b));
        assertFalse(despues.sinArchivo().stream().anyMatch(l->l.id()==a));
        var contenido=new LecturaService(repo,local,suscripciones);long u=usuario();circulacion.prestar(u,a);
        var respuesta=contenido.entregar(u,a,"PDF","ONLINE");
        assertEquals("application/pdf",respuesta.getHeaders().getContentType().toString());
        assertArrayEquals(Files.readAllBytes(temporal.resolve(clave)),respuesta.getBody().getContentAsByteArray());
    }
    @Test void pantallasMuestranEstadosAvisosColaYResumenRestringidoAAdministrador() throws Exception {
        long a=usuario(),b=usuario(),c=usuario(),id=libro();long p=circulacion.prestar(a,id);
        circulacion.reservar(b,id);vencer(p);vencimientos.procesar();
        var mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        String email=repo.usuarioPorId(a).email();
        mvc.perform(get("/mi-cuenta").with(user(email))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("VENCIDO")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ha vencido")));
        mvc.perform(get("/libros/"+id).with(user(repo.usuarioPorId(c).email()))).andExpect(status().isOk())
                .andExpect(model().attribute("puedePrestar",false)).andExpect(model().attribute("puedeReservar",true));
        mvc.perform(get("/libros/"+id).with(user(repo.usuarioPorId(b).email()))).andExpect(status().isOk())
                .andExpect(model().attribute("puedePrestar",true)).andExpect(model().attribute("puedeReservar",false));
        mvc.perform(get("/admin/catalogo/resumen").with(user(email))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/catalogo/resumen").with(user(email).roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Libros sin archivo disponible")));
    }
    @Test void conservaRegistroPlanesBusquedaPreferenciasYRecomendaciones() {
        var u=autenticacion.registrar("Lector prueba",UUID.randomUUID()+"@example.invalid","Prueba temporal 2026!");
        assertEquals("BASICO",repo.suscripcion(u.id()).planCodigo());
        assertNotEquals("Prueba temporal 2026!",u.passwordHash());
        suscripciones.cambiarPlan(u.id(),"PREMIUM");assertEquals("PREMIUM",suscripciones.planVigente(u.id()).codigo());
        String genero="Genero-"+UUID.randomUUID();
        long id=catalogo.crear("Recomendacion semana 14","Autor",genero,"es","Descripcion",null,null,90,"PREMIUM",1);
        assertEquals(id,catalogo.buscar(genero,0).getFirst().id());
        recomendaciones.preferir(u.id(),genero);recomendaciones.valorar(u.id(),id,5);
        repo.guardarBusqueda(u.id(),"Recomendacion semana 14");
        assertEquals(id,recomendaciones.paraUsuario(u.id()).getFirst().id());
        assertTrue(circulacion.prestar(u.id(),id)>0);
        assertTrue(recomendaciones.paraUsuario(u.id()).stream().noneMatch(l->l.id()==id));
    }
}
