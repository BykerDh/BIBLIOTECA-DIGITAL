package com.biblioteca.service;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportesService {
    public enum Seccion {
        LIBROS("Libros",List.of("ID","Titulo","Autor","Genero","Idioma","Plan minimo","Licencias","Formatos","Estado"),
            "SELECT l.id,l.titulo,l.autor,l.genero,l.idioma,l.acceso_minimo,l.licencias,COALESCE((SELECT GROUP_CONCAT(a.formato ORDER BY a.formato SEPARATOR ', ') FROM archivos_libro a WHERE a.libro_id=l.id),'Sin archivos'),IF(l.activo,'ACTIVO','RETIRADO') FROM libros l",
            "libros l","l.id","CONCAT_WS(' ',l.titulo,l.autor,l.genero)"),
        PRESTAMOS("Prestamos",List.of("ID","Usuario","Correo","Libro","Inicio","Vencimiento","Devolucion","Estado"),
            "SELECT p.id,u.nombre,u.email,l.titulo,p.prestado_en,p.vence_en,p.devuelto_en,CASE WHEN p.devuelto_en IS NOT NULL THEN 'DEVUELTO' WHEN p.vence_en<=CURRENT_TIMESTAMP THEN 'VENCIDO' ELSE 'VIGENTE' END FROM prestamos p JOIN usuarios u ON u.id=p.usuario_id JOIN libros l ON l.id=p.libro_id",
            "prestamos p JOIN usuarios u ON u.id=p.usuario_id JOIN libros l ON l.id=p.libro_id","p.id","CONCAT_WS(' ',u.nombre,u.email,l.titulo)"),
        RESERVAS("Reservas",List.of("ID","Usuario","Correo","Libro","Creada","Estado"),
            "SELECT r.id,u.nombre,u.email,l.titulo,r.creada_en,r.estado FROM reservas r JOIN usuarios u ON u.id=r.usuario_id JOIN libros l ON l.id=r.libro_id",
            "reservas r JOIN usuarios u ON u.id=r.usuario_id JOIN libros l ON l.id=r.libro_id","r.id","CONCAT_WS(' ',u.nombre,u.email,l.titulo)"),
        RECOMENDACIONES("Recomendaciones",List.of("ID","Usuario","Correo","Libro recomendado","Posicion","Generada"),
            "SELECT r.id,u.nombre,u.email,l.titulo,r.posicion,r.generada_en FROM recomendaciones_usuario r JOIN usuarios u ON u.id=r.usuario_id JOIN libros l ON l.id=r.libro_id",
            "recomendaciones_usuario r JOIN usuarios u ON u.id=r.usuario_id JOIN libros l ON l.id=r.libro_id","r.id","CONCAT_WS(' ',u.nombre,u.email,l.titulo)"),
        SUSCRIPCIONES("Suscripciones",List.of("ID","Usuario","Correo","Plan","Inicio","Terminacion","Estado","Cuenta"),
            "SELECT s.id,u.nombre,u.email,p.nombre,s.inicia_en,s.vence_en,CASE WHEN s.inicia_en>CURRENT_TIMESTAMP THEN 'PENDIENTE' WHEN s.vence_en<=CURRENT_TIMESTAMP THEN 'VENCIDA' ELSE 'VIGENTE' END,IF(u.activo,'ACTIVA','INACTIVA') FROM suscripciones s JOIN usuarios u ON u.id=s.usuario_id JOIN planes p ON p.codigo=s.plan_codigo",
            "suscripciones s JOIN usuarios u ON u.id=s.usuario_id JOIN planes p ON p.codigo=s.plan_codigo","s.id","CONCAT_WS(' ',u.nombre,u.email,p.nombre)");
        private final String titulo,consulta,desde,id,busqueda;
        private final List<String> columnas;
        Seccion(String titulo,List<String> columnas,String consulta,String desde,String id,String busqueda) {
            this.titulo=titulo;this.columnas=columnas;this.consulta=consulta;this.desde=desde;this.id=id;this.busqueda=busqueda;
        }
        public String titulo() { return titulo; }
        public String ruta() { return name().toLowerCase(Locale.ROOT); }
        public List<String> columnas() { return columnas; }
        public static Seccion de(String ruta) {
            try { return valueOf(ruta.toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException error) { throw new IllegalArgumentException("Reporte desconocido"); }
        }
    }
    public record Fila(long id,List<Object> valores) {
        public List<String> texto() {
            return valores.stream().map(v-> v==null ? "—" : v instanceof Timestamp t
                    ? t.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : v.toString()).toList();
        }
    }
    public record Pagina(List<Fila> filas,long total,int pagina,int tamano) {}
    private final JdbcTemplate jdbc;
    public ReportesService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    private String filtro(String q) {
        if(q==null || q.length()>200) throw new IllegalArgumentException("La busqueda debe tener hasta 200 caracteres");
        return "%"+q.trim()+"%";
    }
    private Fila fila(ResultSet r,int n) throws SQLException {
        var valores=new ArrayList<Object>();
        for(int i=1;i<=r.getMetaData().getColumnCount();i++) valores.add(r.getObject(i));
        return new Fila(r.getLong(1),Collections.unmodifiableList(valores));
    }
    @Transactional(readOnly=true)
    public Pagina consultar(Seccion s,String q,int pagina) {
        String patron=filtro(q);
        long total=jdbc.queryForObject("SELECT COUNT(*) FROM "+s.desde+" WHERE "+s.busqueda+" LIKE ?",Long.class,patron);
        int p=(int)Math.min(Math.max(0,pagina),Math.max(0,(total-1)/50));
        var filas=jdbc.query(s.consulta+" WHERE "+s.busqueda+" LIKE ? ORDER BY "+s.id+" LIMIT 50 OFFSET ?",this::fila,patron,(long)p*50);
        return new Pagina(filas,total,p,50);
    }
    @Transactional(readOnly=true)
    public void exportar(Seccion s,String q,OutputStream salida) throws IOException {
        String patron=filtro(q);
        // Paginacion por ID y ventana de 100 filas: el reporte completo no se carga en memoria.
        try(var libro=new SXSSFWorkbook(100)) {
            libro.setCompressTempFiles(true);
            CellStyle cabecera=libro.createCellStyle();Font fuente=libro.createFont();fuente.setBold(true);fuente.setColor(IndexedColors.WHITE.getIndex());cabecera.setFont(fuente);
            cabecera.setFillForegroundColor(IndexedColors.TEAL.getIndex());cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle fecha=libro.createCellStyle();fecha.setDataFormat(libro.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
            int numeroHoja=1,fila=0;Sheet hoja=hoja(libro,s,numeroHoja,cabecera);long ultimo=0;
            while(true) {
                var lote=jdbc.query(s.consulta+" WHERE "+s.busqueda+" LIKE ? AND "+s.id+">? ORDER BY "+s.id+" LIMIT 500",this::fila,patron,ultimo);
                if(lote.isEmpty()) break;
                for(Fila registro:lote) {
                    if(fila>=1_048_573) { hoja.setAutoFilter(new CellRangeAddress(1,fila+1,0,s.columnas.size()-1));hoja=hoja(libro,s,++numeroHoja,cabecera);fila=0; }
                    Row row=hoja.createRow(++fila+1);
                    for(int i=0;i<registro.valores.size();i++) {
                        Cell cell=row.createCell(i);Object valor=registro.valores.get(i);
                        if(valor instanceof Timestamp t) { cell.setCellValue(t.toLocalDateTime());cell.setCellStyle(fecha); }
                        else if(valor instanceof Number n && i!=0) cell.setCellValue(n.doubleValue());
                        else if(valor!=null) cell.setCellValue(valor.toString()); // Texto, nunca formulas procedentes de usuarios.
                    }
                    ultimo=registro.id;
                }
            }
            hoja.setAutoFilter(new CellRangeAddress(1,Math.max(1,fila+1),0,s.columnas.size()-1));
            libro.write(salida);
        }
    }
    private Sheet hoja(SXSSFWorkbook libro,Seccion s,int numero,CellStyle cabecera) {
        Sheet hoja=libro.createSheet(s.titulo+(numero==1?"":" "+numero));
        hoja.createRow(0).createCell(0).setCellValue("Biblioteca Digital · "+s.titulo+" · "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        Row encabezado=hoja.createRow(1);encabezado.setHeightInPoints(25);
        for(int i=0;i<s.columnas.size();i++) { Cell c=encabezado.createCell(i);c.setCellValue(s.columnas.get(i));c.setCellStyle(cabecera);hoja.setColumnWidth(i,(i==0?12:32)*256); }
        hoja.createFreezePane(0,2);hoja.setRepeatingRows(new CellRangeAddress(0,1,-1,-1));
        return hoja;
    }
}
