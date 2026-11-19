# Generates the original notification tones shipped with Naim Pro.
# The waveforms are synthesised from scratch by this script, so they are
# original works released under the same GPL-3.0 licence as the application.
#
# Usage: pwsh tools/generate-tones.ps1
$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $repoRoot "app\src\main\res\raw"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$sampleRate = 22050

function New-Wav([string]$path, [double]$duration, [double[]]$notes) {
    $total = [int]($duration * $sampleRate)
    $samples = New-Object 'double[]' $total

    $noteDuration = $duration / [Math]::Max($notes.Count, 1)
    for ($n = 0; $n -lt $notes.Count; $n++) {
        $freq = $notes[$n]
        $start = [int]($n * $noteDuration * $sampleRate)
        $length = [int]($noteDuration * 1.7 * $sampleRate)
        for ($i = 0; $i -lt $length; $i++) {
            $idx = $start + $i
            if ($idx -ge $total) { break }
            $t = $i / $sampleRate
            $envelope = [Math]::Exp(-3.0 * $t / $noteDuration)
            $attack = [Math]::Min(1.0, $i / ($sampleRate * 0.012))
            $value = 0.55 * [Math]::Sin(2 * [Math]::PI * $freq * $t) +
                     0.22 * [Math]::Sin(2 * [Math]::PI * $freq * 2.002 * $t) +
                     0.10 * [Math]::Sin(2 * [Math]::PI * $freq * 3.004 * $t) +
                     0.06 * [Math]::Sin(2 * [Math]::PI * $freq * 4.01 * $t)
            $samples[$idx] += ($value * $envelope * $attack)
        }
    }

    $peak = 0.0
    foreach ($s in $samples) { if ([Math]::Abs($s) -gt $peak) { $peak = [Math]::Abs($s) } }
    $gain = if ($peak -gt 0) { 0.707 / $peak } else { 1.0 }

    $dataBytes = $total * 2
    $header = New-Object 'byte[]' 44
    [System.Text.Encoding]::ASCII.GetBytes("RIFF").CopyTo($header, 0)
    [BitConverter]::GetBytes([int](36 + $dataBytes)).CopyTo($header, 4)
    [System.Text.Encoding]::ASCII.GetBytes("WAVE").CopyTo($header, 8)
    [System.Text.Encoding]::ASCII.GetBytes("fmt ").CopyTo($header, 12)
    [BitConverter]::GetBytes([int]16).CopyTo($header, 16)
    [BitConverter]::GetBytes([int16]1).CopyTo($header, 20)
    [BitConverter]::GetBytes([int16]1).CopyTo($header, 22)
    [BitConverter]::GetBytes([int]$sampleRate).CopyTo($header, 24)
    [BitConverter]::GetBytes([int]($sampleRate * 2)).CopyTo($header, 28)
    [BitConverter]::GetBytes([int16]2).CopyTo($header, 32)
    [BitConverter]::GetBytes([int16]16).CopyTo($header, 34)
    [System.Text.Encoding]::ASCII.GetBytes("data").CopyTo($header, 36)
    [BitConverter]::GetBytes([int]$dataBytes).CopyTo($header, 40)

    $pcm = New-Object 'byte[]' $dataBytes
    for ($i = 0; $i -lt $total; $i++) {
        $v = [int]([Math]::Round($samples[$i] * $gain * 32767))
        if ($v -gt 32767) { $v = 32767 }
        if ($v -lt -32768) { $v = -32768 }
        $bytes = [BitConverter]::GetBytes([int16]$v)
        $pcm[$i * 2] = $bytes[0]
        $pcm[$i * 2 + 1] = $bytes[1]
    }

    $stream = [System.IO.File]::Create($path)
    $stream.Write($header, 0, $header.Length)
    $stream.Write($pcm, 0, $pcm.Length)
    $stream.Close()
}

# A warm, meditative five note phrase (D4, F4, A4, C5, A4) with long decays.
New-Wav -path "$outDir\naim_chime.wav" -duration 8.5 -notes @(293.66, 349.23, 440.00, 523.25, 440.00)
# A short two note alert for the "kısa uyarı" notification mode.
New-Wav -path "$outDir\naim_alert.wav" -duration 2.0 -notes @(659.25, 880.00)

Get-ChildItem $outDir | Select-Object Name, Length
