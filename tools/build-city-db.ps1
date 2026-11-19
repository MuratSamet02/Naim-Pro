# Generates the embedded city database for Naim Pro from GeoNames dumps.
# Source: https://download.geonames.org/export/dump/ (CC BY 4.0)
#
# Usage:
#   pwsh tools/build-city-db.ps1 [-GeoDir <path>]
#
# GeoDir must contain: cities15000.txt, TR.txt, admin1CodesASCII.txt, countryInfo.txt
param(
    [string]$GeoDir = (Join-Path $env:USERPROFILE "Downloads\geo")
)

$ErrorActionPreference = "Stop"
$geo = $GeoDir
$repoRoot = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $repoRoot "app\src\main\assets"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

# admin1 names: TR.06 -> Ankara
$admin1 = @{}
Get-Content "$geo\admin1CodesASCII.txt" -Encoding UTF8 | ForEach-Object {
    $p = $_ -split "`t"
    if ($p.Count -ge 2) { $admin1[$p[0]] = $p[1] }
}

# country names
$country = @{}
Get-Content "$geo\countryInfo.txt" -Encoding UTF8 | ForEach-Object {
    if ($_ -notmatch '^#' -and $_.Trim() -ne '') {
        $p = $_ -split "`t"
        if ($p.Count -ge 5) { $country[$p[0]] = $p[4] }
    }
}

$trNames = @{
    'TR' = 'Türkiye'
}
function Get-Country($cc) {
    switch ($cc) {
        'TR' { return 'Türkiye' }
        'CY' { return 'Kıbrıs' }
        'AZ' { return 'Azerbaycan' }
        'DE' { return 'Almanya' }
        'NL' { return 'Hollanda' }
        'FR' { return 'Fransa' }
        'GB' { return 'Birleşik Krallık' }
        'US' { return 'ABD' }
        'AT' { return 'Avusturya' }
        'BE' { return 'Belçika' }
        'CH' { return 'İsviçre' }
        'SE' { return 'İsveç' }
        'DK' { return 'Danimarka' }
        'NO' { return 'Norveç' }
        'IT' { return 'İtalya' }
        'ES' { return 'İspanya' }
        'RU' { return 'Rusya' }
        'SA' { return 'Suudi Arabistan' }
        'EG' { return 'Mısır' }
        'IQ' { return 'Irak' }
        'IR' { return 'İran' }
        'SY' { return 'Suriye' }
        'BG' { return 'Bulgaristan' }
        'GR' { return 'Yunanistan' }
        'MK' { return 'Kuzey Makedonya' }
        'AL' { return 'Arnavutluk' }
        'BA' { return 'Bosna-Hersek' }
        'XK' { return 'Kosova' }
        'GE' { return 'Gürcistan' }
        'KZ' { return 'Kazakistan' }
        'KG' { return 'Kırgızistan' }
        'UZ' { return 'Özbekistan' }
        'TM' { return 'Türkmenistan' }
        'PK' { return 'Pakistan' }
        'IN' { return 'Hindistan' }
        'ID' { return 'Endonezya' }
        'MY' { return 'Malezya' }
        'CA' { return 'Kanada' }
        'AU' { return 'Avustralya' }
        'ZA' { return 'Güney Afrika' }
        'MA' { return 'Fas' }
        'DZ' { return 'Cezayir' }
        'TN' { return 'Tunus' }
        'LY' { return 'Libya' }
        'JO' { return 'Ürdün' }
        'LB' { return 'Lübnan' }
        'KW' { return 'Kuveyt' }
        'QA' { return 'Katar' }
        'AE' { return 'Birleşik Arap Emirlikleri' }
        'BH' { return 'Bahreyn' }
        'OM' { return 'Umman' }
        'YE' { return 'Yemen' }
        'AF' { return 'Afganistan' }
        'BD' { return 'Bangladeş' }
        'CN' { return 'Çin' }
        'JP' { return 'Japonya' }
        'BR' { return 'Brezilya' }
        'AR' { return 'Arjantin' }
        'MX' { return 'Meksika' }
        'UA' { return 'Ukrayna' }
        'RO' { return 'Romanya' }
        'PL' { return 'Polonya' }
        'CZ' { return 'Çekya' }
        'HU' { return 'Macaristan' }
        'PT' { return 'Portekiz' }
        'IE' { return 'İrlanda' }
        'FI' { return 'Finlandiya' }
        'SG' { return 'Singapur' }
        'TH' { return 'Tayland' }
        'PH' { return 'Filipinler' }
        'NG' { return 'Nijerya' }
        'KE' { return 'Kenya' }
        'ET' { return 'Etiyopya' }
        'SD' { return 'Sudan' }
        'SO' { return 'Somali' }
        default { if ($country.ContainsKey($cc)) { return $country[$cc] } else { return $cc } }
    }
}

