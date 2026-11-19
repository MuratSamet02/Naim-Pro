# Derleme Rehberi

## Gereksinimler

| Araç | Sürüm |
|---|---|
| JDK | 17 |
| Android SDK Platform | 36 |
| Android Build Tools | 36.0.0 |
| Gradle | wrapper ile gelir (bkz. `gradle/wrapper/gradle-wrapper.properties`) |

## Derleme nerede yapılır?

Sürüm derlemeleri **GitHub Actions üzerinde** yapılır; depoda üretilmiş APK/AAB
tutulmaz. `v*` etiketi itildiğinde `.github/workflows/release.yml` imzalı APK,
AAB ve GitHub Release'i otomatik üretir. Kalite kontrolleri her `main` push'unda
`.github/workflows/ci.yml` ile çalışır.

Yerelde yalnızca geliştirme sırasında `assembleDebug` / test çalıştırmak yeterlidir.

`local.properties` dosyası depoda tutulmaz:

```properties
# Windows
sdk.dir=C\:\\Users\\<kullanıcı>\\Android\\Sdk

# Linux / macOS
sdk.dir=/home/<kullanıcı>/Android/Sdk
```

## Görevler

```bash
# Hızlı geliştirme derlemesi
./gradlew assembleDebug

# Sürüm derlemesi (imzasızsa debug anahtarıyla imzalanır)
./gradlew assembleRelease

# Android App Bundle (AAB) — ayrı bir çağrıda çalıştırılmalıdır
./gradlew bundleRelease

# APK boyut raporu
./gradlew reportApkSize

# Testler
./gradlew testDebugUnitTest

# Statik analiz
./gradlew ktlintCheck detekt lintDebug

# Hepsi
./gradlew check
```

## İmzalama

İmzalama tamamen ortam değişkenleriyle yapılandırılır; depoda sır tutulmaz.

| Değişken | Açıklama |
|---|---|
| `KEYSTORE_FILE` | Keystore dosyasının yolu |
| `KEYSTORE_PASSWORD` | Keystore şifresi |
| `KEY_ALIAS` | Anahtar takma adı |
| `KEY_PASSWORD` | Anahtar şifresi |

Değişkenler tanımlı değilse (yerel derleme, fork, PR) sürüm derlemesi **debug
anahtarıyla** imzalanır ve yine de APK üretir — CI hiçbir zaman bu yüzden kırılmaz.

### Yeni keystore üretmek

```bash
keytool -genkeypair -v \
  -keystore naim-pro-release.jks \
  -alias naim-pro \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -storetype PKCS12
```

Üretilen dosyayı **asla** depoya eklemeyin (`.gitignore` içinde `*.jks` ve
`*.keystore` vardır). Yedeğini depo dışında güvenli bir yerde tutun; kaybederseniz
aynı paket adıyla güncelleme yayımlayamazsınız.

### GitHub Actions sırları

```bash
base64 -w0 naim-pro-release.jks > keystore.b64
gh secret set ANDROID_KEYSTORE_BASE64 < keystore.b64
gh secret set KEYSTORE_PASSWORD
gh secret set KEY_ALIAS
gh secret set KEY_PASSWORD
```

## Compose derleyici metrikleri

Yeniden hesaplama (recomposition) analizi için:

```bash
./gradlew assembleRelease -Pnaim.composeMetrics=true
```

Raporlar: `app/build/compose-reports/` ve `app/build/compose-metrics/`.

## Şehir verisini yenilemek

`app/src/main/assets/cities.csv.gz` GeoNames dökümlerinden üretilir:

```powershell
pwsh tools/build-city-db.ps1
```

Kaynak: <https://download.geonames.org/export/dump/> (`cities15000.zip`,
`admin1CodesASCII.txt`, `countryInfo.txt`). Lisans: CC BY 4.0.

## Bildirim seslerini yenilemek

```powershell
pwsh tools/generate-tones.ps1
```

Sesler sıfırdan sentezlenir; dış kayıt kullanılmaz.
