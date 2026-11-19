Test Raporu — Naim Pro
======================

Tarih: 19 Kasım 2026

## 1. Birim testleri

`./gradlew testDebugUnitTest` — **73 test, 0 hata, 0 atlanan.**

| Test sınıfı | Test sayısı | Kapsam |
|---|---|---|
| `DiyanetComparisonTest` | 20 | 10 şehir × 32 gün resmî Diyanet vakitleriyle karşılaştırma. Maksimum sapma ≤ 1 dk, ortalama ≤ 30 sn. |
| `PrayerTimeEdgeCaseTest` | 11 | Yaz saati geçişleri, kutup gecesi, gece yarısı güneşi, güney yarımküre, tüm hesap yöntemleri, Hanefî ikindi, yatsı aralığı |
| `HijriCalendarTest` | 14 | Hicri↔miladi dönüşüm, ay uzunlukları, kaydırma (offset) |
| `QiblaCalculatorTest` | 11 | 6 şehir için kıble açısı, Kâbe mesafesi, normalizasyon, tolerans |
| `PrayerTimesTest` | 9 | Model doğrulaması, sıralama, sonraki vakit seçimi |
| `ContentLibraryTest` | 5 | Gömülü içeriklerin bütünlüğü (boş kayıt yok, kaynak alanı dolu) |
| `CityCatalogAssetTest` | 3 | Şehir kataloğu bütünlüğü (≥ 19.000 satır, Ankara) ve gzip algılama |

### Diyanet karşılaştırması

Referans: `app/src/test/resources/diyanet-official-snapshot.csv`
(320 satır = 10 şehir × 32 gün, Diyanet'in yayımladığı resmî vakitler).

Kabul ölçütü: **her vakit için |sapma| ≤ 1 dakika**.

## 2. Statik analiz

| Araç | Sonuç |
|---|---|
| `ktlintCheck` | ✅ İhlal yok |
| `detekt` (maxIssues=0) | ✅ İhlal yok |
| `lintVitalRelease` | ✅ Hata/uyarı yok |

Detekt yapılandırması Compose ekranlarını `LongMethod`/`CyclomaticComplexMethod`
kurallarından muaf tutar; gerekçe `config/detekt/detekt.yml` ve `PLAN.md`
içinde belgelenmiştir.

## 3. Derleme

| Görev | Sonuç |
|---|---|
| `assembleDebug` | ✅ Başarılı |
| `assembleRelease` | ✅ Başarılı (R8 full mode + resource shrinking) |
| `bundleRelease` | ✅ Başarılı (AAB) |
| İmza doğrulama | ✅ APK Signature Scheme v1 + v2 + v3, RSA 4096 |

## 4. Düzeltilen kritik hatalar

1. **Güneş öğlesinin iki kez eklenmesi.** `PrayerTimeCalculator` içinde öğle
   vakti hesabında güneş öğlesi iki kez toplanıyordu; tüm vakitler ~6 saat
   kayıyordu. Diyanet karşılaştırma testi bu hatayı yakaladı.
2. **Kutup enlemlerinde sıralama bozulması.** Gece yarısı güneşi / kutup
   gecesi durumlarında vakitler sırasız çıkabiliyordu; `orderSafely()` eklendi.
3. **Açılışta çökme (1.0.0).** Paketleme sırasında `assets/cities.csv.gz`
   dosyası `assets/cities.csv` adıyla açılıyordu; yükleyici `.gz` adını
   aradığı için uygulama `FileNotFoundException` ile kapanıyordu. Yükleyici
   artık iki adı da dener ve gzip olup olmadığını içerikten algılar. Hata
   yalnızca GitHub Actions'ta üretilen APK'da görüldüğü için emülatör tabanlı
   açılış testi CI'ya eklendi.

## 3a. Emülatör açılış testi

`.github/workflows/smoke.yml` her `main` push'unda release (R8) APK'yi
API 34 x86_64 emülatörde kurar, `MainActivity`'yi açar ve `logcat -b crash`
çıktısında uygulamaya ait kayıt arar. Süreç ölmüşse veya crash buffer'da
`com.naimpro.app` varsa iş başarısız olur.

## 5. Alarm / Doze doğrulaması

Alarmlar `AlarmManager.setExactAndAllowWhileIdle` ile kurulur, `WorkManager`
yedek görevi olarak çalışır. Yeniden başlatma, saat/zaman dilimi değişimi ve
uygulama güncellemesinde `BootCompletedReceiver` alarmları yeniden kurar.

Yerel emülatörde doğrulanan senaryolar:

| Senaryo | Beklenen | Sonuç |
|---|---|---|
| Cihazı yeniden başlatma | Alarmlar yeniden kurulur | ✅ |
| Saat dilimi değişimi | Alarmlar yeni saate göre kurulur | ✅ |
| Doze moduna girme | `setExactAndAllowWhileIdle` alarmı tetiklenir | ✅ |
| Uygulama güncellemesi | `MY_PACKAGE_REPLACED` ile alarmlar yenilenir | ✅ |
| Bildirim izni verilmemesi | Alarm kurulmaz, uygulama çalışmaya devam eder | ✅ |

Not: Doze doğrulaması emülatörde `adb shell dumpsys deviceidle force-idle`
ile yapılmıştır. Açılış (crash) testi GitHub Actions'ta çalışır; Doze ve
yeniden başlatma senaryoları süre maliyeti nedeniyle yalnızca yerelde
doğrulanmıştır.

## 6. Optimizasyon ölçümleri

| Ölçüt | Hedef | Ölçülen | Durum |
|---|---|---|---|
| Release APK boyutu | < 8 MB | arm64-v8a **3.036 KiB ≈ 2,97 MB**, armeabi-v7a 3.035 KiB, x86_64 3.036 KiB (R8 + resource shrinking + ABI split) | ✅ |
| Arka planda sürekli servis | Yok | Yalnızca `AlarmManager` + `WorkManager` | ✅ |
| Wake lock | Yalnızca ezan sırasında kısa süre | ✅ | ✅ |
| Vakit önbelleği | 30 gün | ✅ `PrayerRepository` | ✅ |
| Ana thread'de ağır iş | Yok | ✅ Debug'da StrictMode açık | ✅ |
| Lint (vital) | Hatasız | `lintVitalRelease`: hata/uyarı yok | ✅ |

## 7. Tekrarlama

```bash
./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug
```
