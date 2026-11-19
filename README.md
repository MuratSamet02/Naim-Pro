<div align="center">

<img src="docs/branding/naim-pro-icon-source.jpg" alt="Naim Pro" width="160" />

# Naim Pro

**Reklamsız, izleyicisiz, tamamen çevrimdışı namaz vakti uygulaması.**

[![CI](https://github.com/MuratSamet02/naim-pro/actions/workflows/ci.yml/badge.svg)](https://github.com/MuratSamet02/naim-pro/actions/workflows/ci.yml)
[![Release](https://github.com/MuratSamet02/naim-pro/actions/workflows/release.yml/badge.svg)](https://github.com/MuratSamet02/naim-pro/actions/workflows/release.yml)
[![Lisans: GPL-3.0](https://img.shields.io/badge/lisans-GPL--3.0-blue.svg)](LICENSE)

</div>

---

Naim Pro; namaz vakitlerini **cihazında** hesaplayan, hiçbir veriyi toplamayan, reklam
göstermeyen ve internet olmadan tam olarak çalışan açık kaynaklı bir Android
uygulamasıdır. Ticari "Pro" sürümlerde ücretli olan özelliklerin tamamı burada
ücretsizdir.

## Özellikler

### Vakitler
- Günün altı vakti (İmsak, Güneş, Öğle, İkindi, Akşam, Yatsı) ve sonraki vakte **canlı geri sayım**
- Miladi ve hicri tarih, seçili konum
- **9 hesap yöntemi**: Diyanet (Türkiye), Diyanet · Şâfiî ikindi, MWL, ISNA, Ümmü'l-Kurâ,
  Mısır, Karaçi, Kuveyt, Singapur
- Hanefî / standart ikindi mezhebi seçimi
- Yüksek enlem kuralları (gecenin ortası, gecenin yedide biri, açıya göre kısaltma)
- Her vakit için **±30 dakika** düzeltme
- ~19.700 yerleşim yeri uygulamayla birlikte gelir (Türkiye il/ilçe öncelikli)

### Bildirim ve ezan
- Vakit başına ayrı bildirim türü: ezan sesi, kısa uyarı, yalnızca titreşim, sessiz
- Vakitten önce hatırlatma (dakika ayarlı)
- Ses seviyesi, kendi ses dosyanı seçme, ayrı bildirim kanalları
- `AlarmManager.setExactAndAllowWhileIdle` + WorkManager yedek
- Yeniden başlatma, saat dilimi değişimi ve uygulama güncellemesinde alarmlar otomatik yeniden kurulur
- Pil optimizasyonu rehberi

### Diğer
| Özellik | Açıklama |
|---|---|
| Widget'lar | Jetpack Glance; küçük, orta, büyük boyut, geri sayımlı |
| Kıble pusulası | Rotation-vector sensör füzyonu, hizalanınca titreşim, doğruluk uyarısı |
| İmsakiye | Aylık/yıllık tablo, **PDF ve CSV** dışa aktarma |
| Dini günler | Hicri takvimden türetilen kandil/bayram takvimi + bildirim |
| Namaz takibi | Kılındı/kaza işaretleme, seri (streak), haftalık/aylık istatistik |
| Zikirmatik | Hedefli sayaç, titreşim, ses tuşuyla sayma, geçmiş |
| İçerikler | Esmâ-i Hüsnâ, günün duası/ayeti/hadisi (gömülü) |
| Cami modu | Vakitte telefonu otomatik sessize alma (DND izniyle) |
| Tema | Açık / koyu / AMOLED siyah, Material You dinamik renk, yazı boyutu |
| Diller | Türkçe (varsayılan), İngilizce, Arapça (RTL) |
| Yedekleme | Ayarları yerel JSON dosyasına dışa/içe aktarma |
| Erişilebilirlik | TalkBack, büyük yazı, yeterli kontrast |

## Gizlilik

**Hiçbir veri toplanmaz, hiçbir yere gönderilmez.**

- Reklam SDK'sı yok, analitik yok, çökme raporlama yok
- Google Play Services bağımlılığı yok (F-Droid uyumlu)
- Konum yalnızca **yaklaşık** (coarse) ve yalnızca izin verirsen alınır; cihazdan çıkmaz
- **İnternet izni hiç istenmez**; uygulama ağ erişimi olmadan tam olarak çalışır

Ayrıntılar: [PRIVACY.md](PRIVACY.md)

## Kurulum

### Hazır APK

[Releases](https://github.com/MuratSamet02/naim-pro/releases) sayfasından ABI'ye uygun APK'yı indir:

| Cihaz | Dosya |
|---|---|
| Neredeyse tüm modern telefonlar | `app-arm64-v8a-release.apk` |
| Eski 32-bit ARM cihazlar | `app-armeabi-v7a-release.apk` |
| x86_64 emülatörler | `app-x86_64-release.apk` |

#### Play Protect uyarısı hakkında

Uygulama Google Play dışında dağıtıldığı için Android, kurulum sırasında
"Play Protect bu uygulamanın geliştiricisini tanımıyor" veya "bilinmeyen
kaynak" uyarısı gösterebilir. Bu, uygulamanın zararlı olduğu anlamına gelmez:

- APK, projeye ait **sabit bir sürüm anahtarıyla** imzalanır (v1 + v2 + v3);
  debug anahtarı kullanılmaz.
- Sürümün imzası `apksigner verify --print-certs` ile doğrulanabilir.
- İsterseniz APK'yı kendiniz derleyip karşılaştırabilirsiniz (aşağıdaki bölüm).

Kuruluma devam etmek için uyarı ekranında **"Yine de yükle"** seçeneğini
kullanın. Uyarıyı tamamen kaldırmanın tek yolu uygulamayı Google Play'e
yüklemektir; Play Protect, Play dışından yüklenen her uygulama için bu bilgi
ekranını gösterebilir.

### Kaynaktan derleme

Gereksinimler: JDK 17, Android SDK (platform 36, build-tools 36).

```bash
git clone https://github.com/MuratSamet02/naim-pro.git
cd naim-pro
echo "sdk.dir=$ANDROID_HOME" > local.properties   # Windows: sdk.dir=C\:\\path\\to\\sdk
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

Sürüm derlemesi ve imzalama için [docs/BUILD.md](docs/BUILD.md) dosyasına bakın.

## Kalite

| Kontrol | Komut |
|---|---|
| Birim testleri | `./gradlew testDebugUnitTest` |
| Kod stili | `./gradlew ktlintCheck` |
| Statik analiz | `./gradlew detekt` |
| Android Lint | `./gradlew lintDebug` |
| Her şey | `./gradlew check` |

Bu kontrollerin tamamı her `main` push'unda GitHub Actions'ta otomatik çalışır
(bkz. `.github/workflows/ci.yml`).

Vakit hesaplama motoru, Diyanet'in yayımladığı resmî vakitlerle **10 şehir × 32 gün**
üzerinden karşılaştırılır; kabul edilen sapma ≤ 1 dakikadır.

## Mimari

```
app/src/main/java/com/naimpro/app/
├── domain/      Saf Kotlin: astronomi, hicri takvim, modeller (Android bağımlılığı yok)
├── data/        Room, DataStore, şehir verisi, yedekleme, DI kabı
├── alarm/       AlarmManager, WorkManager, bildirimler, ezan çalma, cami modu
├── widget/      Glance widget'ları
└── ui/          Compose ekranları, tema, ViewModel, yardımcılar
```

- Kotlin + Jetpack Compose + Material 3, tek Activity + Navigation Compose
- MVVM + katmanlı mimari (data / domain / ui), manuel DI (`AppContainer`)
- `minSdk 26`, `targetSdk 36`
- Vakitler astronomik olarak hesaplanır ve 30 günlük önbelleğe alınır; ana thread'de ağır iş yok

## Katkı

Katkılar memnuniyetle karşılanır. Lütfen önce [CONTRIBUTING.md](CONTRIBUTING.md) dosyasını okuyun.

## Lisans

GPL-3.0 — bkz. [LICENSE](LICENSE). Kullanılan dış kaynaklar ve lisansları:
[ATTRIBUTION.md](ATTRIBUTION.md).
