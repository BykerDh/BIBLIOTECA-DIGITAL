# Biblioteca UTS

Biblioteca digital desarrollada con Spring MVC, Java 25, Thymeleaf y MariaDB. Permite gestionar el catálogo, préstamos, reservas, recomendaciones y suscripciones, con lectura en la web y descarga de PDF, EPUB y MOBI.

La interfaz comparte una identidad visual en verde bosque y marfil, portadas tipográficas y navegación adaptada a computador y celular. El lector EPUB/MOBI permite ajustar el texto y elegir fondo claro, sepia o noche.

## Ejecutar con XAMPP y Visual Studio Code

Se requieren JDK 25 y XAMPP instalado en `C:\xampp`. El proyecto incluye Maven Wrapper.

1. Inicia Apache y MariaDB desde XAMPP.
2. Abre esta carpeta en VS Code y ejecuta en su terminal PowerShell:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\run-local.ps1
```

3. Abre `http://localhost:8080`. La base se administra en `http://localhost/phpmyadmin`.
4. La primera ejecución configura `biblioteca_digital`, una cuenta SQL exclusiva de la aplicación y el archivo local `.local.env.ps1`. Flyway crea o actualiza las tablas.
5. Las credenciales iniciales del administrador están en `.local.env.ps1`. Este archivo es privado y está excluido de Git.

Si MariaDB ya tiene la cuenta `biblioteca_proyecto_app`, el instalador se detiene para protegerla. En ese caso, configura las variables `DB_URL`, `DB_USER`, `DB_PASSWORD`, `LIBRARY_ADMIN_EMAIL` y `LIBRARY_ADMIN_PASSWORD` para tu instalación.

Para detener la aplicación usa `Ctrl+C` en la terminal donde la ejecutaste. Si el puerto 8080 está ocupado, comprueba primero si la biblioteca ya está abierta; evita ejecutar dos copias en el mismo puerto.

## Pantallas y funciones

- **Inicio y catálogo:** búsqueda por título, autor o género; filtro de libros con los tres formatos.
- **Ficha del libro:** préstamo, reserva, valoración, lectura en la web y descarga de cada formato disponible. La lectura exige un préstamo vigente y un plan compatible.
- **Colecciones:** navegación por temas, subcolecciones y sus libros sin duplicados.
- **Para ti:** recomendaciones basadas en lecturas, valoraciones y género preferido.
- **Mi cuenta:** préstamos, devoluciones, reservas, avisos y fechas de inicio y terminación de la suscripción.
- **Planes:** selección y renovación del plan según sus límites y beneficios.
- **Administración:** cinco pestañas para libros, préstamos, reservas, recomendaciones y suscripciones. Cada una permite buscar y descargar un reporte Excel con todos los resultados filtrados.
- **Gestión del catálogo:** creación y edición de fichas, carga de archivos, clonación, historial de cambios, organización de colecciones y resumen de disponibilidad.

Las recomendaciones del reporte administrativo corresponden al último resultado generado para cada usuario al visitar Inicio o Para ti.

## Organización

- `src/main/java/com/biblioteca`: código de la aplicación.
- `patterns/creational`: Singleton, Factory Method, Abstract Factory, Prototype y Builder.
- `patterns/structural`: Adapter, Bridge, Composite, Decorator, Facade, Flyweight y Proxy.
- `patterns/behavioral`: Iterator, Mediator, Memento, Observer, State y Visitor.
- `src/main/resources/templates` y `static`: pantallas, estilos, iconos y lector.
- `src/main/resources/static/vendor/foliate`: lector EPUB/MOBI y sus licencias; funciona con recursos locales.
- `src/main/resources/db/migration`: migraciones de MariaDB con Flyway.
- `src/test`: pruebas automatizadas de patrones, reglas, seguridad e integración.
- `scripts`: configuración, cambio de clave, importación y preparación de formatos.
- `catalogo`: manifiestos de procedencia y huellas SHA-256 de los libros.
- `storage`: archivos reales de los libros; queda fuera de Git. No debe borrarse al limpiar el proyecto.
- `Documentacion`: entregas académicas y material del proyecto.
- `target`: compilación regenerable, excluida de Git.

Las tres carpetas de patrones están dentro de `src/main/java/com/biblioteca/patterns`.

## Preparar el catálogo en otra instalación

Los binarios de libros no se incluyen en Git. El catálogo se puede reconstruir mediante los scripts incluidos, después de configurar XAMPP y la conexión local.

`catalogo/libros-publicos.json` registra 100 obras reales en español y sus EPUB de Project Gutenberg. Para descargarlas e importarlas sin duplicados:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\importar-catalogo-publico.ps1
```

Para disponer de los mismos primeros 20 libros también en PDF y MOBI, instala Calibre y ajusta la ruta de `ebook-convert.exe` a tu instalación:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\preparar-formatos.ps1 -EbookConvert "C:\Program Files\Calibre2\ebook-convert.exe"
. .\.local.env.ps1
.\mvnw.cmd -B package
java -jar .\target\BibliotecaProyecto-1.0.0.jar --spring.profiles.active=importar-formatos --server.port=0
```

El script convierte las ediciones EPUB verificadas, guarda los archivos en `storage/multiformato` y actualiza `catalogo/formatos-adicionales.json`. El perfil registra sus formatos y termina al completar la importación. Mantén los archivos en almacenamiento persistente. Detén cualquier ejecución que use el JAR de `target` antes de volver a empaquetarlo.

Las ediciones conservan su licencia y procedencia. Project Gutenberg las clasifica como dominio público en Estados Unidos; consulta las condiciones aplicables en tu país antes de redistribuirlas.

## Patrones de comportamiento en funcionamiento

- **Iterator:** recorre una colección y sus subcolecciones para mostrar libros paginados sin duplicados.
- **Mediator:** coordina el aviso al primer usuario en espera cuando una devolución o cancelación deja cupo.
- **Memento:** permite deshacer hasta diez cambios descriptivos de una ficha. No revierte archivos, ISBN, licencias ni préstamos.
- **Observer:** comunica los eventos de circulación a los componentes de notificación y auditoría.
- **State:** representa préstamos vigentes, vencidos y devueltos y sus operaciones permitidas.
- **Visitor:** genera el resumen del catálogo, sus formatos y los libros sin archivo disponible.

## Pruebas

Para ejecutar las pruebas habituales:

```powershell
.\mvnw.cmd test
```

Para incluir la integración con MariaDB real, inicia MariaDB en XAMPP:

```powershell
. .\.local.env.ps1
$env:BIBLIOTECA_DB_TEST='true'
.\mvnw.cmd verify
Remove-Item Env:BIBLIOTECA_DB_TEST
```

Las pruebas aíslan sus datos temporales; las migraciones del esquema permanecen aplicadas. Antes de ejecutar `clean package`, detén la aplicación. No elimines `storage`, `.local.env.ps1`, las migraciones ni `.mvn`.

## Configuración de despliegue

La aplicación lee la conexión, `BOOK_STORAGE_PATH`, `PORT` y las credenciales iniciales desde variables de entorno. Usa almacenamiento persistente compartido si ejecutas varias instancias. Las sesiones se guardan en MariaDB y el estado se consulta en `/actuator/health`.

El objetivo de más de 100.000 usuarios sigue siendo una meta de capacidad: requiere dimensionar infraestructura y realizar pruebas de carga representativas antes de afirmarlo como rendimiento alcanzado. Para un despliegue público, configura HTTPS y protege el acceso administrativo a MariaDB/phpMyAdmin.
