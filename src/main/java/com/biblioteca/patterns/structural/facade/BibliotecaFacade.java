package com.biblioteca.patterns.structural.facade;

import com.biblioteca.domain.Models.*;
import com.biblioteca.service.*;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

// Facade ofrece a los controladores operaciones completas de biblioteca.
@Service
public class BibliotecaFacade {
    private final CatalogoService catalogo;
    private final PrestamoReservaService circulacion;
    private final SuscripcionService suscripciones;
    private final RecomendacionService recomendaciones;
    private final LecturaService lectura;
    public BibliotecaFacade(CatalogoService catalogo,PrestamoReservaService circulacion,
                            SuscripcionService suscripciones,RecomendacionService recomendaciones,LecturaService lectura) {
        this.catalogo=catalogo; this.circulacion=circulacion; this.suscripciones=suscripciones;
        this.recomendaciones=recomendaciones; this.lectura=lectura;
    }
    public List<Libro> buscar(String q,int pagina) { return catalogo.buscar(q,pagina); }
    public int total(String q) { return catalogo.total(q); }
    public Libro libro(long id) { return catalogo.libro(id); }
    public List<Archivo> archivos(long id) { return catalogo.archivos(id); }
    public int disponibles(Libro libro) { return circulacion.disponibles(libro); }
    public long prestar(long userId,long bookId) { return circulacion.prestar(userId,bookId); }
    public void devolver(long userId,long loanId) { circulacion.devolver(userId,loanId); }
    public long reservar(long userId,long bookId) { return circulacion.reservar(userId,bookId); }
    public void cancelarReserva(long userId,long reservationId) { circulacion.cancelarReserva(userId,reservationId); }
    public List<Prestamo> prestamos(long userId) { return circulacion.prestamos(userId); }
    public List<Reserva> reservas(long userId) { return circulacion.reservas(userId); }
    public List<Libro> recomendaciones(long userId) { return recomendaciones.paraUsuario(userId); }
    public Suscripcion suscripcion(long userId) { return suscripciones.actual(userId); }
    public List<Plan> planes() { return suscripciones.planes(); }
    public void cambiarPlan(long userId,String codigo) { suscripciones.cambiarPlan(userId,codigo); }
    public ResponseEntity<Resource> leer(long userId,long bookId,String formato,String modo) throws IOException {
        return lectura.entregar(userId,bookId,formato,modo);
    }
}
