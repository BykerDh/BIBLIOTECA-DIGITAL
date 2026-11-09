package com.biblioteca.service;

import com.biblioteca.domain.Models.*;
import com.biblioteca.patterns.behavioral.mediator.MediadorCirculacion;
import com.biblioteca.patterns.creational.abstractfactory.FabricaPlan;
import com.biblioteca.repository.BibliotecaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrestamoReservaService {
    private final BibliotecaRepository repo;
    private final SuscripcionService suscripciones;
    private final MediadorCirculacion mediador;
    public PrestamoReservaService(BibliotecaRepository repo,SuscripcionService suscripciones,MediadorCirculacion mediador) {
        this.repo=repo; this.suscripciones=suscripciones; this.mediador=mediador;
    }
    public List<Prestamo> prestamos(long userId) { return repo.prestamosUsuario(userId); }
    public List<Reserva> reservas(long userId) { return repo.reservasUsuario(userId); }
    public int disponibles(Libro libro) { return Math.max(0,libro.licencias()-repo.prestamosActivosLibro(libro.id())); }

    @Transactional
    public long prestar(long userId,long bookId) {
        repo.bloquearUsuario(userId);
        repo.bloquearLibro(bookId);
        Libro libro=repo.libro(bookId);
        suscripciones.validarAcceso(userId,libro);
        Plan plan=suscripciones.planVigente(userId);
        FabricaPlan.PoliticaPrestamo politica=suscripciones.fabrica(userId).crearPolitica(plan);
        if (repo.yaPrestado(userId,bookId)) throw new IllegalStateException("Ya tienes este libro en prestamo");
        if (repo.prestamosActivosUsuario(userId)>=politica.maxPrestamos()) throw new IllegalStateException("Alcanzaste el limite de prestamos de tu plan");
        if (disponibles(libro)<1) throw new IllegalStateException("No hay licencias disponibles; puedes reservar el libro");
        var primera=repo.primeraReserva(bookId);
        if (primera.isPresent() && primera.get().usuarioId()!=userId) throw new IllegalStateException("La licencia disponible corresponde a la primera reserva");
        LocalDateTime ahora=LocalDateTime.now();
        long id=repo.crearPrestamo(userId,bookId,ahora,ahora.plusDays(politica.diasPrestamo()));
        primera.filter(r -> r.usuarioId()==userId).ifPresent(r -> repo.cambiarReserva(r.id(),"COMPLETADA"));
        mediador.disponibilidadCambiada(bookId);
        repo.auditar(userId,"PRESTAMO","LIBRO",bookId);
        return id;
    }

    @Transactional
    public void devolver(long userId,long prestamoId) {
        Prestamo prestamo=repo.prestamo(prestamoId);
        repo.bloquearUsuario(userId);
        repo.bloquearLibro(prestamo.libroId());
        prestamo=repo.prestamo(prestamoId);
        if (prestamo.usuarioId()!=userId || prestamo.devueltoEn()!=null) throw new IllegalArgumentException("Prestamo no disponible");
        repo.devolver(prestamoId);
        long bookId=prestamo.libroId();
        mediador.disponibilidadCambiada(bookId);
        repo.auditar(userId,"DEVOLUCION","PRESTAMO",prestamoId);
    }

    @Transactional
    public long reservar(long userId,long bookId) {
        repo.bloquearUsuario(userId);
        repo.bloquearLibro(bookId);
        Libro libro=repo.libro(bookId);
        suscripciones.validarAcceso(userId,libro);
        Plan plan=suscripciones.planVigente(userId);
        FabricaPlan.PoliticaPrestamo politica=suscripciones.fabrica(userId).crearPolitica(plan);
        if (disponibles(libro)>0 && repo.primeraReserva(bookId).isEmpty()) throw new IllegalStateException("El libro esta disponible; solicita un prestamo");
        if (repo.yaReservado(userId,bookId) || repo.yaPrestado(userId,bookId)) throw new IllegalStateException("Ya tienes una reserva o prestamo de este libro");
        if (repo.reservasPendientesUsuario(userId)>=politica.maxReservas()) throw new IllegalStateException("Alcanzaste el limite de reservas");
        long id=repo.crearReserva(userId,bookId);
        repo.auditar(userId,"RESERVA","LIBRO",bookId);
        return id;
    }

    @Transactional
    public void cancelarReserva(long userId,long reservaId) {
        repo.bloquearUsuario(userId);
        Reserva reserva=repo.reservasUsuario(userId).stream().filter(r -> r.id()==reservaId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        repo.bloquearLibro(reserva.libroId());
        if (!"PENDIENTE".equals(reserva.estado())) throw new IllegalStateException("La reserva ya no esta pendiente");
        repo.cancelarReserva(reservaId,userId);
        mediador.disponibilidadCambiada(reserva.libroId());
    }
}
