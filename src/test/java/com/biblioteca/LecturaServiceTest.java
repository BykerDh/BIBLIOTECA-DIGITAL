package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.biblioteca.domain.Models.Archivo;
import com.biblioteca.domain.Models.Libro;
import com.biblioteca.patterns.creational.builder.FichaLibro;
import com.biblioteca.patterns.structural.adapter.ArchivoResourceAdapter;
import com.biblioteca.repository.BibliotecaRepository;
import com.biblioteca.service.CatalogoService;
import com.biblioteca.service.LecturaService;
import com.biblioteca.service.SuscripcionService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

class LecturaServiceTest {
    private final BibliotecaRepository repo=mock(BibliotecaRepository.class);
    private final CatalogoService catalogo=mock(CatalogoService.class);
    private final SuscripcionService suscripciones=mock(SuscripcionService.class);
    private final LecturaService service=new LecturaService(repo,catalogo,suscripciones);
    private final Libro libro=new FichaLibro.Builder("Don Quijote","Cervantes").construir();

    @Test void sinPrestamoNoSeBuscaElArchivo() throws Exception {
        when(catalogo.libro(5)).thenReturn(libro);
        when(repo.tienePrestamo(1,5)).thenReturn(false);
        assertThrows(IllegalStateException.class,() -> service.entregar(1,5,"EPUB","DESCARGA"));
        verify(catalogo,never()).contenido(anyLong(),anyString());
        verify(repo,never()).auditar(any(),anyString(),anyString(),any());
    }

    @Test void entregaAutorizadaMantieneTipoContenidoYAuditoria() throws Exception {
        when(catalogo.libro(5)).thenReturn(libro);
        when(repo.tienePrestamo(1,5)).thenReturn(true);
        var bytes=new ByteArrayResource(new byte[]{1,2,3});
        when(catalogo.contenido(5,"EPUB")).thenReturn(new ArchivoResourceAdapter(
                new Archivo(1,5,"EPUB","clave","quijote.epub",3),bytes));
        var respuesta=service.entregar(1,5,"epub","descarga");
        assertEquals(200,respuesta.getStatusCode().value());
        assertEquals("application/epub+zip",respuesta.getHeaders().getContentType().toString());
        assertEquals("attachment",respuesta.getHeaders().getContentDisposition().getType());
        assertSame(bytes,respuesta.getBody());
        var orden=inOrder(suscripciones,repo,catalogo);
        orden.verify(catalogo).libro(5);
        orden.verify(suscripciones).validarAcceso(1,libro);
        orden.verify(repo).tienePrestamo(1,5);
        orden.verify(catalogo).contenido(5,"EPUB");
        orden.verify(repo).auditar(1L,"LECTURA_DESCARGA","LIBRO",5L);
    }

    @Test void archivoNoDisponibleNoRegistraLectura() throws Exception {
        when(catalogo.libro(5)).thenReturn(libro);
        when(repo.tienePrestamo(1,5)).thenReturn(true);
        when(catalogo.contenido(5,"PDF")).thenThrow(new IOException("No disponible"));
        assertThrows(IOException.class,() -> service.entregar(1,5,"PDF","ONLINE"));
        verify(repo,never()).auditar(any(),anyString(),anyString(),any());
    }
}
