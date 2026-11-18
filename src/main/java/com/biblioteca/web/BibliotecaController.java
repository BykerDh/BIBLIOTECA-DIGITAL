package com.biblioteca.web;

import com.biblioteca.domain.Models.*;
import com.biblioteca.patterns.structural.facade.BibliotecaFacade;
import com.biblioteca.repository.BibliotecaRepository;
import com.biblioteca.service.CatalogoService;
import com.biblioteca.service.RecomendacionService;
import java.io.IOException;
import java.security.Principal;
import java.util.Locale;
import jakarta.servlet.http.HttpServletResponse;
import com.biblioteca.patterns.creational.factorymethod.LectorFactory;
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
    public String libros(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="false") boolean completos,Principal principal,Model model) {
        if (q.length()>200) throw new IllegalArgumentException("La busqueda supera los 200 caracteres");
        int p=Math.max(0,pagina);
        Usuario u=usuario(principal);
        model.addAttribute("libros",catalogo.buscar(q,p,completos));
        model.addAttribute("total",catalogo.total(q,completos));
        model.addAttribute("completos",completos);
        model.addAttribute("pagina",p);
        model.addAttribute("q",q);
        model.addAttribute("usuario",u);
        if (!q.isBlank()) repo.guardarBusqueda(u.id(),q.trim());
        return "libros";
    }
    @GetMapping("/libros/{id}")
    public String detalle(@PathVariable long id,Principal principal,Model model) {
        Libro libro=biblioteca.libro(id);
        Usuario u=usuario(principal);
        int disponibles=biblioteca.disponibles(libro);
        var primera=repo.primeraReserva(id);
        boolean prestado=repo.yaPrestado(u.id(),id);
        model.addAttribute("libro",libro);
        model.addAttribute("archivos",biblioteca.archivos(id));
        model.addAttribute("fuente",repo.fuente(id).orElse(null));
        model.addAttribute("disponibles",disponibles);
        model.addAttribute("puedePrestar",!prestado && disponibles>0 && (primera.isEmpty() || primera.get().usuarioId()==u.id()));
        model.addAttribute("puedeReservar",!prestado && !repo.yaReservado(u.id(),id) && (disponibles==0 || primera.isPresent()));
        model.addAttribute("usuario",u);
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
    @GetMapping("/libros/{id}/leer/{formato}")
    public String lector(@PathVariable long id,@PathVariable String formato,Principal principal,Model model,HttpServletResponse response) {
        String f=formato.toUpperCase(Locale.ROOT);LectorFactory.para(f);
        biblioteca.validarLectura(usuario(principal).id(),id);
        repo.archivo(id,f);
        model.addAttribute("libro",biblioteca.libro(id));model.addAttribute("formato",f);
        response.setHeader("Cache-Control","private, no-store");
        // Los libros no pueden ejecutar scripts incrustados ni cargar recursos desde sitios externos.
        response.setHeader("Content-Security-Policy","default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline' blob:; img-src 'self' data: blob:; font-src 'self' data: blob:; frame-src 'self' blob:; connect-src 'self' blob:; object-src 'none'; base-uri 'none'; form-action 'self'");
        return "lector";
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
    @GetMapping("/colecciones/{id}/libros")
    public String librosColeccion(@PathVariable long id,@RequestParam(defaultValue="0") int pagina,
                                  Principal principal,Model model) {
        model.addAttribute("usuario",usuario(principal));
        model.addAttribute("recorrido",catalogo.librosColeccion(id,pagina));
        model.addAttribute("coleccionId",id);
        return "coleccion-libros";
    }
}
