package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.biblioteca.domain.Models.*;
import com.biblioteca.patterns.behavioral.iterator.RecorridoColeccion;
import com.biblioteca.patterns.behavioral.mediator.CoordinadorDisponibilidad;
import com.biblioteca.patterns.behavioral.memento.FichaEditable;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.*;
import com.biblioteca.repository.BibliotecaRepository;
import com.biblioteca.service.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ComportamientoTest {
    private Libro libro(int licencias) {
        return new Libro(9,"Original","Autor","Genero","es","Descripcion","ISBN","Editorial",80,"BASICO",licencias,true);
    }
    @Test void iteratorRecorreProfundidadSinRepetirLibrosNiCiclos() {
        var raiz=new ColeccionNodo(1,"Raiz"); var hija=new ColeccionNodo(2,"Hija");
        raiz.agregar(new LibroHoja(1,"Uno")); raiz.agregar(hija);
        hija.agregar(new LibroHoja(1,"Uno")); hija.agregar(new LibroHoja(2,"Dos")); hija.agregar(raiz);
        var recorrido=new RecorridoColeccion(List.of(raiz));
        var i=recorrido.iterator();
        assertTrue(i.hasNext()); assertTrue(i.hasNext());
        assertEquals(1,i.next().id()); assertEquals(2,i.next().id());
        assertFalse(i.hasNext()); assertThrows(NoSuchElementException.class,i::next);
        assertEquals(1,recorrido.iterator().next().id());
    }
    @Test void iteratorVacioYNoModificable() {
        var i=new RecorridoColeccion(List.of()).iterator();
        assertFalse(i.hasNext()); assertThrows(NoSuchElementException.class,i::next);
        assertThrows(UnsupportedOperationException.class,i::remove);
    }
    @Test void iteratorCursoresIndependientes() {
        var r=new RecorridoColeccion(List.of(new LibroHoja(1,"A"),new LibroHoja(2,"B")));
        var a=r.iterator();var b=r.iterator();
        a.next();a.next();assertEquals(1,b.next().id());
    }
    @Test void mementoRestauraDescripcionSinRevertirPoliticasNiIsbn() {
        var original=new FichaEditable(libro(1));var recuerdo=original.guardar();
        var actual=new FichaEditable(new Libro(9,"Nuevo","Otro","Otro","en","Cambio","OTRO ISBN","Otra",200,"PREMIUM",5,true));
        actual.restaurar(recuerdo);
        assertEquals("Original",actual.estado().titulo());assertEquals("Descripcion",actual.estado().descripcion());
        assertEquals("OTRO ISBN",actual.estado().isbn());assertEquals("PREMIUM",actual.estado().accesoMinimo());
        assertEquals(5,actual.estado().licencias());assertEquals(80,actual.estado().paginas());
    }
    @Test void mementoNoAceptaOtroLibro() {
        var ficha=new FichaEditable(libro(1));
        var otro=new FichaEditable.Recuerdo(10,"A","B","C","es","D",null,null);
        assertThrows(IllegalArgumentException.class,()->ficha.restaurar(otro));
        assertEquals("Original",ficha.estado().titulo());
    }
    @Test void mediadorAvisaSoloAlPrimerTurnoConCupo() {
        var repo=mock(BibliotecaRepository.class);
        when(repo.libro(9)).thenReturn(libro(1));
        when(repo.primeraReserva(9)).thenReturn(Optional.of(new Reserva(3,7,9,"PENDIENTE",LocalDateTime.now())));
        var mediador=new CoordinadorDisponibilidad(new ColaReservasService(repo),new NotificacionService(repo));
        mediador.disponibilidadCambiada(9);
        verify(repo).notificar(7,"Ya puedes solicitar el prestamo de Original");
    }
    @Test void mediadorNoAvisaSinLicencias() {
        var repo=mock(BibliotecaRepository.class);when(repo.libro(9)).thenReturn(libro(1));
        when(repo.prestamosActivosLibro(9)).thenReturn(1);
        new CoordinadorDisponibilidad(new ColaReservasService(repo),new NotificacionService(repo)).disponibilidadCambiada(9);
        verify(repo,never()).notificar(anyLong(),anyString());verify(repo,never()).primeraReserva(anyLong());
    }
    @Test void mediadorNoAvisaSinReservas() {
        var repo=mock(BibliotecaRepository.class);when(repo.libro(9)).thenReturn(libro(1));
        when(repo.primeraReserva(9)).thenReturn(Optional.empty());
        new CoordinadorDisponibilidad(new ColaReservasService(repo),new NotificacionService(repo)).disponibilidadCambiada(9);
        verify(repo,never()).notificar(anyLong(),anyString());
    }
    @Test void devolucionNotificaDespuesDeLiberarPrestamo() {
        var repo=mock(BibliotecaRepository.class);var mediator=mock(com.biblioteca.patterns.behavioral.mediator.MediadorCirculacion.class);
        when(repo.prestamo(2)).thenReturn(new Prestamo(2,4,9,LocalDateTime.now(),LocalDateTime.now().plusDays(7),null));
        new PrestamoReservaService(repo,mock(SuscripcionService.class),mediator).devolver(4,2);
        var orden=inOrder(repo,mediator);orden.verify(repo).devolver(2);orden.verify(mediator).disponibilidadCambiada(9);
    }
    @Test void devolucionAjenaNoAvisa() {
        var repo=mock(BibliotecaRepository.class);var mediator=mock(com.biblioteca.patterns.behavioral.mediator.MediadorCirculacion.class);
        when(repo.prestamo(2)).thenReturn(new Prestamo(2,8,9,LocalDateTime.now(),LocalDateTime.now().plusDays(7),null));
        assertThrows(IllegalArgumentException.class,()->new PrestamoReservaService(repo,mock(SuscripcionService.class),mediator).devolver(4,2));
        verifyNoInteractions(mediator);verify(repo,never()).devolver(anyLong());
    }
}
