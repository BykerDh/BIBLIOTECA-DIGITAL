package com.biblioteca.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.biblioteca.domain.Models.Archivo;
import com.biblioteca.domain.Models.FuenteLibro;
import com.biblioteca.repository.BibliotecaRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import tools.jackson.databind.json.JsonMapper;

class ImportacionCatalogoServiceTest {
    @TempDir Path storage;
    private final BibliotecaRepository repo=mock(BibliotecaRepository.class);
    private final CatalogoService catalogo=mock(CatalogoService.class);
    private final PlatformTransactionManager manager=mock(PlatformTransactionManager.class);
    private ImportacionCatalogoService service;
    private ImportacionCatalogoService.EntradaLibro entrada;
    private Path manifiesto;

    @BeforeEach void preparar() throws Exception {
        service=new ImportacionCatalogoService(repo,catalogo,manager,storage.toString());
        when(manager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        Files.createDirectories(storage.resolve("importacion"));
        Path epub=storage.resolve("importacion/2000.epub");
        try (var zip=new ZipOutputStream(Files.newOutputStream(epub))) {
            zip.putNextEntry(new ZipEntry("mimetype"));
            zip.write("application/epub+zip".getBytes(StandardCharsets.US_ASCII));
            zip.closeEntry();
        }
        entrada=new ImportacionCatalogoService.EntradaLibro(2000,"Don Quijote","Cervantes","Novela","es","Descripcion",
                "https://www.gutenberg.org/ebooks/2000","https://gutenberg.pglaf.org/cache/generated/2000/pg2000-images.epub",
                "Public domain in the USA.",1616,ImportacionCatalogoService.sha256(epub));
        manifiesto=storage.resolve("catalogo.json");
        escribir(List.of(entrada));
    }

    private void escribir(List<ImportacionCatalogoService.EntradaLibro> entradas) {
        JsonMapper.builder().build().writeValue(manifiesto.toFile(),new ImportacionCatalogoService.CatalogoPublico("2026-10-13",entradas));
    }

    @Test void libroExistenteSeVerificaSinDuplicarlo() throws Exception {
        Files.copy(storage.resolve("importacion/2000.epub"),storage.resolve("clave"));
        when(repo.fuenteExterna("Project Gutenberg","2000")).thenReturn(Optional.of(
                new FuenteLibro(10,"Project Gutenberg","2000",entrada.fuenteUrl(),entrada.derechos(),entrada.sha256())));
        when(repo.archivo(10,"EPUB")).thenReturn(new Archivo(1,10,"EPUB","clave","quijote.epub",10));
        assertEquals(new ImportacionCatalogoService.Resultado(0,1),service.importar(manifiesto));
        verifyNoInteractions(catalogo,manager);
    }

    @Test void hashAlteradoSeRechazaAntesDeTocarLaBase() throws Exception {
        Files.writeString(storage.resolve("importacion/2000.epub"),"invalido");
        assertThrows(Exception.class,() -> service.importar(manifiesto));
        verifyNoInteractions(repo,catalogo,manager);
    }

    @Test void idsDuplicadosSeRechazanAntesDeCrear() {
        escribir(List.of(entrada,entrada));
        assertThrows(IllegalArgumentException.class,() -> service.importar(manifiesto));
        verifyNoInteractions(repo,catalogo,manager);
    }

    @Test void falloDeBaseRevierteYEliminaSoloLaCopiaNueva() throws Exception {
        when(catalogo.crear(anyString(),anyString(),anyString(),anyString(),anyString(),isNull(),anyString(),isNull(),anyString(),anyInt()))
                .thenThrow(new IllegalStateException("BD no disponible"));
        assertThrows(IllegalStateException.class,() -> service.importar(manifiesto));
        verify(manager).rollback(any());
        try (var archivos=Files.list(storage)) { assertEquals(2,archivos.count()); }
        assertTrue(Files.exists(storage.resolve("importacion/2000.epub")));
    }

    @Test void faltaDeArchivoNoSeConsideraImportacionCompletada() throws Exception {
        Files.delete(storage.resolve("importacion/2000.epub"));
        assertThrows(IOException.class,() -> service.importar(manifiesto));
        verifyNoInteractions(repo,catalogo,manager);
    }
}
