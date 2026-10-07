package com.biblioteca.web;

import com.biblioteca.domain.Models.Libro;
import com.biblioteca.service.CatalogoService;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdministracionController {
    private final CatalogoService catalogo;
    public AdministracionController(CatalogoService catalogo) { this.catalogo=catalogo; }
    @GetMapping("/admin/libros/nuevo") public String nuevo(Model model) { return "admin-libro"; }
    @GetMapping("/admin/libros/{id}/editar")
    public String editar(@PathVariable long id,Model model) {
        model.addAttribute("libro",catalogo.libro(id));
        model.addAttribute("archivos",catalogo.archivos(id));
        return "admin-libro";
    }
    @PostMapping("/admin/libros/guardar")
    public String guardar(@RequestParam(required=false) Long id,@RequestParam String titulo,@RequestParam String autor,
                          @RequestParam String genero,@RequestParam String idioma,@RequestParam String descripcion,
                          @RequestParam(required=false) String isbn,@RequestParam(required=false) String editorial,
                          @RequestParam(required=false) Integer paginas,@RequestParam String accesoMinimo,
                          @RequestParam int licencias) {
        if (id==null) id=catalogo.crear(titulo,autor,genero,idioma,descripcion,isbn,editorial,paginas,accesoMinimo,licencias);
        else catalogo.actualizar(id,titulo,autor,genero,idioma,descripcion,isbn,editorial,paginas,accesoMinimo,licencias);
        return "redirect:/admin/libros/"+id+"/editar?guardado";
    }
    @PostMapping("/admin/libros/{id}/archivo")
    public String subir(@PathVariable long id,@RequestParam String formato,@RequestParam MultipartFile archivo) throws IOException {
        catalogo.subirArchivo(id,formato,archivo);
        return "redirect:/admin/libros/"+id+"/editar?archivo";
    }
    @PostMapping("/admin/libros/{id}/clonar")
    public String clonar(@PathVariable long id,@RequestParam String titulo) {
        long nuevo=catalogo.clonar(id,titulo);
        return "redirect:/admin/libros/"+nuevo+"/editar?clonado";
    }
    @PostMapping("/admin/libros/{id}/retirar")
    public String retirar(@PathVariable long id) {
        catalogo.desactivar(id);
        return "redirect:/libros?retirado";
    }
    @GetMapping("/admin/colecciones")
    public String colecciones(Model model) {
        model.addAttribute("colecciones",catalogo.colecciones());
        return "admin-colecciones";
    }
    @PostMapping("/admin/colecciones")
    public String crearColeccion(@RequestParam String nombre,@RequestParam(required=false) Long padreId) {
        catalogo.crearColeccion(nombre,padreId);
        return "redirect:/admin/colecciones?creada";
    }
    @PostMapping("/admin/colecciones/asignar")
    public String asignar(@RequestParam long coleccionId,@RequestParam long libroId) {
        catalogo.agregarLibro(coleccionId,libroId);
        return "redirect:/admin/colecciones?asignado";
    }
}
