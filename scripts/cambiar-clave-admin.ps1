$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$envFile = Join-Path $projectRoot '.local.env.ps1'
$phpFile = Join-Path $PSScriptRoot 'cambiar-clave-admin.php'
$phpExe = 'C:\xampp\php\php.exe'

if (-not (Test-Path -LiteralPath $envFile)) { throw 'No existe .local.env.ps1. Inicia la aplicacion una vez primero.' }
if (-not (Test-Path -LiteralPath $phpExe)) { throw 'No se encontro PHP de XAMPP.' }
. $envFile

$newSecure = Read-Host 'Nueva clave del administrador (minimo 12 caracteres)' -AsSecureString
$confirmSecure = Read-Host 'Repite la nueva clave' -AsSecureString
$newPassword = [System.Net.NetworkCredential]::new('', $newSecure).Password
$confirmation = [System.Net.NetworkCredential]::new('', $confirmSecure).Password
if ($newPassword -cne $confirmation) { throw 'Las claves no coinciden.' }
if ($newPassword.Length -lt 12 -or [System.Text.Encoding]::UTF8.GetByteCount($newPassword) -gt 72) {
    throw 'La clave debe tener al menos 12 caracteres y no superar 72 bytes.'
}

$env:NEW_ADMIN_PASSWORD = $newPassword
try {
    & $phpExe $phpFile
    if ($LASTEXITCODE -ne 0) { throw 'No se actualizo la clave en MariaDB.' }

    $current = Get-Content -LiteralPath $envFile -Raw
    $pattern = '(?m)^\$env:LIBRARY_ADMIN_PASSWORD=.*$'
    if (-not [regex]::IsMatch($current, $pattern)) {
        throw 'La clave cambio en MariaDB, pero falta LIBRARY_ADMIN_PASSWORD en .local.env.ps1.'
    }
    $line = '$env:LIBRARY_ADMIN_PASSWORD=' + "'" + $newPassword.Replace("'", "''") + "'"
    $updated = [regex]::Replace($current, $pattern, [System.Text.RegularExpressions.MatchEvaluator]{ param($match) $line })
    [System.IO.File]::WriteAllText($envFile, $updated, [System.Text.UTF8Encoding]::new($false))
    Write-Output 'Listo. Cierra sesion en el navegador e ingresa con la nueva clave.'
} finally {
    Remove-Item Env:NEW_ADMIN_PASSWORD -ErrorAction SilentlyContinue
    $newPassword = $null
    $confirmation = $null
}
