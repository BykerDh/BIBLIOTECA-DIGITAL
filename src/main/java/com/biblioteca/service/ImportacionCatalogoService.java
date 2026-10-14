package com.biblioteca.service;

import com.biblioteca.domain.Models.FuenteLibro;
import com.biblioteca.repository.BibliotecaRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ImportacionCatalogoService {
    public record EntradaLibro(int gutenbergId,String titulo,String autor,String genero,String idioma,
                               String descripcion,String fuenteUrl,String descargaUrl,String derechos,
                               int ultimoFallecimiento,String sha256) {}
    public record CatalogoPublico(String fecha,List<EntradaLibro> libros) {}
    public record Resultado(int nuevos,int existentes) {}

    private final BibliotecaRepository repo;
    private final CatalogoService catalogo;
    private final TransactionTemplate transaccion;
    private final Path storage;

    public ImportacionCatalogoService(BibliotecaRepository repo,CatalogoService catalogo,
            PlatformTransactionManager manager,@Value("${app.storage.path}") String storageDir) {
        this.repo=repo;
        this.catalogo=catalogo;
        this.transaccion=new TransactionTemplate(manager);
        this.storage=Path.of(storageDir).toAbsolutePath().normalize();
    }

    public Resultado importar(Path manifiesto) throws IOException {
        CatalogoPublico datos=JsonMapper.builder().build().readValue(manifiesto.toFile(),CatalogoPublico.class);
        if (datos.libros()==null || datos.libros().isEmpty()) throw new IllegalArgumentException("Catalogo vacio");
        var ids=new HashSet<Integer>();
        // Valida todo el lote antes de crear registros. No descarga nada en el servidor web.
        for (EntradaLibro libro:datos.libros()) {
            validar(libro);
            if (!ids.add(libro.gutenbergId())) throw new IllegalArgumentException("Identificador Gutenberg repetido");
            validarArchivo(origen(libro),libro.sha256());
        }
        int nuevos=0;
        for (EntradaLibro libro:datos.libros()) if (importarLibro(libro)) nuevos++;
        return new Resultado(nuevos,datos.libros().size()-nuevos);
    }

    static void validar(EntradaLibro libro) {
        if (libro==null || libro.gutenbergId()<1 || libro.titulo()==null || libro.titulo().isBlank()
                || libro.titulo().length()>250 || libro.autor()==null || libro.autor().isBlank()
                || libro.autor().length()>180 || libro.genero()==null || libro.genero().isBlank()
                || libro.genero().length()>100 || libro.descripcion()==null || !"es".equals(libro.idioma()))
            throw new IllegalArgumentException("Ficha de importacion invalida");
        if (!"Public domain in the USA.".equals(libro.derechos())
                || libro.ultimoFallecimiento()<1 || libro.ultimoFallecimiento()>1930)
            throw new IllegalArgumentException("El libro no cumple los criterios de seleccion del lote");
        if (!Objects.equals(libro.fuenteUrl(),"https://www.gutenberg.org/ebooks/"+libro.gutenbergId())
                || !Objects.equals(libro.descargaUrl(),"https://gutenberg.pglaf.org/cache/generated/"
                    +libro.gutenbergId()+"/pg"+libro.gutenbergId()+"-images.epub")
                || libro.sha256()==null || !libro.sha256().matches("[a-f0-9]{64}"))
            throw new IllegalArgumentException("Fuente o huella SHA-256 invalida");
    }

    private Path origen(EntradaLibro libro) { return storage.resolve("importacion/"+libro.gutenbergId()+".epub"); }

    private void validarArchivo(Path archivo,String hash) throws IOException {
        if (!Files.isRegularFile(archivo)) throw new IOException("Falta el EPUB: "+archivo.getFileName());
        if (Files.size(archivo)>30L*1024*1024) throw new IOException("EPUB superior a 30 MiB");
        CatalogoService.verificarFirma(archivo,"EPUB");
        if (!sha256(archivo).equals(hash)) throw new IOException("SHA-256 diferente: "+archivo.getFileName());
    }

    static String sha256(Path archivo) throws IOException {
        try {
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            try (var entrada=Files.newInputStream(archivo)) {
                byte[] buffer=new byte[8192];
                int leidos;
                while ((leidos=entrada.read(buffer))!=-1) digest.update(buffer,0,leidos);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 no disponible",e); }
    }

    private boolean importarLibro(EntradaLibro libro) throws IOException {
        var existente=repo.fuenteExterna("Project Gutenberg",String.valueOf(libro.gutenbergId()));
        if (existente.isPresent()) {
            FuenteLibro fuente=existente.get();
            if (!fuente.sha256().equals(libro.sha256())) throw new IOException("La edicion ya importada tiene otra huella");
            Path archivo=storage.resolve(repo.archivo(fuente.libroId(),"EPUB").claveArchivo()).normalize();
            if (!archivo.startsWith(storage)) throw new IOException("Ruta de archivo invalida");
            validarArchivo(archivo,libro.sha256());
            return false;
        }
        String clave=UUID.randomUUID().toString();
        Path destino=storage.resolve(clave);
        Files.copy(origen(libro),destino);
        try {
            long tamano=Files.size(destino);
            // Una transaccion por libro permite reanudar un lote interrumpido sin duplicarlo.
            transaccion.executeWithoutResult(status -> {
                long id=catalogo.crear(libro.titulo(),libro.autor(),libro.genero(),libro.idioma(),
                        libro.descripcion(),null,"Project Gutenberg",null,"BASICO",5);
                repo.crearArchivo(id,"EPUB",clave,"gutenberg-"+libro.gutenbergId()+".epub",tamano);
                repo.registrarFuente(new FuenteLibro(id,"Project Gutenberg",String.valueOf(libro.gutenbergId()),
                        libro.fuenteUrl(),libro.derechos(),libro.sha256()));
                long raiz=coleccion("Clasicos de dominio publico",null);
                long genero=coleccion(libro.genero(),raiz);
                repo.agregarColeccionLibro(genero,id);
                repo.auditar(null,"IMPORTAR_GUTENBERG","LIBRO",id);
            });
            return true;
        } catch (RuntimeException | IOException error) {
            // La BD revierte la ficha y aqui se limpia unicamente el archivo recien creado.
            try { Files.deleteIfExists(destino); } catch (IOException limpieza) { error.addSuppressed(limpieza); }
            throw error;
        }
    }

    private long coleccion(String nombre,Long padre) {
        return repo.colecciones().stream().filter(c -> c.nombre().equals(nombre) && Objects.equals(c.padreId(),padre))
                .mapToLong(c -> c.id()).findFirst().orElseGet(() -> catalogo.crearColeccion(nombre,padre));
    }
}
