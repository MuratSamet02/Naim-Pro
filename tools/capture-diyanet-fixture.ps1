# Captures an official Diyanet prayer time snapshot used as a unit-test fixture.
# Source: https://ezanvakti.emushaf.net (the public API backing Diyanet's own app).
#
# Usage: pwsh tools/capture-diyanet-fixture.ps1
$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$out = Join-Path $repoRoot "app\src\test\resources\diyanet-official-snapshot.csv"
New-Item -ItemType Directory -Force -Path (Split-Path $out) | Out-Null

$cities = @(
    @{ n = "Ankara";    id = "9206" },
    @{ n = "Antalya";   id = "9225" },
    @{ n = "Diyarbakir";id = "9402" },
    @{ n = "Hakkari";   id = "9507" },
    @{ n = "Istanbul";  id = "9541" },
    @{ n = "Izmir";     id = "9560" },
    @{ n = "Konya";     id = "9676" },
    @{ n = "Samsun";    id = "9819" },
    @{ n = "Trabzon";   id = "9905" },
    @{ n = "Van";       id = "9930" }
)

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("city,date,imsak,gunes,ogle,ikindi,aksam,yatsi")
foreach ($c in $cities) {
    $data = Invoke-RestMethod -Uri "https://ezanvakti.emushaf.net/vakitler/$($c.id)" -TimeoutSec 60
    foreach ($d in $data) {
        $parts = $d.MiladiTarihKisa -split '\.'
        $iso = "$($parts[2])-$($parts[1])-$($parts[0])"
        $lines.Add("$($c.n),$iso,$($d.Imsak),$($d.Gunes),$($d.Ogle),$($d.Ikindi),$($d.Aksam),$($d.Yatsi)")
    }
}
$utf8 = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($out, ($lines -join "`n") + "`n", $utf8)
Write-Host "wrote $($lines.Count - 1) rows to $out"
