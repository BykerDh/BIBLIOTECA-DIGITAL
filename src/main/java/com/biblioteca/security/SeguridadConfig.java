package com.biblioteca.security;

import com.biblioteca.repository.BibliotecaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SeguridadConfig {
    @Bean
    SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a -> a
                .requestMatchers("/css/**","/login","/registrarse","/error","/actuator/health/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/",true).permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?salio"));
        return http.build();
    }

    @Bean PasswordEncoder codificador() { return new BCryptPasswordEncoder(); }

    @Bean UserDetailsService usuarios(BibliotecaRepository repo) {
        return email -> repo.usuarioPorEmail(email.toLowerCase())
                .map(u -> User.withUsername(u.email()).password(u.passwordHash()).roles(u.rol())
                        .disabled(!u.activo()).build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Usuario no encontrado"));
    }
}
