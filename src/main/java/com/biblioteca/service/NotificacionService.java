package com.biblioteca.service;

import com.biblioteca.repository.BibliotecaRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {
    private final BibliotecaRepository repo;
    public NotificacionService(BibliotecaRepository repo) { this.repo = repo; }
    public void disponibilidad(long usuarioId, String titulo) {
        repo.notificar(usuarioId, "Ya puedes solicitar el prestamo de " + titulo);
    }
}
