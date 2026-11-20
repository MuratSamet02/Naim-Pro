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
| Offline-first | Şehir verisi gömülü, vakitler cihazda hesaplanır, hiçbir ağ izni istenmez |
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

---

# PLAN — v2.0 "Devrim" (v1.0.1 → v2.0.0)

> Kaynak şartname: `naim-pro-v2.0-opencode-prompt.md` (kullanıcı tarafından verildi).
> Bu bölüm v2.0 kararlarını, F0 denetimini, başlangıç ölçümlerini ve faz
> durumunu tutar. Çelişki durumunda şartname esastır; burada yalnızca
> uygulama kararları ve gerekçeleri kaydedilir.

## v2.0 hedefi

v1.0.1'i işlev, güvenilirlik, hız ve tasarımda ticari ezan uygulamalarının
önüne geçirmek; reklamsız, izleyicisiz, %100 çevrimdışı ve GPL-3.0 kalarak.
Yedi faz, her faz sonunda `v2.0.0-beta.N` ön sürümü, sonunda `v2.0.0`.

Değişmez kurallar şartnamenin 1. bölümündedir (ağ izni yok, Play Services yok,
analitik yok, yalnızca `ACCESS_COARSE_LOCATION`, GPL-3.0, lisansı belirsiz
kaynak yok, APK < 12 MB, soğuk açılış < 500 ms, keystore değişmez,
mevcut 73 test silinmez/zayıflatılmaz). Bu kurallar her fazın kabul kapısıdır.

## F0 — Denetim sonuçları (20.11.2026)

Şartnamedeki S1–S13 bulgularının tamamı kaynak kodda **doğrulandı**. Kanıtlar:

| # | Bulgu | Doğrulama | Durum |
|---|---|---|---|
| S1 | Onboarding yok | `AppSettings.onboardingCompleted` yalnızca DataStore'da okunup yazılıyor; hiçbir UI/navigasyon kararı kullanmıyor. `AppViewModel.init` konum yoksa `cities.defaultLocation()` (Ankara) kaydediyor. `MainActivity` doğrudan `Destination.HOME` açıyor. | ✅ doğrulandı |
| S2 | Dini gün bildirimi ölü | `PrayerNotifications.showEvent` tanımlı (satır 109) ama **hiçbir çağrısı yok**; `diniGunNotifications` yalnızca `CalendarScreen` anahtarında ve yedek/prefs alanında geçiyor. Bu bildirim için alarm kurulmuyor. | ✅ doğrulandı |
| S3 | Ezan sesi + uzun çalma riski | `res/raw` yalnızca `naim_chime.ogg` (20 KB) ve `naim_alert.ogg` (7,8 KB); ikisi de `tools/generate-tones.ps1` ile sentezlenmiş. `PrayerAlarmReceiver` `goAsync()` + `pendingResult.finish()` ile hemen dönüyor, ses `EzanPlayer` statik `MediaPlayer`'ında **servissiz** çalıyor. `takePersistableUriPermission` yalnızca `SettingsScreen`'de alınıyor (alarm tarafında yeniden doğrulama yok). | ✅ doğrulandı |
| S4 | Kıble sapma düzeltmesi yok | `CompassReading` `getOrientation` azimutunu (manyetik kuzey) veriyor; kod tabanında `GeomagneticField` **hiç yok**. | ✅ doğrulandı |
| S5 | Cami modu ezanı kesebilir | `CamiModeController.enableFor` `INTERRUPTION_FILTER_NONE` kullanıyor, süre `CAMI_MODE_MINUTES = 15` sabit; ezan `USAGE_ALARM` ile çalınıyor. | ✅ doğrulandı |
| S6 | `salahTrackingEnabled` ölü | Ayar modelde/DataStore'da/yedekte var; Ayarlar UI'ında anahtar yok, hiçbir koşul okumuyor. | ✅ doğrulandı |
| S7 | Navigasyon kusurları | `qibla` + `qibla-screen`, `monthly` + `monthly-screen` çift `composable`. `MoreScreen` içinde `onOpenEsma` ve `onOpenContent` ikisi de `content/0` açıyor. Ana ekranda gün gezinme yok (`HomeUiState` yalnızca bugün + yarın). | ✅ doğrulandı |
| S8 | Widget eksikleri | Üç `widget_*_info.xml` dosyasında `updatePeriodMillis="1800000"`; kesin vakit/gece yarısı tetiklemesi yok. `WidgetData.formatDuration` içinde `"${hours}s ${minutes}dk"` / `"${minutes}dk"` **sabit Türkçe**. Widget ayarı ve pin isteği yok. | ✅ doğrulandı |
| S9 | Sabit metinler | `ZikirScreen:198` `if (settings.language.tag == "tr") "Zikret" else "Count"`. | ✅ doğrulandı |
| S10 | Şehir kapsamı | `cities.csv.gz` 430 KB, GeoNames `cities15000` (≥15k nüfus); TR ilçelerinin bir kısmı yok. `CityRepository.search` doğrusal tarama; `normalize()` Türkçe karakterleri sadeleştiriyor ve testleri var. | ✅ doğrulandı |
| S11 | Tek konum | `LocationRepository` tek `selected_location` anahtarı tutuyor; favori/çoklu konum yok. | ✅ doğrulandı |
| S12 | İçerik azlığı | `ContentLibrary` toplam **80** `ContentItem` (30 dua + 25 ayet + 25 hadis). | ✅ doğrulandı |
| S13 | Test/yayın boşlukları | `app/src/androidTest` **yok**; `fastlane/` **yok**; `release.yml` etiketten `versionCode`/`versionName` türetmiyor ve `--prerelease` işaretlemiyor. | ✅ doğrulandı |

