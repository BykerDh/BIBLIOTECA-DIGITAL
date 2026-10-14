param([switch]$SinDescargar)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Push-Location $root
try {
    if (-not (Test-Path -LiteralPath '.local.env.ps1')) { throw 'Ejecuta primero run-local.ps1 para configurar MariaDB.' }
    . .\.local.env.ps1
    if (-not $SinDescargar) { & (Join-Path $PSScriptRoot 'descargar-catalogo-publico.ps1') }
    & .\mvnw.cmd -B package
    if ($LASTEXITCODE -ne 0) { throw 'La compilacion o las pruebas fallaron.' }
    # El perfil importa y termina. Puerto automatico para no ocupar el de la biblioteca.
    & java -jar .\target\BibliotecaProyecto-1.0.0.jar --spring.profiles.active=importar-catalogo --server.port=0
    if ($LASTEXITCODE -ne 0) { throw 'La importacion fallo. Los libros ya confirmados no se duplicaran al reintentar.' }
} finally { Pop-Location }
