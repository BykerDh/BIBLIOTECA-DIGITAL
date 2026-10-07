<?php
declare(strict_types=1);

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

$url = getenv('DB_URL') ?: '';
if (!preg_match('~^jdbc:mariadb://([^:/?]+)(?::([0-9]+))?/([^?]+)~', $url, $parts)) {
    fwrite(STDERR, "DB_URL no es una direccion MariaDB valida.\n");
    exit(1);
}

$email = strtolower(trim(getenv('LIBRARY_ADMIN_EMAIL') ?: ''));
$password = getenv('NEW_ADMIN_PASSWORD') ?: '';
if ($email === '' || strlen($password) < 12 || strlen($password) > 72) {
    fwrite(STDERR, "El correo debe existir y la clave debe tener entre 12 y 72 bytes.\n");
    exit(1);
}

try {
    $db = new mysqli($parts[1], getenv('DB_USER'), getenv('DB_PASSWORD'), $parts[3], (int) ($parts[2] ?: 3306));
    $db->set_charset('utf8mb4');
    $check = $db->prepare("SELECT id FROM usuarios WHERE email = ? AND rol = 'ADMIN' AND activo = TRUE");
    $check->bind_param('s', $email);
    $check->execute();
    $check->store_result();
    if ($check->num_rows !== 1) {
        throw new RuntimeException('No se encontro una cuenta administradora activa con ese correo.');
    }

    $hash = password_hash($password, PASSWORD_BCRYPT);
    $update = $db->prepare("UPDATE usuarios SET password_hash = ? WHERE email = ? AND rol = 'ADMIN' AND activo = TRUE");
    $update->bind_param('ss', $hash, $email);
    $update->execute();
    if ($update->affected_rows !== 1) {
        throw new RuntimeException('No se pudo actualizar la cuenta administradora.');
    }
    echo "Clave actualizada en MariaDB.\n";
} catch (Throwable $error) {
    fwrite(STDERR, $error->getMessage() . "\n");
    exit(1);
}