Ek olarak doğrulanan tasarım bulguları: özel font yok (sistem varsayılanı),
`ui/theme` dışında ortak tasarım katmanı yok, `dynamicColor` varsayılanı açık.

## F0 — Başlangıç ölçümleri

| Ölçüt | Başlangıç (v1.0.1) | Hedef |
|---|---|---|
| Birim testleri | **73 test / 7 dosya — 0 hata, 0 atlanan** | her fazda yeşil, artan |
| `androidTest` | **0** (klasör yok) | onboarding, alarm, DND, widget |
| ktlint + detekt + assembleDebug | **yeşil** (4 dk 58 sn, cache'li) | her fazda yeşil |
| Debug APK (ABI başına) | **≈ 22,6 MB** × 3 ABI (debug, küçültmesiz) | — |
| Release APK (arm64) | v1.0.1 yayınında **3,20 MB** (F0'da yerel doğrulama) | **< 12 MB** |
| Soğuk açılış | yerelde ölçülemiyor (emülatör yok, `HypervisorPresent=False`) | **< 500 ms** (CI emülatöründe) |
| Jank | ölçülmedi | **< %2** |
| Kaynak | 58 `.kt` / 8.335 satır (main) + 707 satır (test) | — |
| Yerelleştirme | TR 212 / EN 211 / AR 211 string | eksiksiz TR/EN/AR |
| Gömülü veri | `cities.csv.gz` 430 KB; içerik 80 kayıt; font yok | şehir TR tam, içerik ≥ 150/100/100, font ≤ 1,5 MB |
| Room şeması | sürüm 1 (`app/schemas/.../1.json`) | migration ile artırılır |

**Ölçüm yöntemi notu:** Soğuk açılış/jank yalnızca GitHub Actions emülatöründe
ölçülebilir; bu makinede donanım hızlandırma yok. Her beta etiketinde CI süresi
bütçeye eklenir. Release boyutu `./gradlew reportApkSize` ile her fazda ölçülür.

## Faz planı ve kabul kapıları

| Faz | Kapsam | Kapı (bitmeden geçilmez) | Etiket |
|---|---|---|---|
| **F0** | Denetim, bulgu doğrulama, ölçüm, bu bölüm | PLAN.md + yeşil build + commit | — |
| **F1** | A2.1–A2.5 (ses mimarisi, FGS, cami modu, dini gün, ek bildirimler) + A3.1 (declination) | `PrayerAlarmScheduler` birim testleri; cami modu `ALARMS`/`PRIORITY`; dini gün alarmı; kıble sapma testi | `beta.1` |
| **F2** | Paket B (tasarım dili, bileşenler, ana ekran, kıble, takvim, ayarlar, navigasyon) + A1 onboarding + A4.1–A4.3 | çift rota temiz; onboarding akışı + UI testi; SkyPalette kontrast testi; 1.6× taşma yok | `beta.2` |
| **F3** | A3.2–A3.3 (81 il/ilçe, ek vakitler, doğruluk) + C1–C4 (Kur'an, rehber, tesbihat, kaza) | Türkçe arama testleri; Diyanet 81 il × 12 ay ≤ 1 dk; migration | `beta.3` |
| **F4** | C5–C11 (oruç, zekât, gökyüzü/ay, paylaşım, uyandırıcı, kilit, günlük plan) | hesap formülleri birim testli; ay evresi ±1 gün testi; kilit alarm akışını etkilemez | `beta.4` |
| **F5** | Paket D (7 widget, QS, kısayollar, canlı bildirim) + A5 | her vakit kesin widget yenilemesi; widget metinleri TR/EN/AR; konfigürasyon ekranı | `beta.5` |
| **F6** | Paket E (yedek türleri) + A6 (yerelleştirme/erişilebilirlik) | eski yedek içe aktarma testi; sabit metin taraması temiz; TalkBack/48dp/kontrast | `beta.6` |
| **F7** | Paket F (performans, test piramidi) + G (yayın, F-Droid, belgeler) | APK < 12 MB, soğuk açılış < 500 ms, tam test seti, fastlane görselleri, sürüm notları | `v2.0.0` |

## v2.0 uygulama kararları

### Ses ve alarm mimarisi (F1)

- Ezan çalma **kısa ömürlü** `mediaPlayback` foreground service'e taşınır; servis
  ses bitince kendini durdurur. Kalıcı servis yok (kural 9).
- Ses kaynağı önceliği: vakit bazlı kullanıcı dosyası → gömülü ton → bildirim
  kanalı sesi. Özel URI her tetiklemede `takePersistableUriPermission` ile
  doğrulanır; geçersizse varsayılana düşülür ve kullanıcıya bildirilir.
- Lisansı **doğrulanmış** ezan kaydı bulunursa `ATTRIBUTION.md` kaydıyla eklenir;
  bulunamazsa uydurma kayıt eklenmez (mevcut politika korunur).
- Cami modu filtresi `ALARMS` (varsayılan) / `PRIORITY`; süre 5–60 dk; ezan
  başladıktan sonra devreye girer; `dndRestoreAt` boot ve açılışta kontrol edilir.

### Tasarım sistemi (F2)

- `ui/design/{tokens,components}` tek kaynak. `SkyPalette.forPhase(phase, dark)`
  saf Kotlin; kontrast birim testi (≥ 4.5) zorunlu.
- Varsayılan tema "Naim"; Material You isteğe bağlı, **mevcut kullanıcı tercihi
  korunur** (kayıtlı değer varsa dokunulmaz).
- Tüm görseller Compose `Canvas`/vektör; bitmap/harici görsel yok.
- Fontlar yalnızca SIL OFL/benzeri; `pyftsubset` ile alt küme; toplam ≤ 1,5 MB.

### Veri ve migration (F2–F4)

- Çoklu konum için Room tablosu **şema 1 → 2** migration'ı ile eklenir; mevcut
  ayar/veri korunur. Yedek şeması `schemaVersion` ile sürümlenir; v1.0.x yedekleri
  içe aktarılabilir kalır.
- Kaza/oruç/zekât/günlük plan verileri yalnızca cihazda; tam yedek kapsamına girer.

### Yayın hattı (F7)

- `versionCode = MAJOR*1_000_000 + MINOR*10_000 + PATCH*100 + (beta N | 99)`.
- `release.yml`: etiketten sürüm, `-beta`/`-rc` → **pre-release**, SHA-256 dosyası.
- F-Droid `fastlane` meta verisi ve ekran görüntüleri CI emülatöründen üretilir;
  yer tutucu konmaz.

## Ajan orkestrasyonu

Alt ajanlar aynı çalışma dizininde çalışır; çakışmayı önlemek için:

- **Paralel güvenli:** içerik yazımı (ayrı `data/content/*.kt`), şehir verisi
  üretimi, bağımsız ekranlar (oruç/zekât/gökyüzü), widget sınıfları, lisans
  araştırması, bağımsız test dosyaları.
- **Tek ajan (ben):** `app/build.gradle.kts`, `libs.versions.toml`,
  `AndroidManifest.xml`, `strings.xml`, `MainActivity.kt`, `AppSettings.kt`,
  `PLAN.md`, Room şeması/migration, git işlemleri.
- Ajanlar **commit/push etmez**; çıktıyı okur, doğrular, entegre eder, build'i
  ben çalıştırırım. Kalitesiz çıktı düzeltilir.

## Risk kaydı

| Risk | Etki | Azaltma |
|---|---|---|
| Ezan/Kur'an/meal/font lisansı bulunamaması | Özellik daralır | Doğrulanamazsa içe aktarma moduna düş; PLAN + ATTRIBUTION'a yaz |
| FGS + Android 12+ başlatma kısıtları | Ses yarıda kesilir | Kesin alarm istisnası, kanal sesine düşüş, `am kill` testi (CI) |
| 12 MB APK bütçesi | Yayın engeli | Font subset, gzip, ABI split, R8 full mode; her fazda ölçüm |
| Room migration hatası | v1.0.x verisi kaybı | Migration + yedek geri yükleme testleri |
| Emülatör bu makinede çalışmıyor | Cihaz doğrulaması gecikir | Tüm cihaz testleri GitHub Actions emülatöründe |
| Bağlam penceresi dolması | İş sürekliliği | Faz sonu commit/tag + bu bölümdeki durum notu |

## Çalışma protokolü

- **"dur"**: işi derli toplu noktaya getir, build'i yeşile çek, commit + push,
  bu bölüme durum notu yaz, dur. Yarım dosya bırakılmaz.
- **"devam"**: bu bölümdeki durum notu → `git log` → `git status` → build
  kontrolü → kaldığı yerden devam.
- Her mantıksal adım ayrı Conventional Commit; her faz sonunda etiket + push.
- Yeşil olmayan hiçbir şey commit edilmez.

## Durum günlüğü

### Gün 0 — F0 tamamlandı (20.11.2026)

- Faz: **F0 ✅**
- Bitti: S1–S13 doğrulaması, başlangıç ölçümleri, v2.0 planı ve kararları.
- Ölçüm: 73 test yeşil; ktlint/detekt/assembleDebug yeşil; release arm64 3,20 MB.
- Kaldı: —
- Sıradaki: **F1** (kullanıcı "başla" dedi; otonom akış uyarınca F0 bitince F1'e geçilir).
- Doğrulama komutu: `.\gradlew.bat ktlintCheck detekt testDebugUnitTest assembleDebug`
