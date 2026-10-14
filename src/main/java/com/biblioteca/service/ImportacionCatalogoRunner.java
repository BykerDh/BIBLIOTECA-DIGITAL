package com.biblioteca.service;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("importar-catalogo")
@Order(Ordered.LOWEST_PRECEDENCE)
public class ImportacionCatalogoRunner implements ApplicationRunner {
    private final ImportacionCatalogoService importacion;
    private final Path storage;

    public ImportacionCatalogoRunner(ImportacionCatalogoService importacion,
                                    @Value("${app.storage.path}") String storageDir) {
        this.importacion=importacion; this.storage=Path.of(storageDir);
    }

    @Override public void run(ApplicationArguments args) throws Exception {
        // Este perfil solo se activa desde el comando de importacion, nunca al iniciar la web normalmente.
        try (var canal=FileChannel.open(storage.resolve("importacion.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);
             var bloqueo=canal.tryLock()) {
            if (bloqueo==null) throw new IllegalStateException("Ya hay una importacion en curso");
            var resultado=importacion.importar(Path.of("catalogo/libros-publicos.json"));
            System.out.printf("CATALOGO: nuevos=%d, existentes=%d%n",resultado.nuevos(),resultado.existentes());
        }
    }
}
