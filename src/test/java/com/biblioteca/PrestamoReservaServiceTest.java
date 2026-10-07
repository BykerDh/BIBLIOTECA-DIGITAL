package com.biblioteca;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.biblioteca.domain.Models.*;
import com.biblioteca.repository.BibliotecaRepository;
import com.biblioteca.service.PrestamoReservaService;
import com.biblioteca.service.SuscripcionService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PrestamoReservaServiceTest {
    private final BibliotecaRepository repo=mock(BibliotecaRepository.class);
    private final SuscripcionService suscripciones=mock(SuscripcionService.class);
    private final PrestamoReservaService servicio=new PrestamoReservaService(repo,suscripciones);
    private final Libro libro=new Libro(9,"Java","Autor","Programacion","es","Texto",null,null,180,"BASICO",1,true);
    private final Plan plan=new Plan("BASICO","Basico",7,2,2,false);

    @Test void prestamoConsumeLicenciaYRespetaPlan() {
        when(repo.libro(9)).thenReturn(libro);
        when(suscripciones.planVigente(4)).thenReturn(plan);
        when(suscripciones.fabrica(4)).thenReturn(com.biblioteca.patterns.creational.abstractfactory.FabricaPlan.para("BASICO"));
        when(repo.primeraReserva(9)).thenReturn(Optional.empty());
        when(repo.crearPrestamo(eq(4L),eq(9L),any(LocalDateTime.class),any(LocalDateTime.class))).thenReturn(17L);
        assertEquals(17,servicio.prestar(4,9));
        var orden=inOrder(repo);
        orden.verify(repo).bloquearUsuario(4);
        orden.verify(repo).bloquearLibro(9);
        verify(repo).crearPrestamo(eq(4L),eq(9L),any(LocalDateTime.class),any(LocalDateTime.class));
    }
    @Test void reservaSoloCuandoNoHayLicencia() {
        when(repo.libro(9)).thenReturn(libro);
        when(suscripciones.planVigente(4)).thenReturn(plan);
        when(suscripciones.fabrica(4)).thenReturn(com.biblioteca.patterns.creational.abstractfactory.FabricaPlan.para("BASICO"));
        when(repo.primeraReserva(9)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class,() -> servicio.reservar(4,9));
        verify(repo,never()).crearReserva(anyLong(),anyLong());
    }
}
