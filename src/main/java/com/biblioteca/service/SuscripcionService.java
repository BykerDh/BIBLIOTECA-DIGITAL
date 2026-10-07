package com.biblioteca.service;

import com.biblioteca.domain.Models.*;
import com.biblioteca.patterns.creational.abstractfactory.FabricaPlan;
import com.biblioteca.repository.BibliotecaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuscripcionService {
    private final BibliotecaRepository repo;
    public SuscripcionService(BibliotecaRepository repo) { this.repo=repo; }
    public List<Plan> planes() { return repo.planes(); }
    public Suscripcion actual(long userId) { return repo.suscripcion(userId); }
    public Plan planVigente(long userId) {
        Suscripcion sus=repo.suscripcion(userId);
        if (!sus.vigente()) throw new IllegalStateException("La suscripcion no esta vigente");
        return repo.plan(sus.planCodigo());
    }
    public FabricaPlan fabrica(long userId) { return FabricaPlan.para(planVigente(userId).codigo()); }
    public void validarAcceso(long userId,Libro libro) {
        if (!fabrica(userId).crearCatalogo().permite(libro)) throw new IllegalStateException("El plan no permite acceder a este libro");
    }
    @Transactional
    public void cambiarPlan(long userId,String codigo) {
        repo.bloquearUsuario(userId);
        Plan plan=repo.plan(codigo);
        LocalDateTime ahora=LocalDateTime.now();
        repo.guardarSuscripcion(userId,plan.codigo(),ahora,ahora.plusYears(1));
        repo.auditar(userId,"CAMBIO_PLAN","SUSCRIPCION",userId);
    }
}
