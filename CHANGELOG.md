# Değişiklik Günlüğü

Bu proje [Conventional Commits](https://www.conventionalcommits.org/) ve
[Semantic Versioning](https://semver.org/lang/tr/) kullanır.

## [1.0.1] — 2026-11-19

Açılışta çökme düzeltmesi.

### Düzeltilenler
- **Açılışta çökme:** Paketleme sırasında `assets/cities.csv.gz` dosyası
  `assets/cities.csv` olarak açılıyordu; uygulama eski adı aradığı için
  `FileNotFoundException` ile kapanıyordu. Yükleyici artık iki adı da dener,
  gzip olup olmadığını dosya adına güvenmeden algılar ve gömülü şehir listesi
  okunamazsa yerleşik kısa listeye düşer.
- Şehir kataloğu artık ana thread'de değil `Dispatchers.IO` üzerinde yükleniyor;
  bozuk ayar dosyası da açılışı engellemiyor.

### Değişenler
- `INTERNET` ve `ACCESS_NETWORK_STATE` izinleri kaldırıldı; uygulama tamamen
  çevrimdışı ve hiçbir ağ bağlantısı kurmuyor. Bununla birlikte hiçbir şey
  yapmayan "Diyanet ile çevrim içi karşılaştırma" anahtarı da kaldırıldı.
- Sürüm imzasına v1 (JAR) imzası eklendi (v2 + v3 ile birlikte), böylece tüm
  kurulum ve doğrulama araçları imzayı geçerli sayar.

### Altyapı
- GitHub Actions'ta gerçek emülatörde release APK açılış testi eklendi
  (`.github/workflows/smoke.yml`); çökme bu testle yakalandı.

## [1.0.0] — 2026-11-19

İlk sürüm.

### Eklenenler
- Günün altı vakti ve sonraki vakte canlı geri sayım
- 9 hesap yöntemi (Diyanet, Diyanet · Şâfiî, MWL, ISNA, Ümmü'l-Kurâ, Mısır,
  Karaçi, Kuveyt, Singapur), mezhep ve yüksek enlem seçenekleri
- Vakit başına ±30 dakika düzeltme
- Vakit başına bildirim türü, vakit öncesi hatırlatma, kendi ses dosyası,
  ayrı bildirim kanalları, pil optimizasyonu rehberi
- Cami modu (vakitte otomatik sessize alma)
- Glance widget'ları: küçük, orta, büyük
- Kıble pusulası (sensör füzyonu, titreşimli hizalama, doğruluk uyarısı)
- İmsakiye: aylık tablo, PDF ve CSV dışa aktarma
- Hicri takvim ve dini günler/geceler takvimi + bildirim
- Namaz takibi: seri, haftalık/aylık istatistik
- Zikirmatik: hedefli sayaç, titreşim, ses tuşuyla sayma, geçmiş
- Esmâ-i Hüsnâ, günün duası/ayeti/hadisi
- Açık/koyu/AMOLED tema, Material You, TR/EN/AR
- Ayarları JSON olarak yedekleme/geri yükleme
- ~19.700 yerleşik yerleşim yeri (GeoNames, CC BY 4.0)
- CI (ktlint, detekt, testler, debug APK) ve sürüm iş akışı (imzalı APK + AAB)

### Güvenlik
- Hiçbir reklam, analitik veya çökme raporlama bağımlılığı yok
- Google Play Services bağımlılığı yok (F-Droid uyumlu)
- Yalnızca yaklaşık konum izni; internet izni isteğe bağlı
