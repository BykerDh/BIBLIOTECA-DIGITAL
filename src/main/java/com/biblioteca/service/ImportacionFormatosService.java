package com.biblioteca.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import com.biblioteca.repository.BibliotecaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ImportacionFormatosService {
    public record Entrada(int gutenbergId,String formato,String archivo,String sha256,String epubSha256) {}
    public record Manifiesto(List<Entrada> archivos) {}
    private final BibliotecaRepository repo;
    private final Path storage;
    private final TransactionTemplate transaccion;
    public ImportacionFormatosService(BibliotecaRepository repo,@Value("${app.storage.path}") String storage,PlatformTransactionManager manager) {
        this.repo=repo;this.storage=Path.of(storage).toAbsolutePath().normalize();transaccion=new TransactionTemplate(manager);
    }
    public int importar(Path manifiesto) throws IOException {
        var datos=JsonMapper.builder().build().readValue(manifiesto.toFile(),Manifiesto.class);
        if(datos.archivos()==null || datos.archivos().size()!=40) throw new IllegalArgumentException("Se requieren PDF y MOBI para exactamente 20 libros");
        Set<String> claves=new HashSet<>();Set<Integer> libros=new HashSet<>();
        for(Entrada e:datos.archivos()) {
            if(e.gutenbergId()<1 || !Set.of("PDF","MOBI").contains(e.formato()) || !e.archivo().equals(e.gutenbergId()+"."+e.formato().toLowerCase(Locale.ROOT))
                    || !claves.add(e.gutenbergId()+"-"+e.formato()) || !e.sha256().matches("[a-f0-9]{64}")) throw new IllegalArgumentException("Entrada de formato invalida");
            libros.add(e.gutenbergId());
            var fuente=repo.fuenteExterna("Project Gutenberg",String.valueOf(e.gutenbergId())).orElseThrow(()->new IllegalArgumentException("Primero importa el catalogo EPUB"));
            if(!fuente.sha256().equals(e.epubSha256())) throw new IllegalArgumentException("La conversion corresponde a otra edicion EPUB");
            verificar(origen(e),e.formato(),e.sha256());
            verificar(storage.resolve(repo.archivo(fuente.libroId(),"EPUB").claveArchivo()),"EPUB",e.epubSha256());
        }
        if(libros.size()!=20) throw new IllegalArgumentException("La seleccion debe contener 20 libros distintos");
        int nuevos=0;
        for(Entrada e:datos.archivos()) {
            long id=repo.fuenteExterna("Project Gutenberg",String.valueOf(e.gutenbergId())).orElseThrow().libroId();
            var existentes=repo.archivos(id).stream().filter(a->a.formato().equals(e.formato())).toList();
            if(!existentes.isEmpty()) { verificar(storage.resolve(existentes.getFirst().claveArchivo()),e.formato(),e.sha256());continue; }
            String clave=UUID.randomUUID().toString();Path destino=storage.resolve(clave);Files.copy(origen(e),destino);
            try {
                long tamano=Files.size(destino);
                transaccion.executeWithoutResult(s->{repo.bloquearLibro(id);repo.crearArchivo(id,e.formato(),clave,"gutenberg-"+e.archivo(),tamano);repo.auditar(null,"AGREGAR_FORMATO_"+e.formato(),"LIBRO",id);});
                nuevos++;
            } catch(RuntimeException | IOException error) {Files.deleteIfExists(destino);throw error;}
        }
        return nuevos;
    }
    private Path origen(Entrada e) {return storage.resolve("multiformato").resolve(e.archivo());}
    private void verificar(Path ruta,String formato,String hash) throws IOException {
        Path normal=ruta.toAbsolutePath().normalize();
        if(!normal.startsWith(storage) || !Files.isRegularFile(normal)) throw new IOException("Falta el archivo de "+formato);
        CatalogoService.verificarFirma(normal,formato);
        if(!ImportacionCatalogoService.sha256(normal).equals(hash)) throw new IOException("El archivo no coincide con su SHA-256");
    }
}
