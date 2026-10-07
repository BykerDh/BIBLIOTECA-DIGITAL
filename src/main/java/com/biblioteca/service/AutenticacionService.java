package com.biblioteca.service;

import com.biblioteca.domain.Models.Usuario;
import com.biblioteca.repository.BibliotecaRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionService implements ApplicationRunner {
    private final BibliotecaRepository repo;
    private final PasswordEncoder encoder;
    @Value("${app.admin.email:}") private String adminEmail;
    @Value("${app.admin.password:}") private String adminPassword;

    public AutenticacionService(BibliotecaRepository repo,PasswordEncoder encoder) {
        this.repo=repo; this.encoder=encoder;
    }

    @Transactional
    public Usuario registrar(String nombre,String email,String password) {
        if (nombre==null || nombre.isBlank() || email==null || !email.contains("@") || password==null || password.length()<10)
            throw new IllegalArgumentException("Escribe nombre, correo valido y una contrasena de al menos 10 caracteres");
        String normalizado=email.trim().toLowerCase();
        long id=repo.crearUsuario(nombre.trim(),normalizado,encoder.encode(password),"USER");
        LocalDateTime ahora=LocalDateTime.now();
        repo.guardarSuscripcion(id,"BASICO",ahora,ahora.plusYears(1));
        return repo.usuarioPorId(id);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) return;
        if (adminPassword.length()<12) throw new IllegalStateException("LIBRARY_ADMIN_PASSWORD debe tener al menos 12 caracteres");
        String normalizado=adminEmail.trim().toLowerCase();
        if (repo.usuarioPorEmail(normalizado).isPresent()) return;
        try {
            long id=repo.crearUsuario("Administrador",normalizado,encoder.encode(adminPassword),"ADMIN");
            LocalDateTime ahora=LocalDateTime.now();
            repo.guardarSuscripcion(id,"PREMIUM",ahora,ahora.plusYears(10));
        } catch (DuplicateKeyException ignored) {
            // Otra instancia pudo crear la cuenta administradora al mismo tiempo.
        }
    }
}
