$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location -LiteralPath $projectRoot

if (-not (Test-Path -LiteralPath '.local.env.ps1')) {
    & 'C:\xampp\php\php.exe' 'scripts\setup-xampp.php'
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo configurar MariaDB de XAMPP.' }
}

. '.\.local.env.ps1'
$env:SPRING_THYMELEAF_CACHE = 'false'
& '.\mvnw.cmd' 'spring-boot:run'
