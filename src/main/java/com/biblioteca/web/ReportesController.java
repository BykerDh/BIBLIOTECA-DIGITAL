package com.biblioteca.web;

import com.biblioteca.service.ReportesService;
import com.biblioteca.service.ReportesService.Seccion;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/gestion")
public class ReportesController {
    private final ReportesService reportes;
    public ReportesController(ReportesService reportes) { this.reportes=reportes; }
    @GetMapping public String inicio() { return "redirect:/admin/gestion/libros"; }
    @GetMapping("/{tipo}")
    public String pagina(@PathVariable String tipo,@RequestParam(defaultValue="") String q,
                         @RequestParam(defaultValue="0") int pagina,Model model) {
        Seccion s=Seccion.de(tipo);
        model.addAttribute("seccion",s);model.addAttribute("secciones",Seccion.values());model.addAttribute("q",q);
        model.addAttribute("resultado",reportes.consultar(s,q,pagina));return "admin-gestion";
    }
    @GetMapping("/{tipo}/excel")
    public void excel(@PathVariable String tipo,@RequestParam(defaultValue="") String q,HttpServletResponse response) throws IOException {
        Seccion s=Seccion.de(tipo);
        if(q.length()>200) throw new IllegalArgumentException("Busqueda demasiado larga");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition","attachment; filename=\"biblioteca-"+s.ruta()+".xlsx\"");
        response.setHeader("Cache-Control","private, no-store");
        reportes.exportar(s,q,response.getOutputStream());
    }
}
