package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import com.biblioteca.domain.Models.Archivo;
import com.biblioteca.patterns.creational.factorymethod.LectorFactory;
import com.biblioteca.patterns.structural.adapter.ArchivoResourceAdapter;
import com.biblioteca.patterns.structural.decorator.EntregaContenido;
import com.biblioteca.patterns.structural.flyweight.FabricaFormatosCompartidos;
import com.biblioteca.patterns.structural.proxy.EntregaLibroProxy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

class FlyweightProxyTest {
    private ArchivoResourceAdapter contenido(long libroId) {
        return new ArchivoResourceAdapter(new Archivo(1,libroId,"EPUB","clave","libro.epub",3),
                new ByteArrayResource(new byte[]{1,2,3}));
    }

    @Test void factoryMethodReutilizaElMismoLectorPorFormato() {
        assertSame(LectorFactory.para("EPUB").crearLector(),LectorFactory.para(" epub ").crearLector());
        assertNotSame(LectorFactory.para("PDF").crearLector(),LectorFactory.para("EPUB").crearLector());
        assertEquals("application/epub+zip",FabricaFormatosCompartidos.obtener("EPUB").tipoContenido().toString());
    }

    @Test void cienMilSolicitudesAlPoolUsanSoloTresInstancias() {
        Set<Object> identidades=ConcurrentHashMap.newKeySet();
        String[] formatos={"PDF","EPUB","MOBI"};
        IntStream.range(0,100_000).parallel().forEach(i ->
                identidades.add(LectorFactory.para(formatos[i%3]).crearLector()));
        assertEquals(3,identidades.size());
    }

    @Test void noSeCreanEntradasParaFormatosDesconocidos() {
        assertThrows(IllegalArgumentException.class,() -> FabricaFormatosCompartidos.obtener("ZIP"));
        assertThrows(IllegalArgumentException.class,() -> FabricaFormatosCompartidos.obtener(null));
    }

    @Test void proxyNoCargaEnConstructorYReutilizaLaCarga() throws Exception {
        AtomicInteger cargas=new AtomicInteger();
        var original=contenido(1);
        var proxy=new EntregaLibroProxy(() -> { cargas.incrementAndGet(); return original; });
        assertEquals(0,cargas.get());
        assertSame(original,proxy.entregar());
        assertSame(original,proxy.entregar());
        assertEquals(1,cargas.get());
    }

    @Test void rechazoNoConsultaArchivoNiAudita() {
        AtomicInteger cargas=new AtomicInteger();
        AtomicInteger auditorias=new AtomicInteger();
        var proxy=new EntregaLibroProxy(() -> { cargas.incrementAndGet(); return contenido(1); });
        EntregaContenido entrega=new EntregaContenido.EntregaAuditada(
                new EntregaContenido.EntregaProtegida(proxy,() -> { throw new IllegalStateException("Sin prestamo"); }),
                auditorias::incrementAndGet);
        assertThrows(IllegalStateException.class,entrega::entregar);
        assertEquals(0,cargas.get());
        assertEquals(0,auditorias.get());
    }

    @Test void autorizaAntesDeCargarYAudiTaDespues() throws Exception {
        List<String> pasos=new ArrayList<>();
        var proxy=new EntregaLibroProxy(() -> { pasos.add("carga"); return contenido(1); });
        new EntregaContenido.EntregaAuditada(
                new EntregaContenido.EntregaProtegida(proxy,() -> pasos.add("permiso")),
                () -> pasos.add("auditoria")).entregar();
        assertEquals(List.of("permiso","carga","auditoria"),pasos);
    }

    @Test void falloDeCargaNoAuditaYPermiteReintentar() throws Exception {
        AtomicInteger intentos=new AtomicInteger();
        AtomicInteger auditorias=new AtomicInteger();
        var proxy=new EntregaLibroProxy(() -> {
            if (intentos.incrementAndGet()==1) throw new IOException("Archivo inaccesible");
            return contenido(1);
        });
        var entrega=new EntregaContenido.EntregaAuditada(proxy,auditorias::incrementAndGet);
        assertThrows(IOException.class,entrega::entregar);
        assertEquals(0,auditorias.get());
        entrega.entregar();
        assertEquals(2,intentos.get());
        assertEquals(1,auditorias.get());
    }

    @Test void proxiesDeSolicitudesDistintasNoCompartenArchivos() throws Exception {
        var uno=new EntregaLibroProxy(() -> contenido(1));
        var dos=new EntregaLibroProxy(() -> contenido(2));
        assertEquals(1,uno.entregar().metadatos().libroId());
        assertEquals(2,dos.entregar().metadatos().libroId());
        assertNotSame(uno.entregar(),dos.entregar());
    }

    @Test void llamadasConcurrentesAlMismoProxyCarganUnaVez() throws Exception {
        AtomicInteger cargas=new AtomicInteger();
        var proxy=new EntregaLibroProxy(() -> { cargas.incrementAndGet(); return contenido(1); });
        try (var executor=Executors.newVirtualThreadPerTaskExecutor()) {
            var tareas=IntStream.range(0,100).mapToObj(i -> executor.submit(proxy::entregar)).toList();
            var primero=tareas.getFirst().get();
            for (var tarea:tareas) assertSame(primero,tarea.get());
        }
        assertEquals(1,cargas.get());
    }
}
