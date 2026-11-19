# Katkı Rehberi

Naim Pro'ya katkıda bulunmak istediğiniz için teşekkürler! Bu proje GPL-3.0 ile
lisanslanmıştır; katkılarınız da aynı lisans altında yayımlanır.

## Başlamadan önce

- Büyük değişiklikler için önce bir **issue** açıp yaklaşımı tartışın.
- Reklam, analitik, izleme veya Google Play Services gerektiren hiçbir bağımlılık
  kabul edilmez. Bu projenin temel ilkesidir.
- Uygulama internet olmadan tam çalışmalıdır (offline-first).
- Telifli kod, görsel veya ses kopyalanamaz. Yeni bir dış kaynak ekliyorsanız
  `ATTRIBUTION.md` dosyasını da güncelleyin.

## Geliştirme ortamı

Gereksinimler:

- JDK 17
- Android SDK: platform 36, build-tools 36

```bash
git clone https://github.com/MuratSamet02/naim-pro.git
cd naim-pro
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew assembleDebug
```

Windows'ta `local.properties` içeriği:

```properties
sdk.dir=C\:\\Users\\<kullanıcı>\\Android\\Sdk
```

## Kod stili

- Kotlin resmi stil rehberi + `ktlint` kuralları (`.editorconfig`)
- `@Composable` fonksiyonları PascalCase olduğu için adlandırma kuralından muaftır
- Maksimum satır uzunluğu 140
- Compose ekranları mantıksal parçalara (`@Composable` alt fonksiyonlar) ayrılır;
  iş mantığı ViewModel'de durur

Commit'lemeden önce mutlaka:

```bash
./gradlew ktlintFormat    # biçimlendir
./gradlew check           # ktlint + detekt + testler + lint
```

**Yeşil olmayan hiçbir şey commit'lenmez.**

## Commit mesajları

[Conventional Commits](https://www.conventionalcommits.org/) kullanılır:

```
feat(kible): sensör doğruluk uyarısı ekle
fix(alarm): saat dilimi değişiminde alarmları yeniden kur
docs(readme): derleme talimatını güncelle
test(prayer): yüksek enlem kenar durumları
chore(deps): compose bom 2026.05.00
```

Türler: `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `build`, `ci`, `chore`, `revert`.

## Testler

- Yeni iş mantığı için birim testi zorunludur.
- Vakit hesaplama ile ilgili değişikliklerde `DiyanetComparisonTest` sapmasını
  ≤ 1 dakikada tutmalıdır.
- Hesaplama motoru saf Kotlin'dir; testler Android'e ihtiyaç duymadan çalışır.

```bash
./gradlew testDebugUnitTest
```

## Pull request

1. Dalı `main` üzerinden açın: `feat/kisa-aciklama`
2. Değişiklikleri tek bir konuya odaklayın
3. PR açıklamasında **ne** ve **neden** değiştiğini yazın
4. CI yeşil olmalıdır (ktlint, detekt, testler, `assembleDebug`)
5. Ekran görüntüsü gerektiren UI değişikliklerinde önce/sonra görseli ekleyin

## Hata bildirimi

Issue açarken şunları ekleyin:

- Cihaz modeli ve Android sürümü
- Uygulama sürümü (Ayarlar → Hakkında)
- Adımlar: ne yaptınız, ne beklediniz, ne oldu
- Vakit hatasıysa: şehir, tarih, beklenen ve görünen saat
