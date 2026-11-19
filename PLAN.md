# PLAN — Naim Pro

Bu dosya, projenin aşamalarını ve alınan kararların gerekçelerini kaydeder.
Tam otonom çalışma modunda her karar buraya not düşülür.

## Hedef

"Ezan Vakti Pro" tarzı bir namaz vakti uygulamasının; tamamen ücretsiz, reklamsız,
izleyicisiz, açık kaynak ve orijinalinden daha optimize bir sürümünü sıfırdan yazmak.

## Kısıtlar

| Kural | Uygulama şekli |
|---|---|
| Reklam / analitik / crash-reporting yok | Bağımlılık listesinde hiçbiri yok; `dependencies` bloğu denetlenebilir |
| Google Play Services yok (F-Droid uyumlu) | androidx dışında Google bağımlılığı yok |
| GPL-3.0 | `LICENSE`, başlık bildirimleri, README |
| Offline-first | Şehir verisi gömülü, vakitler cihazda hesaplanır, internet izni isteğe bağlı |
| Yalnızca yaklaşık konum | `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` yok |
| Özgün görsel/içerik | İkon ve widget'lar bu proje için üretildi; sesler sentezlendi |
| Her dış kaynak belgelenir | `ATTRIBUTION.md` |

## Aşama durumu

| # | Aşama | Durum |
|---|---|---|
| 1 | Depo, iskelet, tema, navigasyon, CI | ✅ |
| 2 | Vakit hesaplama motoru + testler | ✅ |
| 3 | Konum/şehir verisi + ana ekran | ✅ |
| 4 | Bildirim/alarm altyapısı + ezan | ✅ |
| 5 | Widget, kıble, imsakiye, takvim | ✅ |
| 6 | Takip, zikirmatik, içerikler, yedekleme | ✅ |
| 7 | Optimizasyon, erişilebilirlik, dokümantasyon | ✅ |
| 8 | Release workflow, keystore/secret, v1.0.0 etiketi, Actions doğrulaması | ✅ |

## Alınan kararlar

### Mimari

- **Manuel DI (`AppContainer`) yerine Hilt değil.** Gerekçe: tek modüllü bir
  uygulamada Hilt'in eklediği ~500 KB ve derleme süresi maliyeti, sağladığı
  faydayı aşmıyor. `AppContainer` sade, test edilebilir ve hızlı.
- **Vakit hesaplama sıfırdan yazıldı.** Gerekçe: `adhan-kotlin` Diyanet'in
  "temkin" düzeltmelerini içermiyor ve Diyanet ile ±1 dk hedefini tutturamıyor.
  Kendi motorumuz `SolarPosition` (NOAA algoritması) + `PrayerTimeCalculator`
  üzerine kurulu ve Diyanet'e kalibre edildi.
- **Hicri takvim tablo değil, aritmetik.** Gerekçe: 1900–2100 aralığında tablo
  yükü yerine `HijriCalendar` ile dönüşüm yapılıyor; kullanıcı ±2 gün kaydırma
  ayarına sahip (yerel hilal gözlemi farkları için).

### Performans

- Vakitler 30 günlük önbelleğe alınır (`PrayerRepository`); ana thread'de ağır iş yok.
- WorkManager varsayılan başlatıcısı kaldırıldı (`tools:node="remove"`), istek
  üzerine başlatılıyor — soğuk açılışta gereksiz iş yok.
- `resourceConfigurations` yalnızca `tr`, `en`, `ar` — APK'da kullanılmayan
  dil kaynakları yok.
- ABI split açık (`armeabi-v7a`, `arm64-v8a`, `x86_64`).
- R8 full mode + resource shrinking + `proguard-rules.pro`.

### Hesaplama doğruluğu

- Diyanet karşılaştırması: 10 şehir × 32 gün = 320 örnek.
- Bulunan ve düzeltilen kritik hata: güneş öğlesi iki kez ekleniyordu (vakitler
  ~6 saat kayıyordu). Ayrıca kutup enlemlerinde sıralama bozulmasına karşı
  `orderSafely()` eklendi.
- Kabul ölçütü: maksimum sapma ≤ 1 dk, ortalama ≤ 30 sn.

### Bildirimler

- `AlarmManager.setExactAndAllowWhileIdle` birincil, `WorkManager` yedek.
- `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`, `MY_PACKAGE_REPLACED`,
  `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` → alarmlar yeniden kurulur.
- Sürekli arka plan servisi **yok**; wake lock yalnızca ezan çalarken kısa süre.

### Statik analiz

- `ktlint`: `standard:function-naming` ve `standard:max-line-length` kapalı
  (Compose PascalCase ve 140 karakter sınırı `.editorconfig`/detekt tarafından
  yönetiliyor).
- `detekt`: `@Composable` fonksiyonları `LongMethod`/`CyclomaticComplexMethod`
  kurallarından muaf — Compose ağacını yapay olarak parçalamak okunabilirliği
  bozuyor.

### Yayın

- **Lint baseline dosyası depoda tutulur** (`app/lint-baseline.xml`). Gerekçe:
  `lintVitalRelease` baseline tanımlıysa ama dosya yoksa baseline'ı yazıp
  derlemeyi bilinçli olarak durduruyor. Dosya sürüm kontrolünde olduğu için bu
  durum tekrar oluşmuyor.
- **ABI split'li APK'lar ve AAB ayrı Gradle çağrılarında üretilir.** Gerekçe:
  AGP, aynı çalıştırmada hem çoklu APK hem App Bundle istendiğinde
  `Multiple shrunk-resources files found` hatası veriyor. `bundleRelease`
  çağrıldığında `splits.abi` otomatik kapatılıyor.
- **`reportApkSize` görevi tip tabanlıdır** (`DefaultTask` + `DirectoryProperty`).
  Gerekçe: `doLast` içinde `Project`/script referansı kullanmak configuration
  cache ile uyumsuzdu; artık ölçüm görevi cache'lenebilir.

## Bilinen sınırlar

- Ezan kaydı gömülü değildir (lisansı uygun kayıt bulunamadı). Kısa uyarı sesi
  ve kullanıcının kendi dosyasını seçme desteği vardır.
- Diyanet ile çevrim içi karşılaştırma varsayılan kapalıdır ve ağ olmadan çalışmaz.
- Emülatörde Doze/alarm doğrulaması, GitHub Actions'ta emülatör çalıştırma
  maliyeti nedeniyle yerel doğrulama ile sınırlıdır (bkz. `docs/TEST-REPORT.md`).
