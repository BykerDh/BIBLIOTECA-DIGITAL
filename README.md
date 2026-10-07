# Biblioteca Digital

Aplicacion Spring MVC para catalogo, lectura de PDF/EPUB/MOBI, prestamos, reservas, recomendaciones y suscripciones. Incluye los patrones Singleton, Factory Method, Abstract Factory, Prototype, Builder, Adapter, Bridge, Composite, Decorator y Facade.

## Inicio local con XAMPP

1. Inicia Apache y MariaDB en el panel de XAMPP.
2. Abre PowerShell en esta carpeta y ejecuta `./run-local.ps1`.
3. Abre `http://localhost:8080` y phpMyAdmin en `http://localhost/phpmyadmin`.
4. La primera ejecucion crea la base `biblioteca_digital`, una cuenta SQL exclusiva de la aplicacion y `.local.env.ps1`. Las migraciones crean las tablas al arrancar.
5. Las credenciales del administrador de la biblioteca estan en `.local.env.ps1`. Ese archivo no se incluye en Git.

Se requiere JDK 25 y XAMPP en `C:\xampp`. Si MariaDB ya usa la cuenta `biblioteca_proyecto_app`, el instalador se detiene para protegerla. Configura entonces `DB_URL`, `DB_USER`, `DB_PASSWORD`, `LIBRARY_ADMIN_EMAIL` y `LIBRARY_ADMIN_PASSWORD` manualmente.

## Configuracion de despliegue

La aplicacion lee `DB_URL`, `DB_USER`, `DB_PASSWORD`, `BOOK_STORAGE_PATH` y las credenciales iniciales de administrador desde variables de entorno. Configura un directorio persistente compartido para `BOOK_STORAGE_PATH` si vas a ejecutar varias instancias. Usa HTTPS y una base MariaDB administrada; no publiques phpMyAdmin directamente en Internet. Las sesiones web se guardan en MariaDB y pueden compartirse entre instancias. El estado se consulta en `/actuator/health`.

Las tablas se versionan en `src/main/resources/db/migration`. Los archivos de libros se guardan en `storage/` fuera de Git. El patron Prototype copia solo metadatos y requiere subir archivos nuevos. Los libros no incluyen contenido precargado; el administrador carga los archivos con licencia de uso.

## Pruebas

`./mvnw.cmd test` ejecuta las pruebas de patrones y reglas de circulacion. La meta de mas de 100.000 usuarios es una meta de capacidad: requiere infraestructura y pruebas de carga antes de afirmarse como rendimiento alcanzado.
