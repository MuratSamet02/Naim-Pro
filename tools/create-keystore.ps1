# Generates the Naim Pro release keystore outside the repository and stores the
# backup in the user's home directory. Secrets are never written into the repo.
$ErrorActionPreference = "Stop"

$backupDir = Join-Path $env:USERPROFILE "naim-pro-keystore-yedek"
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null

$keystore = Join-Path $backupDir "naim-pro-release.jks"
$credentials = Join-Path $backupDir "credentials.txt"

if (Test-Path $keystore) {
    Write-Host "Keystore already exists: $keystore"
} else {
    # Cryptographically strong random password (32 chars, no shell-hostile characters).
    $alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"
    $bytes = [byte[]]::new(32)
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $password = -join ($bytes | ForEach-Object { $alphabet[$_ % $alphabet.Length] })

    & keytool -genkeypair -v `
        -keystore $keystore `
        -alias "naim-pro" `
        -keyalg RSA -keysize 4096 -validity 10000 `
        -storetype PKCS12 `
        -storepass $password -keypass $password `
        -dname "CN=Naim Pro, OU=Mobile, O=Naim Pro, L=Istanbul, C=TR"

    @"
Naim Pro release keystore
=========================
File:      $keystore
Alias:     naim-pro
Storepass: $password
Keypass:   $password

Keep this folder private. If you lose the keystore you can never publish an
update for the same application id again.
"@ | Set-Content -Path $credentials -Encoding UTF8

    Write-Host "Keystore created: $keystore"
}

$b64 = Join-Path $backupDir "naim-pro-release.jks.b64"
[Convert]::ToBase64String([IO.File]::ReadAllBytes($keystore)) | Set-Content -Path $b64 -NoNewline -Encoding ASCII

Write-Host "Backup directory: $backupDir"
Write-Host "Base64 for GitHub secret: $b64"
