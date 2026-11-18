package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.biblioteca.domain.Models.*;
import com.biblioteca.patterns.behavioral.mediator.MediadorCirculacion;
import com.biblioteca.patterns.behavioral.observer.*;
import com.biblioteca.patterns.behavioral.state.CicloPrestamo;
import com.biblioteca.patterns.behavioral.visitor.InformeCatalogoVisitor;
import com.biblioteca.patterns.behavioral.visitor.VisitanteCatalogo;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.*;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;

class ObserverStateVisitorTest {
    private final LocalDateTime ahora=LocalDateTime.of(2026,11,16,12,0);
    private Prestamo prestamo(LocalDateTime vence,LocalDateTime devuelto) {
        return new Prestamo(1,2,3,ahora.minusDays(7),vence,devuelto);
    }
    private Libro libro(long id,int licencias) {
        return new Libro(id,"Libro "+id,"Autor","Genero","es","Descripcion",null,null,100,"BASICO",licencias,true);
    }
    @Test void stateVigenteCambiaADevueltoYBloqueaSegundaDevolucion() {
        var ciclo=new CicloPrestamo(prestamo(ahora.plusDays(1),null),ahora);
        assertEquals("VIGENTE",ciclo.nombre());assertTrue(ciclo.permiteLectura());
        assertTrue(ciclo.permiteDevolucion());assertEquals(ahora,ciclo.devolver().devueltoEn());
        assertEquals("DEVUELTO",ciclo.nombre());assertFalse(ciclo.permiteLectura());
        assertFalse(ciclo.permiteDevolucion());assertThrows(IllegalStateException.class,ciclo::devolver);
    }
    @Test void stateVenceExactamenteEnFechaLimite() {
        var ciclo=new CicloPrestamo(prestamo(ahora,null),ahora);
        assertEquals("VENCIDO",ciclo.nombre());assertFalse(ciclo.permiteLectura());
        assertFalse(ciclo.permiteDevolucion());assertThrows(IllegalStateException.class,ciclo::devolver);
        assertTrue(new CicloPrestamo(prestamo(ahora.plusNanos(1),null),ahora).permiteLectura());
    }
    @Test void stateConservaDevueltoAunqueLaFechaDeVencimientoPase() {
        var ciclo=new CicloPrestamo(prestamo(ahora.minusDays(1),ahora.minusDays(2)),ahora);
        assertEquals("DEVUELTO",ciclo.nombre());assertFalse(ciclo.permiteLectura());
    }
    @Test void stateNoAutorizaPrestamoQueAunNoComienza() {
        var p=new Prestamo(1,2,3,ahora.plusHours(1),ahora.plusDays(1),null);
        assertFalse(new CicloPrestamo(p,ahora).permiteLectura());
    }
    @Test void observerNotificaCadaSuscriptorYNoExponeListaMutable() {
        var a=mock(ObservadorCirculacion.class);var b=mock(ObservadorCirculacion.class);
        var lista=new ArrayList<ObservadorCirculacion>(List.of(a,b));
        var publicador=new PublicadorCirculacion(lista);lista.clear();
        var evento=new EventoCirculacion(EventoCirculacion.Tipo.PRESTAMO,2,3,1);
        publicador.publicar(evento);verify(a).actualizar(evento);verify(b).actualizar(evento);
        verifyNoMoreInteractions(a,b);
    }
    @Test void observerPropagaFallaParaQueLaTransaccionNoConfirmeParcialmente() {
        var fallo=mock(ObservadorCirculacion.class);var siguiente=mock(ObservadorCirculacion.class);
        doThrow(new IllegalStateException("Fallo de persistencia")).when(fallo).actualizar(any());
        var p=new PublicadorCirculacion(List.of(fallo,siguiente));
        assertThrows(IllegalStateException.class,()->p.publicar(new EventoCirculacion(EventoCirculacion.Tipo.PRESTAMO,2,3,1)));
        verifyNoInteractions(siguiente);
    }
    @Test void observerDisponibilidadDelegaEnMediadorSoloParaLibroActivo() {
        var repo=mock(com.biblioteca.repository.BibliotecaRepository.class);
        var mediador=mock(MediadorCirculacion.class);var o=new ObservadorDisponibilidad(mediador,repo);
        var evento=new EventoCirculacion(EventoCirculacion.Tipo.VENCIMIENTO,2,3,1);
        o.actualizar(evento);verifyNoInteractions(mediador);
        when(repo.libroActivo(3)).thenReturn(true);o.actualizar(evento);
        verify(mediador).disponibilidadCambiada(3);
    }
    @Test void visitorDespachaSegunTipoConcretoDelElemento() {
        var visitante=mock(VisitanteCatalogo.class);
        var coleccion=new ColeccionNodo(1,"Raiz");var hoja=new LibroHoja(1,"Libro");
        coleccion.aceptar(visitante);hoja.aceptar(visitante);
        verify(visitante).visitar(coleccion);verify(visitante).visitar(hoja);
    }
    @Test void visitorCuentaSinDuplicarSoportaCiclosYDetectaSinArchivoYSinColeccion() {
        var raiz=new ColeccionNodo(1,"Raiz");var hija=new ColeccionNodo(2,"Hija");
        raiz.agregar(hija);raiz.agregar(new LibroHoja(1,"Uno"));
        hija.agregar(raiz);hija.agregar(new LibroHoja(1,"Uno"));hija.agregar(new LibroHoja(2,"Dos"));
        hija.agregar(new LibroHoja(99,"Retirado"));
        var archivos=List.of(new Archivo(1,1,"PDF","a","a.pdf",10),
                new Archivo(2,1,"EPUB","b","b.epub",20),new Archivo(3,1,"PDF","c","c.pdf",10));
        var visitor=new InformeCatalogoVisitor(Map.of(1L,libro(1,2),2L,libro(2,3),3L,libro(3,4)),archivos);
        var resumen=visitor.analizar(List.of(raiz));
        assertEquals(2,resumen.colecciones());assertEquals(3,resumen.libros());assertEquals(9,resumen.licencias());
        assertEquals(1,resumen.sinColeccion());assertEquals(Map.of("PDF",1,"EPUB",1,"MOBI",0),resumen.formatos());
        assertEquals(Set.of(2L,3L),new HashSet<>(resumen.sinArchivo().stream().map(LibroHoja::id).toList()));
        assertEquals(resumen,visitor.analizar(List.of(raiz)));
        assertThrows(UnsupportedOperationException.class,()->resumen.formatos().put("PDF",99));
        assertThrows(UnsupportedOperationException.class,()->resumen.sinArchivo().clear());
    }
    @Test void visitorCatalogoVacioProduceResumenVacio() {
        var resumen=new InformeCatalogoVisitor(Map.of(),List.of()).analizar(List.of());
        assertEquals(0,resumen.libros());assertEquals(0,resumen.colecciones());assertEquals(0,resumen.licencias());
        assertTrue(resumen.sinArchivo().isEmpty());assertEquals(0,resumen.sinColeccion());
    }
}
