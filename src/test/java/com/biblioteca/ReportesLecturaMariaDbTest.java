package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.biblioteca.repository.BibliotecaRepository;
import com.biblioteca.service.*;
import com.biblioteca.service.ReportesService.Seccion;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties="app.vencimientos.habilitados=false")
@Transactional
@EnabledIfEnvironmentVariable(named="BIBLIOTECA_DB_TEST",matches="true")
class ReportesLecturaMariaDbTest {
    @Autowired BibliotecaRepository repo;
    @Autowired CatalogoService catalogo;
    @Autowired ReportesService reportes;
    @Autowired PrestamoReservaService circulacion;
    @Autowired SuscripcionService suscripciones;
    @Autowired RecomendacionService recomendaciones;
    @Autowired JdbcTemplate jdbc;
    @Autowired WebApplicationContext context;
    private MockMvc mvc() {return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();}
    private long usuario() {
        long id=repo.crearUsuario("Prueba reportes",UUID.randomUUID()+"@example.invalid","no-login","USER");
        repo.guardarSuscripcion(id,"BASICO",LocalDateTime.now().minusDays(1),LocalDateTime.now().plusYears(1));return id;
    }
    private long libro(String titulo) {return catalogo.crear(titulo,"Autor","Prueba","es","Descripcion",null,null,80,"BASICO",1);}
    @Test void administradorVeCincoPestanasYDescargaCincoExcelsValidos() throws Exception {
        String email=repo.usuarioPorId(usuario()).email();var mvc=mvc();
        for(Seccion s:Seccion.values()) {
            mvc.perform(get("/admin/gestion/"+s.ruta()).with(user(email).roles("ADMIN"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Descargar Excel")));
            byte[] bytes=mvc.perform(get("/admin/gestion/"+s.ruta()+"/excel").with(user(email).roles("ADMIN")))
                    .andExpect(status().isOk()).andExpect(header().string("Cache-Control","private, no-store")).andReturn().getResponse().getContentAsByteArray();
            try(var workbook=WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
                assertEquals(s.columnas().get(0),workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
            }
        }
    }
    @Test void usuarioComunYAnonimoNoPuedenConsultarNiDescargarReportes() throws Exception {
        String email=repo.usuarioPorId(usuario()).email();var mvc=mvc();
        for(Seccion s:Seccion.values()) for(String sufijo:List.of("","/excel")) {
            mvc.perform(get("/admin/gestion/"+s.ruta()+sufijo).with(user(email))).andExpect(status().isForbidden());
            mvc.perform(get("/admin/gestion/"+s.ruta()+sufijo)).andExpect(status().is3xxRedirection());
        }
    }
    @Test void excelIncluyeMasDeUnaPaginaYMasDeUnLoteSinInterpretarFormulas() throws Exception {
        String prefijo="PruebaExcel-"+UUID.randomUUID();var filas=new ArrayList<Object[]>();
        for(int i=0;i<502;i++) filas.add(new Object[]{"=SUM(1,2) "+prefijo+" "+i,"Autor","Prueba","es","Prueba"});
        jdbc.batchUpdate("INSERT INTO libros(titulo,autor,genero,idioma,descripcion) VALUES(?,?,?,?,?)",filas);
        var pagina=reportes.consultar(Seccion.LIBROS,prefijo,0);assertEquals(502,pagina.total());assertEquals(50,pagina.filas().size());
        var salida=new ByteArrayOutputStream();reportes.exportar(Seccion.LIBROS,prefijo,salida);
        try(var libro=WorkbookFactory.create(new ByteArrayInputStream(salida.toByteArray()))) {
            var hoja=libro.getSheetAt(0);assertEquals(503,hoja.getLastRowNum());
            assertEquals(CellType.STRING,hoja.getRow(2).getCell(1).getCellType());
            assertTrue(hoja.getRow(2).getCell(1).getStringCellValue().startsWith("=SUM"));
            Set<String> ids=new HashSet<>();for(int i=2;i<=503;i++) ids.add(hoja.getRow(i).getCell(0).getStringCellValue());assertEquals(502,ids.size());
        }
    }
    @Test void prestamosYReservasIncluyenUsuariosDiferentesYEstadosReales() {
        long a=usuario(),b=usuario(),id=libro("Reporte "+UUID.randomUUID());String titulo=repo.libro(id).titulo();
        circulacion.prestar(a,id);circulacion.reservar(b,id);
        assertEquals(repo.usuarioPorId(a).email(),reportes.consultar(Seccion.PRESTAMOS,titulo,0).filas().getFirst().valores().get(2));
        assertEquals(repo.usuarioPorId(b).email(),reportes.consultar(Seccion.RESERVAS,titulo,0).filas().getFirst().valores().get(2));
        assertEquals("VIGENTE",reportes.consultar(Seccion.PRESTAMOS,titulo,0).filas().getFirst().valores().getLast());
    }
    @Test void suscripcionMuestraFechasAlUsuarioYAlAdministradorYPermiteRenovar() throws Exception {
        long id=usuario();String email=repo.usuarioPorId(id).email();suscripciones.cambiarPlan(id,"PREMIUM");
        var sus=repo.suscripcion(id);String inicio=sus.iniciaEn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        var mvc=mvc();for(String ruta:List.of("/planes","/mi-cuenta")) mvc.perform(get(ruta).with(user(email))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(inicio))).andExpect(content().string(org.hamcrest.Matchers.containsString("Fecha de terminación")));
        assertEquals(1,reportes.consultar(Seccion.SUSCRIPCIONES,email,0).total());
        assertEquals("VIGENTE",reportes.consultar(Seccion.SUSCRIPCIONES,email,0).filas().getFirst().valores().get(6));
        repo.guardarSuscripcion(id,"PREMIUM",LocalDateTime.now().minusYears(2),LocalDateTime.now().minusDays(1));
        mvc.perform(get("/planes").with(user(email))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Renovar plan")));
    }
    @Test void recomendacionesReportadasCorrespondenAlResultadoDelUsuario() {
        long id=usuario();String email=repo.usuarioPorId(id).email();var resultado=recomendaciones.paraUsuario(id);
        assertEquals(resultado.size(),reportes.consultar(Seccion.RECOMENDACIONES,email,0).total());
        for(var libro:resultado) assertTrue(reportes.consultar(Seccion.RECOMENDACIONES,email,0).filas().stream().anyMatch(f->f.valores().get(3).equals(libro.titulo())));
    }
    @Test void filtroDeTresFormatosExcluyeLibrosIncompletos() {
        String q="Formatos-"+UUID.randomUUID();long completo=libro(q+" completo"),incompleto=libro(q+" incompleto");
        for(String formato:List.of("PDF","EPUB","MOBI")) repo.crearArchivo(completo,formato,UUID.randomUUID().toString(),"prueba."+formato,100);
        repo.crearArchivo(incompleto,"EPUB",UUID.randomUUID().toString(),"prueba.epub",100);
        assertEquals(1,catalogo.total(q,true));assertEquals(completo,catalogo.buscar(q,0,true).getFirst().id());
        assertEquals(2,catalogo.total(q,false));
    }
    @Test void lectorTresFormatosValidaPrestamoYEnviaPoliticaDeSeguridad() throws Exception {
        long u=usuario(),id=libro("Lectura web prueba");String email=repo.usuarioPorId(u).email();var mvc=mvc();
        for(String f:List.of("PDF","EPUB","MOBI")) repo.crearArchivo(id,f,UUID.randomUUID().toString(),"prueba."+f,100);
        mvc.perform(get("/libros/"+id+"/leer/EPUB").with(user(email))).andExpect(status().isBadRequest());
        circulacion.prestar(u,id);
        for(String f:List.of("PDF","EPUB","MOBI")) mvc.perform(get("/libros/"+id+"/leer/"+f).with(user(email)))
                .andExpect(status().isOk()).andExpect(header().string("Content-Security-Policy",org.hamcrest.Matchers.containsString("script-src 'self'")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Descargar "+f)));
    }
}
