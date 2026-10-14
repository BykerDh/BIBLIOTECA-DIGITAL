package com.biblioteca.service;

import com.biblioteca.patterns.creational.factorymethod.LectorFactory;
import com.biblioteca.patterns.structural.adapter.ContenidoLibro;
import com.biblioteca.patterns.structural.bridge.SesionLectura;
import com.biblioteca.patterns.structural.decorator.EntregaContenido;
import com.biblioteca.patterns.structural.proxy.EntregaLibroProxy;
import com.biblioteca.repository.BibliotecaRepository;
import java.io.IOException;
import java.util.Locale;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LecturaService {
    private final BibliotecaRepository repo;
    private final CatalogoService catalogo;
    private final SuscripcionService suscripciones;
    public LecturaService(BibliotecaRepository repo,CatalogoService catalogo,SuscripcionService suscripciones) {
        this.repo=repo; this.catalogo=catalogo; this.suscripciones=suscripciones;
    }
    @Transactional
    public ResponseEntity<Resource> entregar(long userId,long bookId,String formato,String modo) throws IOException {
        String f=formato.trim().toUpperCase(Locale.ROOT);
        String m=modo.toUpperCase(Locale.ROOT);
        LectorFactory.LectorDigital lector=LectorFactory.para(f).crearLector();
        SesionLectura sesion=SesionLectura.crear(m,f,lector);
        // El proxy pospone la consulta del archivo hasta que el decorador autoriza el acceso.
        EntregaContenido diferida=new EntregaLibroProxy(() -> catalogo.contenido(bookId,f));
        EntregaContenido entrega=new EntregaContenido.EntregaAuditada(
                new EntregaContenido.EntregaProtegida(diferida,() -> {
                    suscripciones.validarAcceso(userId,catalogo.libro(bookId));
                    if (!repo.tienePrestamo(userId,bookId)) throw new IllegalStateException("Necesitas un prestamo vigente para leer este libro");
                }),() -> repo.auditar(userId,"LECTURA_"+m,"LIBRO",bookId));
        ContenidoLibro contenido=entrega.entregar();
        return ResponseEntity.ok().contentType(sesion.tipoContenido())
                .header(HttpHeaders.CONTENT_DISPOSITION,sesion.disposicion(contenido.metadatos().nombreOriginal()).toString())
                .header(HttpHeaders.CACHE_CONTROL,"private, no-store")
                .contentLength(contenido.metadatos().tamano()).body(contenido.recurso());
    }
}
