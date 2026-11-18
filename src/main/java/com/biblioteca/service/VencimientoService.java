package com.biblioteca.service;

import com.biblioteca.patterns.behavioral.observer.EventoCirculacion;
import com.biblioteca.patterns.behavioral.observer.PublicadorCirculacion;
import com.biblioteca.patterns.behavioral.state.CicloPrestamo;
import com.biblioteca.repository.BibliotecaRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class VencimientoService {
    private static final Logger LOG = LoggerFactory.getLogger(VencimientoService.class);
    private final BibliotecaRepository repo;
    private final PublicadorCirculacion eventos;
    private final TransactionTemplate transaccion;

    public VencimientoService(BibliotecaRepository repo, PublicadorCirculacion eventos, PlatformTransactionManager manager) {
        this.repo = repo; this.eventos = eventos; this.transaccion = new TransactionTemplate(manager);
    }
    public int procesar() {
        int procesados = 0;
        for (var candidato : repo.vencidosPendientes(100)) {
            try {
                Boolean cambio = transaccion.execute(status -> {
                    // El mismo orden de bloqueos que prestar/devolver, tambien para cuentas o libros retirados.
                    repo.bloquearCirculacionHistorica(candidato.usuarioId(),candidato.libroId());
                    var prestamo = repo.prestamo(candidato.id());
                    if (!new CicloPrestamo(prestamo,LocalDateTime.now()).nombre().equals("VENCIDO")) return false;
                    if (!repo.marcarVencimiento(prestamo.id())) return false;
                    eventos.publicar(new EventoCirculacion(EventoCirculacion.Tipo.VENCIMIENTO,
                            prestamo.usuarioId(),prestamo.libroId(),prestamo.id()));
                    return true;
                });
                if (Boolean.TRUE.equals(cambio)) procesados++;
            } catch (RuntimeException error) {
                LOG.warn("No se pudo procesar el vencimiento del prestamo {}; se reintentara",candidato.id(),error);
            }
        }
        return procesados;
    }
}
