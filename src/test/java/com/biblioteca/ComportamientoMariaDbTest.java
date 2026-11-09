package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.biblioteca.repository.BibliotecaRepository;
import com.biblioteca.service.CatalogoService;
import com.biblioteca.service.PrestamoReservaService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// Opt-in: usa MariaDB real y revierte cada prueba; no reemplaza los datos del usuario.
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named="BIBLIOTECA_DB_TEST", matches="true")
class ComportamientoMariaDbTest {
    @Autowired CatalogoService catalogo;
    @Autowired BibliotecaRepository repo;
    @Autowired PrestamoReservaService circulacion;
    @Autowired WebApplicationContext context;

    private long libro() { return catalogo.crear("Prueba original","Autor","Prueba","es","Original",null,null,90,"BASICO",1); }
    private void editar(long id,long version,String titulo,int licencias) {
        catalogo.actualizar(id,version,titulo,"Autor","Prueba","es","Actualizada",null,null,100,"BASICO",licencias);
    }
    private long usuario() {
        long id=repo.crearUsuario("Prueba",UUID.randomUUID()+"@example.invalid","no-login","USER");
        repo.guardarSuscripcion(id,"BASICO",LocalDateTime.now().minusDays(1),LocalDateTime.now().plusDays(30));
        return id;
    }
    @Test void historialPersistenteDeshacerYVersionObsoleta() {
        long id=libro();editar(id,0,"Version dos",2);
        assertEquals(1,catalogo.historial(id).size());
        catalogo.deshacer(id,1);
        assertEquals("Prueba original",catalogo.libro(id).titulo());
        assertEquals(2,catalogo.libro(id).licencias());
        assertEquals(2,catalogo.versionFicha(id));assertTrue(catalogo.historial(id).isEmpty());
    }
    @Test void rechazaFormularioViejoSinCambiarLibro() {
        long id=libro();editar(id,0,"Version dos",1);
        assertThrows(IllegalStateException.class,()->editar(id,0,"No guardar",1));
        assertEquals("Version dos",catalogo.libro(id).titulo());
    }
    @Test void conservaDiezEstadosYPermiteDeshacerEnOrden() {
        long id=libro();for(int i=0;i<12;i++) editar(id,i,"Version "+i,1);
        assertEquals(10,catalogo.historial(id).size());
        catalogo.deshacer(id,12);assertEquals("Version 10",catalogo.libro(id).titulo());
        catalogo.deshacer(id,13);assertEquals("Version 9",catalogo.libro(id).titulo());
    }
    @Test void historialVacioNoModificaVersion() {
        long id=libro();
        assertThrows(IllegalStateException.class,()->catalogo.deshacer(id,0));
        assertEquals(0,catalogo.versionFicha(id));
    }
    @Test void deshacerObsoletoNoConsumeHistorial() {
        long id=libro();editar(id,0,"Version dos",1);
        assertThrows(IllegalStateException.class,()->catalogo.deshacer(id,0));
        assertEquals(1,catalogo.historial(id).size());assertEquals("Version dos",catalogo.libro(id).titulo());
    }
    @Test void iteratorIncluyeSubcoleccionSinDuplicar() {
        long id=libro(),raiz=catalogo.crearColeccion("Prueba raiz",null),hija=catalogo.crearColeccion("Prueba hija",raiz);
        catalogo.agregarLibro(raiz,id);catalogo.agregarLibro(hija,id);
        var pagina=catalogo.librosColeccion(raiz,0);
        assertEquals(1,pagina.total());assertEquals(id,pagina.libros().getFirst().id());
        assertTrue(catalogo.librosColeccion(raiz,1).libros().isEmpty());
    }
    @Test void mediadorDevuelveCancelaYAvisaSiguienteEnMariaDb() {
        long id=libro(),a=usuario(),b=usuario(),c=usuario();
        long prestamo=circulacion.prestar(a,id);
        long reserva=circulacion.reservar(b,id);circulacion.reservar(c,id);
        assertTrue(repo.notificaciones(b).isEmpty());
        circulacion.devolver(a,prestamo);assertEquals(1,repo.notificaciones(b).size());
        assertTrue(repo.notificaciones(c).isEmpty());
        circulacion.cancelarReserva(b,reserva);assertEquals(1,repo.notificaciones(c).size());
        circulacion.prestar(c,id);assertEquals("COMPLETADA",repo.reservasUsuario(c).getFirst().estado());
    }
    @Test void paginasHtmlSeguridadYFormularioDeshacer() throws Exception {
        long id=libro(),raiz=catalogo.crearColeccion("Coleccion prueba",null);catalogo.agregarLibro(raiz,id);
        long u=usuario();String email=repo.usuarioPorId(u).email();
        var mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mvc.perform(get("/admin/libros/"+id+"/editar").with(user(email).roles("USER"))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/libros/"+id+"/editar").with(user(email).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Historial de la ficha")));
        mvc.perform(get("/colecciones/"+raiz+"/libros").with(user(email))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Prueba original")));
        mvc.perform(post("/admin/libros/guardar").with(user(email).roles("ADMIN")).with(csrf())
                .param("id",Long.toString(id)).param("version","0").param("titulo","Version web")
                .param("autor","Autor").param("genero","Prueba").param("idioma","es")
                .param("descripcion","Actualizada").param("accesoMinimo","BASICO").param("licencias","1"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/admin/libros/"+id+"/editar").with(user(email).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Deshacer ultimo cambio")));
        mvc.perform(post("/admin/libros/"+id+"/deshacer").with(user(email).roles("ADMIN")).param("version","1"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/libros/"+id+"/deshacer").with(user(email).roles("ADMIN")).with(csrf()).param("version","1"))
                .andExpect(status().is3xxRedirection());
        assertEquals("Prueba original",catalogo.libro(id).titulo());
    }
    @Test void busquedaRecortaEspaciosYRechazaTerminosLargos() throws Exception {
        long id=usuario();String email=repo.usuarioPorId(id).email();
        var mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mvc.perform(get("/libros").param("q","  Prueba  ").with(user(email))).andExpect(status().isOk());
        assertEquals("Prueba",repo.ultimaBusqueda(id).orElseThrow());
        mvc.perform(get("/libros").param("q","a".repeat(201)).with(user(email))).andExpect(status().isBadRequest());
        assertEquals("Prueba",repo.ultimaBusqueda(id).orElseThrow());
    }
}
