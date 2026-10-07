$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
. (Join-Path $root '.local.env.ps1')
$base = 'http://localhost:8080'
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$login = Invoke-WebRequest -Uri "$base/login" -WebSession $session -UseBasicParsing
$match = [regex]::Match($login.Content, 'name="_csrf" value="([^"]+)"')
if (-not $match.Success) { throw 'El formulario de acceso no contiene CSRF.' }

$body = @{ username = $env:LIBRARY_ADMIN_EMAIL; password = $env:LIBRARY_ADMIN_PASSWORD; _csrf = $match.Groups[1].Value }
$response = Invoke-WebRequest -Uri "$base/login" -Method Post -Body $body -WebSession $session -UseBasicParsing
if ($response.Content -notmatch 'Hola, Administrador') { throw 'No se pudo iniciar sesion como administrador.' }

$paths = @('/', '/libros', '/mi-cuenta', '/planes', '/recomendaciones', '/colecciones', '/admin/libros/nuevo', '/admin/colecciones')
foreach ($path in $paths) {
    $page = Invoke-WebRequest -Uri "$base$path" -WebSession $session -UseBasicParsing
    if ($page.StatusCode -ne 200 -or $page.Content -match 'Whitelabel Error Page') { throw "Fallo la pagina $path" }
    Write-Output "OK $path"
}
