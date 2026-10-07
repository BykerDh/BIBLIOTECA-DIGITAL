<?php
declare(strict_types=1);

$localFile = dirname(__DIR__) . DIRECTORY_SEPARATOR . '.local.env.ps1';
if (is_file($localFile)) {
    echo "La configuracion local ya existe.\n";
    exit(0);
}

$configFile = 'C:/xampp/phpMyAdmin/config.inc.php';
if (!is_file($configFile)) {
    fwrite(STDERR, "No se encontro la configuracion de phpMyAdmin de XAMPP.\n");
    exit(1);
}

$cfg = [];
require $configFile;
$server = $cfg['Servers'][1];
$admin = new mysqli($server['host'], $server['user'], $server['password']);
$admin->set_charset('utf8mb4');

$database = 'biblioteca_digital';
$user = 'biblioteca_proyecto_app';
$exists = $admin->query("SELECT User FROM mysql.user WHERE User = 'biblioteca_proyecto_app' AND Host = '127.0.0.1'");
if ($exists->num_rows > 0) {
    fwrite(STDERR, "Ya existe la cuenta de base de datos. Configurala manualmente antes de continuar.\n");
    exit(1);
}

$dbPassword = bin2hex(random_bytes(24));
$adminPassword = bin2hex(random_bytes(16));
$admin->query("CREATE DATABASE IF NOT EXISTS `$database` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
$admin->query("CREATE USER '$user'@'127.0.0.1' IDENTIFIED BY '$dbPassword'");
$admin->query("GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES ON `$database`.* TO '$user'@'127.0.0.1'");

$content = "\$env:DB_URL='jdbc:mariadb://127.0.0.1:3306/biblioteca_digital'\r\n"
    . "\$env:DB_USER='$user'\r\n"
    . "\$env:DB_PASSWORD='$dbPassword'\r\n"
    . "\$env:LIBRARY_ADMIN_EMAIL='admin@biblioteca.local'\r\n"
    . "\$env:LIBRARY_ADMIN_PASSWORD='$adminPassword'\r\n";
if (file_put_contents($localFile, $content, LOCK_EX) === false) {
    fwrite(STDERR, "Cuenta creada, pero no se pudo guardar la configuracion local.\n");
    exit(1);
}
echo "Base de datos y cuenta de aplicacion creadas. Configuracion guardada en .local.env.ps1.\n";
