param([Parameter(Mandatory=$true)][string]$EbookConvert,[string]$StoragePath)
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
if (!(Test-Path -LiteralPath $EbookConvert -PathType Leaf)) { throw 'Indica la ruta de ebook-convert.exe de Calibre.' }
if (!$StoragePath) {
    if (Test-Path -LiteralPath (Join-Path $root '.local.env.ps1')) { . (Join-Path $root '.local.env.ps1') }
    $StoragePath=if($env:BOOK_STORAGE_PATH){$env:BOOK_STORAGE_PATH}else{Join-Path $root 'storage'}
}
if (![IO.Path]::IsPathRooted($StoragePath)) {$StoragePath=Join-Path $root $StoragePath}
$StoragePath=[IO.Path]::GetFullPath($StoragePath)
$salida=Join-Path $StoragePath 'multiformato'
New-Item -ItemType Directory -Path $salida -Force | Out-Null
$libros=(Get-Content -LiteralPath (Join-Path $root 'catalogo/libros-publicos.json') -Raw -Encoding UTF8 | ConvertFrom-Json).libros | Select-Object -First 20
$entradas=@()
foreach($libro in $libros) {
    $id=[int]$libro.gutenbergId
    $epub=Join-Path $StoragePath "importacion/$id.epub"
    if (!(Test-Path -LiteralPath $epub) -or (Get-FileHash -LiteralPath $epub -Algorithm SHA256).Hash.ToLowerInvariant() -ne $libro.sha256) {throw "Falta la edicion original verificada de PG$id"}
    foreach($formato in @('pdf','mobi')) {
        $archivo=Join-Path $salida "$id.$formato"
        $registro=Join-Path $salida "$id-$formato.log"
        if (!(Test-Path -LiteralPath $archivo)) {
            $opciones=if($formato -eq 'pdf') {@('--paper-size','a5','--pdf-default-font-size','12','--pdf-page-numbers')}else{@('--mobi-file-type','old','--output-profile','kindle')}
            & $EbookConvert $epub $archivo @opciones *> $registro
            if($LASTEXITCODE -ne 0) {throw "Fallo al convertir PG$id a $formato. Revisa $registro"}
        }
        $entradas+=@{gutenbergId=$id;formato=$formato.ToUpperInvariant();archivo="$id.$formato";sha256=(Get-FileHash -LiteralPath $archivo -Algorithm SHA256).Hash.ToLowerInvariant();epubSha256=$libro.sha256}
        Write-Output "PG$id preparado: $formato"
    }
}
@{archivos=$entradas} | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $root 'catalogo/formatos-adicionales.json') -Encoding UTF8
Write-Output '20 libros preparados. Importa con el perfil importar-formatos.'
