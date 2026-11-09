package com.biblioteca.service;

import com.biblioteca.repository.BibliotecaRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ColaReservasService {
    private final BibliotecaRepository repo;
    public ColaReservasService(BibliotecaRepository repo) { this.repo = repo; }
    public record Turno(long usuarioId, String titulo) {}

    public Optional<Turno> turnoDisponible(long libroId) {
        var libro = repo.libro(libroId);
        if (repo.prestamosActivosLibro(libroId) >= libro.licencias()) return Optional.empty();
        return repo.primeraReserva(libroId).map(r -> new Turno(r.usuarioId(), libro.titulo()));
    }
}
