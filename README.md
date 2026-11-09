# Biblioteca Digital

Aplicacion Spring MVC para catalogo, entrega de PDF/EPUB/MOBI, prestamos, reservas, recomendaciones y suscripciones.

## Organizacion

- `src/main/java/com/biblioteca`: codigo de la aplicacion.
- `patterns/creational`: Singleton, Factory Method, Abstract Factory, Prototype y Builder.
- `patterns/structural`: Adapter, Bridge, Composite, Decorator, Facade, Flyweight y Proxy.
- `patterns/behavioral`: Iterator, Mediator y Memento.
- `src/main/resources/templates` y `static`: pantallas y estilos.
- `src/main/resources/db/migration`: esquema de MariaDB versionado con Flyway.
- `src/test`: pruebas automatizadas. No son codigo sobrante.
- `scripts`: configuracion, cambio de clave, importacion y comprobacion local.
- `catalogo`: manifiesto y procedencia de los 100 libros.
- `storage`: archivos reales de los libros; `storage/importacion` conserva los originales usados por el importador. No se debe borrar al limpiar el proyecto.
- `Documentacion`: entregas y material academico.
- `target`: resultado regenerable de Maven, excluido de Git.

Las tres carpetas de patrones estan dentro de `src/main/java/com/biblioteca/patterns`.

## Inicio local con XAMPP

1. Inicia Apache y MariaDB en el panel de XAMPP.
2. Abre PowerShell en esta carpeta y ejecuta `./run-local.ps1`.
3. Abre `http://localhost:8080` y phpMyAdmin en `http://localhost/phpmyadmin`.
4. La primera ejecucion crea la base `biblioteca_digital`, una cuenta SQL exclusiva de la aplicacion y `.local.env.ps1`. Las migraciones crean las tablas al arrancar.
5. Las credenciales del administrador de la biblioteca estan en `.local.env.ps1`. Ese archivo no se incluye en Git.

Se requiere JDK 25 y XAMPP en `C:\xampp`. Si MariaDB ya usa la cuenta `biblioteca_proyecto_app`, el instalador se detiene para protegerla. Configura entonces `DB_URL`, `DB_USER`, `DB_PASSWORD`, `LIBRARY_ADMIN_EMAIL` y `LIBRARY_ADMIN_PASSWORD` manualmente.

## Configuracion de despliegue

La aplicacion lee `DB_URL`, `DB_USER`, `DB_PASSWORD`, `BOOK_STORAGE_PATH` y las credenciales iniciales de administrador desde variables de entorno. Configura un directorio persistente compartido para `BOOK_STORAGE_PATH` si vas a ejecutar varias instancias. Usa HTTPS y una base MariaDB administrada; no publiques phpMyAdmin directamente en Internet. Las sesiones web se guardan en MariaDB y pueden compartirse entre instancias. El estado se consulta en `/actuator/health`.

Las tablas se versionan en `src/main/resources/db/migration`. Los archivos de libros se guardan en `storage/` fuera de Git. El patron Prototype copia solo metadatos y requiere subir archivos nuevos. El administrador puede cargar archivos con licencia de uso.

## Catalogo de 100 libros

`catalogo/libros-publicos.json` registra 100 obras reales en espanol, su fuente Project Gutenberg y la huella SHA-256 del EPUB. Para descargarlas e importarlas sin duplicados, despues de configurar XAMPP:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\importar-catalogo-publico.ps1
```

La carga conserva los registros existentes y agrega la procedencia en `fuentes_libro`. Las ediciones conservan su licencia original. Gutenberg las clasifica como dominio publico en Estados Unidos; revisa las condiciones de tu pais antes de redistribuirlas. Los EPUB se descargan para abrirse en un lector compatible; los PDF pueden abrirse en el navegador.

Las entregas academicas se conservan en `Documentacion`, organizadas por corte.

## Patrones de comportamiento

- Iterator: abrir Colecciones y pulsar una coleccion para listar sus libros y los de sus subcolecciones sin duplicados.
- Mediator: al devolver un libro o cancelar una reserva, coordina el aviso al primer usuario en espera si hay cupo.
- Memento: en Editar libro, Historial de la ficha permite deshacer hasta diez cambios descriptivos. No revierte archivos, ISBN, licencias ni prestamos.

## Pruebas

`./mvnw.cmd test` ejecuta las pruebas de patrones y reglas de circulacion. La meta de mas de 100.000 usuarios es una meta de capacidad: requiere infraestructura y pruebas de carga antes de afirmarse como rendimiento alcanzado.

Para incluir las pruebas con MariaDB real, iniciar MySQL en XAMPP y ejecutar:

```powershell
. .\.local.env.ps1
$env:BIBLIOTECA_DB_TEST='true'
.\mvnw.cmd verify
Remove-Item Env:BIBLIOTECA_DB_TEST
```

Los datos creados por esas pruebas se revierten al terminar cada caso; las migraciones del esquema permanecen aplicadas.

Para regenerar los archivos de compilacion, detener primero la aplicacion y ejecutar `./mvnw.cmd clean package`. No borrar `storage`, `.local.env.ps1`, las migraciones ni `.mvn`. No publicar contrasenas en Git.
