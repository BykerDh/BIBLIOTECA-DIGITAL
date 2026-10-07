package com.biblioteca.service;

import com.biblioteca.domain.Models.*;
import com.biblioteca.patterns.creational.builder.FichaLibro;
import com.biblioteca.patterns.creational.prototype.PlantillaLibroDigital;
import com.biblioteca.patterns.creational.singleton.ConfiguracionBiblioteca;
import com.biblioteca.patterns.structural.adapter.ArchivoResourceAdapter;
import com.biblioteca.patterns.structural.adapter.ContenidoLibro;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.ColeccionNodo;
import com.biblioteca.patterns.structural.composite.ComponenteCatalogo.LibroHoja;
import com.biblioteca.repository.BibliotecaRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CatalogoService {
    private final BibliotecaRepository repo;
    private final Path storage;
    public CatalogoService(BibliotecaRepository repo,@Value("${app.storage.path}") String storageDir) throws IOException {
        this.repo=repo;
        this.storage=Path.of(storageDir).toAbsolutePath().normalize();
        Files.createDirectories(storage);
    }
    public List<Libro> buscar(String texto,int pagina) { return repo.libros(texto,20,Math.max(0,pagina)*20); }
    public int total(String texto) { return repo.totalLibros(texto); }
    public Libro libro(long id) { return repo.libro(id); }
    public List<Archivo> archivos(long id) { return repo.archivos(id); }
    public List<Coleccion> colecciones() { return repo.colecciones(); }

    @Transactional
    public long crear(String titulo,String autor,String genero,String idioma,String descripcion,String isbn,
                      String editorial,Integer paginas,String acceso,int licencias) {
        Libro ficha=new FichaLibro.Builder(titulo,autor).conGenero(genero).conIdioma(idioma)
                .conDescripcion(descripcion).conIsbn(isbn).conEditorial(editorial)
                .conPaginas(paginas).conAccesoMinimo(acceso).conLicencias(licencias).construir();
        long id=repo.crearLibro(ficha);
        repo.auditar(null,"CREAR_LIBRO","LIBRO",id);
        return id;
    }
    @Transactional
    public void actualizar(long id,String titulo,String autor,String genero,String idioma,String descripcion,
                           String isbn,String editorial,Integer paginas,String acceso,int licencias) {
        repo.bloquearLibro(id);
        if (licencias<repo.prestamosActivosLibro(id)) throw new IllegalStateException("No puedes reducir las licencias por debajo de los prestamos activos");
        Libro ficha=new FichaLibro.Builder(titulo,autor).conGenero(genero).conIdioma(idioma)
                .conDescripcion(descripcion).conIsbn(isbn).conEditorial(editorial)
                .conPaginas(paginas).conAccesoMinimo(acceso).conLicencias(licencias).construir();
        repo.actualizarLibro(new Libro(id,ficha.titulo(),ficha.autor(),ficha.genero(),ficha.idioma(),ficha.descripcion(),
                ficha.isbn(),ficha.editorial(),ficha.paginas(),ficha.accesoMinimo(),ficha.licencias(),true));
        repo.auditar(null,"ACTUALIZAR_LIBRO","LIBRO",id);
    }
    @Transactional
    public long clonar(long id,String nuevoTitulo) {
        if (nuevoTitulo==null || nuevoTitulo.isBlank()) throw new IllegalArgumentException("Indica el titulo de la copia");
        Libro copia=new PlantillaLibroDigital(repo.libro(id)).clonar(nuevoTitulo.trim());
        long nuevoId=repo.crearLibro(copia);
        repo.auditar(null,"CLONAR_LIBRO","LIBRO",nuevoId);
        return nuevoId;
    }
    @Transactional
    public void desactivar(long id) {
        repo.bloquearLibro(id);
        if (repo.prestamosActivosLibro(id)>0) throw new IllegalStateException("No puedes retirar un libro con prestamos activos");
        repo.desactivarLibro(id);
        repo.auditar(null,"DESACTIVAR_LIBRO","LIBRO",id);
    }

    public void subirArchivo(long bookId,String formato,MultipartFile subida) throws IOException {
        repo.libro(bookId);
        String f=formato.toUpperCase();
        if (!ConfiguracionBiblioteca.getInstancia().getFormatos().contains(f)) throw new IllegalArgumentException("Formato no admitido");
        if (subida.isEmpty()) throw new IllegalArgumentException("El archivo esta vacio");
        String clave=UUID.randomUUID().toString();
        Path ruta=storage.resolve(clave).normalize();
        try {
            Files.copy(subida.getInputStream(),ruta);
            verificarFirma(ruta,f);
            String nombre=Path.of(subida.getOriginalFilename()==null?"libro."+f.toLowerCase():subida.getOriginalFilename()).getFileName().toString();
            repo.crearArchivo(bookId,f,clave,nombre,Files.size(ruta));
            repo.auditar(null,"SUBIR_ARCHIVO","LIBRO",bookId);
        } catch (Exception e) {
            Files.deleteIfExists(ruta);
            throw e;
        }
    }
    private void verificarFirma(Path ruta,String formato) throws IOException {
        byte[] prefijo;
        try (InputStream entrada=Files.newInputStream(ruta)) { prefijo=entrada.readNBytes(80); }
        if ("PDF".equals(formato) && (prefijo.length<5 || !new String(prefijo,0,5,StandardCharsets.US_ASCII).equals("%PDF-")))
            throw new IllegalArgumentException("El archivo no es PDF");
        if ("EPUB".equals(formato)) {
            try (ZipFile zip=new ZipFile(ruta.toFile())) {
                var entry=zip.getEntry("mimetype");
                if (entry==null || !new String(zip.getInputStream(entry).readAllBytes(),StandardCharsets.US_ASCII).equals("application/epub+zip"))
                    throw new IllegalArgumentException("El archivo no es EPUB");
            }
        }
        if ("MOBI".equals(formato) && (prefijo.length<68 || !new String(prefijo,60,8,StandardCharsets.US_ASCII).equals("BOOKMOBI")))
            throw new IllegalArgumentException("El archivo no es MOBI");
    }
    public ContenidoLibro contenido(long bookId,String formato) throws IOException {
        Archivo a=repo.archivo(bookId,formato);
        Path ruta=storage.resolve(a.claveArchivo()).normalize();
        if (!ruta.startsWith(storage) || !Files.isRegularFile(ruta)) throw new IllegalStateException("Archivo no disponible");
        return new ArchivoResourceAdapter(a,new UrlResource(ruta.toUri()));
    }
    @Transactional
    public long crearColeccion(String nombre,Long padreId) {
        if (nombre==null || nombre.isBlank()) throw new IllegalArgumentException("Nombre obligatorio");
        if (padreId!=null && repo.colecciones().stream().noneMatch(c -> c.id()==padreId)) throw new IllegalArgumentException("Coleccion padre no encontrada");
        return repo.crearColeccion(nombre.trim(),padreId);
    }
    @Transactional
    public void agregarLibro(long coleccionId,long libroId) {
        repo.libro(libroId);
        if (repo.colecciones().stream().noneMatch(c -> c.id()==coleccionId)) throw new IllegalArgumentException("Coleccion no encontrada");
        repo.agregarColeccionLibro(coleccionId,libroId);
    }
    public List<ColeccionNodo> arbolColecciones() {
        Map<Long,ColeccionNodo> nodos=new HashMap<>();
        List<Coleccion> todas=repo.colecciones();
        for (Coleccion c:todas) nodos.put(c.id(),new ColeccionNodo(c.id(),c.nombre()));
        List<ColeccionNodo> raices=new ArrayList<>();
        for (Coleccion c:todas) {
            ColeccionNodo nodo=nodos.get(c.id());
            if (c.padreId()==null) raices.add(nodo);
            else nodos.get(c.padreId()).agregar(nodo);
            for (long bookId:repo.idsLibrosColeccion(c.id())) {
                try { Libro b=repo.libro(bookId); nodo.agregar(new LibroHoja(b.id(),b.titulo())); }
                catch (IllegalArgumentException ignored) { /* El libro retirado no aparece en el arbol. */ }
            }
        }
        return raices;
    }
}
