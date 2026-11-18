package com.biblioteca.service;

import java.nio.channels.FileChannel;
import java.nio.file.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("importar-formatos")
public class ImportacionFormatosRunner implements ApplicationRunner {
    private final ImportacionFormatosService formatos;
    private final Path storage;
    public ImportacionFormatosRunner(ImportacionFormatosService formatos,@Value("${app.storage.path}") String storage) {this.formatos=formatos;this.storage=Path.of(storage);}
    @Override public void run(ApplicationArguments args) throws Exception {
        try(var canal=FileChannel.open(storage.resolve("formatos.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);var bloqueo=canal.tryLock()) {
            if(bloqueo==null) throw new IllegalStateException("Ya se estan importando formatos");
            System.out.println("FORMATOS NUEVOS: "+formatos.importar(Path.of("catalogo/formatos-adicionales.json")));
        }
    }
}