$rows = New-Object System.Collections.Generic.List[string]

# --- Türkiye: full administrative detail -------------------------------------
$trCodes = @('PPLA', 'PPLA2', 'PPLA3', 'PPLA4', 'PPLC', 'PPL')
Get-Content "$geo\TR.txt" -Encoding UTF8 | ForEach-Object {
    $p = $_ -split "`t"
    if ($p.Count -lt 19) { return }
    if ($p[6] -ne 'P') { return }
    $fc = $p[7]
    if ($trCodes -notcontains $fc) { return }
    $pop = 0; [int]::TryParse($p[14], [ref]$pop) | Out-Null
    if ($fc -eq 'PPL' -and $pop -lt 3000) { return }
    $a1code = "TR.$($p[10])"
    $a1 = $admin1[$a1code]
    if (-not $a1) { $a1 = $p[10] }
    $a1 = (Get-Culture).TextInfo.ToTitleCase($a1.ToLower())
    $rows.Add(("TR|{0}|{1}|{2}|{3}|{4}|{5}|{6}" -f $p[1], $a1, $p[4], $p[5], $p[17], $pop, $fc))
}

# --- World: GeoNames cities15000 --------------------------------------------
$worldCodes = @('PPLA', 'PPLA2', 'PPLA3', 'PPLC', 'PPL')
Get-Content "$geo\cities15000.txt" -Encoding UTF8 | ForEach-Object {
    $p = $_ -split "`t"
    if ($p.Count -lt 19) { return }
    $cc = $p[8]
    if ($cc -eq 'TR') { return }
    $fc = $p[7]
    if ($worldCodes -notcontains $fc) { return }
    $pop = 0; [int]::TryParse($p[14], [ref]$pop) | Out-Null
    # Outside Türkiye we keep settlements that are realistic search targets.
    if ($pop -lt 30000 -and $fc -notin @('PPLC', 'PPLA')) { return }
    $a1 = $admin1["$cc.$($p[10])"]
    if (-not $a1) { $a1 = $p[10] }
    $rows.Add(("{0}|{1}|{2}|{3}|{4}|{5}|{6}|{7}" -f $cc, $p[1], $a1, $p[4], $p[5], $p[17], $pop, $fc))
}

$unique = $rows | Sort-Object -Unique
Write-Host "rows: $($unique.Count)"

$content = "v1`n" + ($unique -join "`n")
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$tmp = "$outDir\cities.csv"
[System.IO.File]::WriteAllText($tmp, $content, $utf8NoBom)

# gzip
$fs = [System.IO.File]::Create("$outDir\cities.csv.gz")
$gz = New-Object System.IO.Compression.GZipStream($fs, [System.IO.Compression.CompressionLevel]::Optimal)
$bytes = $utf8NoBom.GetBytes($content)
$gz.Write($bytes, 0, $bytes.Length)
$gz.Dispose(); $fs.Dispose()
Remove-Item $tmp
Write-Host "raw bytes: $($bytes.Length)"
Write-Host "gz bytes: $((Get-Item "$outDir\cities.csv.gz").Length)"
