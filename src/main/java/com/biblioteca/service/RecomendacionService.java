package com.biblioteca.service;

import com.biblioteca.domain.Models.Libro;
import com.biblioteca.repository.BibliotecaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecomendacionService {
    private final BibliotecaRepository repo;
    private final SuscripcionService suscripciones;
    public RecomendacionService(BibliotecaRepository repo,SuscripcionService suscripciones) {
        this.repo=repo; this.suscripciones=suscripciones;
    }
    public List<Libro> paraUsuario(long userId) {
        String genero=repo.preferencia(userId).or(() -> repo.generoMejorValorado(userId))
                .or(() -> repo.generoFavorito(userId)).orElse("");
        String busqueda=repo.ultimaBusqueda(userId).orElse("");
        return repo.recomendar(userId,genero,busqueda,suscripciones.planVigente(userId).catalogoCompleto());
    }
    @Transactional
    public void preferir(long userId,String genero) {
        if (genero==null || genero.isBlank() || genero.length()>100) throw new IllegalArgumentException("Genero invalido");
        repo.guardarPreferencia(userId,genero.trim());
    }
    @Transactional
    public void valorar(long userId,long bookId,int puntos) {
        if (puntos<1 || puntos>5) throw new IllegalArgumentException("La valoracion debe estar entre 1 y 5");
        repo.libro(bookId);
        repo.valorar(userId,bookId,puntos);
    }
}
