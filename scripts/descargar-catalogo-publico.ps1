$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$root = Split-Path -Parent $PSScriptRoot
$manifest = Get-Content -LiteralPath (Join-Path $root 'catalogo/libros-publicos.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$storage = Join-Path $root 'storage'
if (Test-Path -LiteralPath (Join-Path $root '.local.env.ps1')) { . (Join-Path $root '.local.env.ps1') }
if ($env:BOOK_STORAGE_PATH) {
    $storage = if ([IO.Path]::IsPathRooted($env:BOOK_STORAGE_PATH)) { $env:BOOK_STORAGE_PATH } else { Join-Path $root $env:BOOK_STORAGE_PATH }
}
$downloads = Join-Path $storage 'importacion'
New-Item -ItemType Directory -Path $downloads -Force | Out-Null
$count = 0
foreach ($book in $manifest.libros) {
    $id = [int]$book.gutenbergId
    if ($id -lt 1 -or $book.sha256 -notmatch '^[a-f0-9]{64}$') { throw 'Manifiesto invalido' }
    $expectedUrl = "https://gutenberg.pglaf.org/cache/generated/$id/pg$id-images.epub"
    if ($book.descargaUrl -ne $expectedUrl) { throw 'El manifiesto contiene una fuente no permitida' }
    $file = Join-Path $downloads "$id.epub"
    if (Test-Path -LiteralPath $file) {
        if ((Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() -eq $book.sha256) {
            $count++
            Write-Output "$count/$($manifest.libros.Count) verificado: PG$id"
            continue
        }
        throw "El archivo existente PG$id no coincide. Revisalo antes de reemplazarlo."
    }
    $partial = Join-Path $downloads ([guid]::NewGuid().ToString()+'.partial')
    try {
        # Descargas secuenciales desde el mirror habilitado para uso automatizado.
        Start-Sleep -Seconds 2
        Invoke-WebRequest -Uri $expectedUrl -OutFile $partial -TimeoutSec 120 -UseBasicParsing
        if ((Get-FileHash -LiteralPath $partial -Algorithm SHA256).Hash.ToLowerInvariant() -ne $book.sha256) {
            throw "La edicion PG$id ha cambiado en origen. Revisa y actualiza el manifiesto; no se importara automaticamente."
        }
        Move-Item -LiteralPath $partial -Destination $file
        $count++
        Write-Output "$count/$($manifest.libros.Count) descargado: PG$id"
    } finally {
        if (Test-Path -LiteralPath $partial) { Remove-Item -LiteralPath $partial }
    }
}
Write-Output "EPUB verificados: $count"
