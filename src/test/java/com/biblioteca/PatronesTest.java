package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import com.biblioteca.domain.Models.Libro;
import com.biblioteca.domain.Models.Plan;
import com.biblioteca.patterns.creational.abstractfactory.FabricaPlan;
import com.biblioteca.patterns.creational.builder.FichaLibro;
import com.biblioteca.patterns.creational.factorymethod.LectorFactory;
import com.biblioteca.patterns.creational.prototype.PlantillaLibroDigital;
import com.biblioteca.patterns.creational.singleton.ConfiguracionBiblioteca;
import com.biblioteca.patterns.structural.bridge.SesionLectura;
import com.biblioteca.patterns.structural.adapter.ArchivoResourceAdapter;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.ColeccionNodo;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.LibroHoja;
import com.biblioteca.patterns.structural.decorator.EntregaContenido;
import com.biblioteca.domain.Models.Archivo;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.core.io.ByteArrayResource;
import org.junit.jupiter.api.Test;

class PatronesTest {
    @Test void singletonComparteInstancia() {
        assertSame(ConfiguracionBiblioteca.getInstancia(),ConfiguracionBiblioteca.getInstancia());
    }
    @Test void abstractFactoryGeneraFamiliasCompatibles() {
        Libro premium=new FichaLibro.Builder("Arquitectura","Autor").conAccesoMinimo("PREMIUM").construir();
        assertFalse(FabricaPlan.para("BASICO").crearCatalogo().permite(premium));
        assertTrue(FabricaPlan.para("PREMIUM").crearCatalogo().permite(premium));
        Plan basico=new Plan("BASICO","Basico",7,2,2,false);
        assertEquals(2,FabricaPlan.para("BASICO").crearPolitica(basico).maxPrestamos());
    }
    @Test void prototypeNoCopiaIdentidadNiIsbn() {
        Libro original=new Libro(41,"Original","Autor","Historia","es","Texto","123",null,100,"BASICO",1,true);
        Libro copia=new PlantillaLibroDigital(original).clonar("Copia");
        assertEquals(0,copia.id());
        assertNull(copia.isbn());
        assertEquals("Original",original.titulo());
        assertEquals("Copia",copia.titulo());
    }
    @Test void builderValidaDatosYFactoryMethodCreaLector() {
        assertThrows(IllegalArgumentException.class,() -> new FichaLibro.Builder("","Autor").construir());
        assertEquals("EPUB",LectorFactory.para("EPUB").crearLector().formato());
    }
    @Test void bridgeCombinaModoYFormato() {
        var lector=LectorFactory.para("PDF").crearLector();
        assertEquals("inline",SesionLectura.crear("ONLINE","PDF",lector).disposicion("libro.pdf").getType());
        assertEquals("attachment",SesionLectura.crear("DESCARGA","PDF",lector).disposicion("libro.pdf").getType());
    }
    @Test void compositeContieneLibrosYSubcolecciones() {
        ColeccionNodo raiz=new ColeccionNodo(1,"Tecnologia");
        ColeccionNodo hija=new ColeccionNodo(2,"Java");
        hija.agregar(new LibroHoja(3,"Spring"));
        raiz.agregar(hija);
        assertEquals("Spring",raiz.hijos().getFirst().hijos().getFirst().nombre());
    }
    @Test void adapterYDecoratorProtegenAntesDeAuditar() {
        var archivo=new Archivo(1,2,"PDF","uuid","libro.pdf",3);
        var adaptador=new ArchivoResourceAdapter(archivo,new ByteArrayResource(new byte[]{1,2,3}));
        AtomicInteger auditados=new AtomicInteger();
        EntregaContenido protegida=new EntregaContenido.EntregaAuditada(
                new EntregaContenido.EntregaProtegida(new EntregaContenido.EntregaBase(adaptador),
                        () -> { throw new IllegalStateException("Sin prestamo"); }),auditados::incrementAndGet);
        assertThrows(IllegalStateException.class,protegida::entregar);
        assertEquals(0,auditados.get());
        EntregaContenido permitida=new EntregaContenido.EntregaAuditada(
                new EntregaContenido.EntregaProtegida(new EntregaContenido.EntregaBase(adaptador),() -> {}),auditados::incrementAndGet);
        assertEquals("libro.pdf",permitida.entregar().metadatos().nombreOriginal());
        assertEquals(1,auditados.get());
    }
}
