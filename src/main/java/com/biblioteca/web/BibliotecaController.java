package com.biblioteca.web;

import com.biblioteca.domain.Models.*;
import com.biblioteca.patterns.structural.facade.BibliotecaFacade;
import com.biblioteca.repository.BibliotecaRepository;
import com.biblioteca.service.CatalogoService;
import com.biblioteca.service.RecomendacionService;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BibliotecaController {
    private final BibliotecaFacade biblioteca;
    private final BibliotecaRepository repo;
    private final RecomendacionService recomendaciones;
    private final CatalogoService catalogo;
    public BibliotecaController(BibliotecaFacade biblioteca,BibliotecaRepository repo,
                                RecomendacionService recomendaciones,CatalogoService catalogo) {
        this.biblioteca=biblioteca; this.repo=repo; this.recomendaciones=recomendaciones; this.catalogo=catalogo;
    }
    private Usuario usuario(Principal principal) { return repo.usuarioPorEmail(principal.getName()).orElseThrow(); }
    @GetMapping("/")
    public String inicio(Principal principal,Model model) {
        Usuario u=usuario(principal);
        model.addAttribute("usuario",u);
        model.addAttribute("novedades",biblioteca.buscar("",0).stream().limit(8).toList());
        model.addAttribute("recomendados",biblioteca.recomendaciones(u.id()).stream().limit(4).toList());
        return "index";
    }
    @GetMapping("/libros")
    public String libros(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="0") int pagina,Principal principal,Model model) {
        if (q.length()>200) throw new IllegalArgumentException("La busqueda supera los 200 caracteres");
        int p=Math.max(0,pagina);
        model.addAttribute("libros",biblioteca.buscar(q,p));
        model.addAttribute("total",biblioteca.total(q));
        model.addAttribute("pagina",p);
        model.addAttribute("q",q);
        model.addAttribute("usuario",usuario(principal));
        if (!q.isBlank()) repo.guardarBusqueda(usuario(principal).id(),q.trim().substring(0,Math.min(200,q.trim().length())));
        return "libros";
    }
    @GetMapping("/libros/{id}")
    public String detalle(@PathVariable long id,Principal principal,Model model) {
        Libro libro=biblioteca.libro(id);
        model.addAttribute("libro",libro);
        model.addAttribute("archivos",biblioteca.archivos(id));
        model.addAttribute("disponibles",biblioteca.disponibles(libro));
        model.addAttribute("usuario",usuario(principal));
        return "libro";
    }
    @PostMapping("/libros/{id}/prestar")
    public String prestar(@PathVariable long id,Principal principal) {
        biblioteca.prestar(usuario(principal).id(),id);
        return "redirect:/libros/"+id+"?prestado";
    }
    @PostMapping("/libros/{id}/reservar")
    public String reservar(@PathVariable long id,Principal principal) {
        biblioteca.reservar(usuario(principal).id(),id);
        return "redirect:/libros/"+id+"?reservado";
    }
    @GetMapping("/libros/{id}/archivo/{formato}")
    public ResponseEntity<Resource> archivo(@PathVariable long id,@PathVariable String formato,
            @RequestParam(defaultValue="ONLINE") String modo,Principal principal) throws IOException {
        return biblioteca.leer(usuario(principal).id(),id,formato,modo);
    }
    @PostMapping("/libros/{id}/valorar")
    public String valorar(@PathVariable long id,@RequestParam int puntos,Principal principal) {
        recomendaciones.valorar(usuario(principal).id(),id,puntos);
        return "redirect:/libros/"+id+"?valorado";
    }
    @GetMapping("/mi-cuenta")
    public String cuenta(Principal principal,Model model) {
        Usuario u=usuario(principal);
        model.addAttribute("usuario",u);
        model.addAttribute("prestamos",biblioteca.prestamos(u.id()));
        model.addAttribute("reservas",biblioteca.reservas(u.id()));
        model.addAttribute("titulos",repo.titulosDelUsuario(u.id()));
        model.addAttribute("suscripcion",biblioteca.suscripcion(u.id()));
        model.addAttribute("avisos",repo.notificaciones(u.id()));
        return "cuenta";
    }
    @PostMapping("/prestamos/{id}/devolver")
    public String devolver(@PathVariable long id,Principal principal) {
        biblioteca.devolver(usuario(principal).id(),id);
        return "redirect:/mi-cuenta?devuelto";
    }
    @PostMapping("/reservas/{id}/cancelar")
    public String cancelar(@PathVariable long id,Principal principal) {
        biblioteca.cancelarReserva(usuario(principal).id(),id);
        return "redirect:/mi-cuenta?cancelado";
    }
    @PostMapping("/notificaciones/{id}/leer")
    public String leerAviso(@PathVariable long id,Principal principal) {
        repo.marcarLeida(id,usuario(principal).id());
        return "redirect:/mi-cuenta";
    }
    @GetMapping("/planes")
    public String planes(Principal principal,Model model) {
        Usuario u=usuario(principal);
        model.addAttribute("usuario",u);
        model.addAttribute("planes",biblioteca.planes());
        model.addAttribute("suscripcion",biblioteca.suscripcion(u.id()));
        return "planes";
    }
    @PostMapping("/planes")
    public String cambiarPlan(@RequestParam String codigo,Principal principal) {
        biblioteca.cambiarPlan(usuario(principal).id(),codigo);
        return "redirect:/planes?actualizado";
    }
    @GetMapping("/recomendaciones")
    public String recomendaciones(Principal principal,Model model) {
        Usuario u=usuario(principal);
        model.addAttribute("usuario",u);
        model.addAttribute("libros",biblioteca.recomendaciones(u.id()));
        model.addAttribute("preferencia",repo.preferencia(u.id()).orElse(""));
        return "recomendaciones";
    }
    @PostMapping("/preferencias")
    public String preferencia(@RequestParam String genero,Principal principal) {
        recomendaciones.preferir(usuario(principal).id(),genero);
        return "redirect:/recomendaciones?actualizado";
    }
    @GetMapping("/colecciones")
    public String colecciones(Principal principal,Model model) {
        model.addAttribute("usuario",usuario(principal));
        model.addAttribute("colecciones",catalogo.arbolColecciones());
        return "colecciones";
    }
}
