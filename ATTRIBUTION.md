# Kaynaklar ve Lisanslar — Naim Pro

Naim Pro'da kullanılan her dış kaynak, lisansı ve kullanım amacı aşağıda
listelenmiştir. Uygulamanın kendi kodu, ikonu, widget'ları, sesleri ve
hesaplama motoru özgündür.

## Uygulama kodu

| Bileşen | Lisans |
|---|---|
| Naim Pro kaynak kodu | **GPL-3.0-or-later** |
| Vakit hesaplama motoru (`domain/prayer`) | GPL-3.0-or-later — sıfırdan yazıldı, hiçbir kütüphaneden kopyalanmadı |
| Hicri takvim dönüşümü (`domain/hijri`) | GPL-3.0-or-later — sıfırdan yazıldı |
| Launcher ikonu, bildirim ikonu, widget görselleri | GPL-3.0-or-later — bu proje için özgün olarak üretildi |

## Veri

| Kaynak | Lisans | Kullanım |
|---|---|---|
| [GeoNames](https://www.geonames.org/) — `cities15000` + `admin1CodesASCII` + `countryInfo` dökümleri | **CC BY 4.0** | Uygulamaya gömülü ~19.700 yerleşim yeri (`assets/cities.csv.gz`). Ülke/il/ilçe adları ve koordinatlar. |

`tools/build-city-db.ps1` bu dosyayı GeoNames dökümlerinden yeniden üretir.
Veri dosyası değiştirilmeden, yalnızca alan seçimi ve sıkıştırma yapılarak
kullanılmıştır.

## Referans veriler (test amaçlı)

| Kaynak | Lisans | Kullanım |
|---|---|---|
| Diyanet İşleri Başkanlığı — kamuya açık yayımlanmış vakit listeleri | Kamuya açık resmî veri | `app/src/test/resources/diyanet-official-snapshot.csv` içinde 10 şehir × 32 gün, hesaplama motorunu kalibre etmek ve doğrulamak için. Uygulama bu veriyi çalışma anında indirmez. |

Vakit karşılaştırmaları yalnızca **doğruluk testi** içindir; hiçbir tablo
kopyalanmamış, yalnızca astronomik modelin sapması ölçülmüştür.

## Sesler

| Dosya | Kaynak | Lisans |
|---|---|---|
| `res/raw/naim_chime.ogg` | `tools/generate-tones.ps1` ile sentezlendi | GPL-3.0-or-later |
| `res/raw/naim_alert.ogg` | `tools/generate-tones.ps1` ile sentezlendi | GPL-3.0-or-later |

Ezan kaydı **gönderilmemektedir**: telifsiz/CC0 uygun bir ezan kaydı
bulunmadığından uygulama kısa bir bildirim sesi içerir ve kullanıcının kendi ses
dosyasını seçmesini destekler (Ayarlar → Kendi ses dosyanız).

## Kütüphaneler

Uygulama yalnızca aşağıdaki açık kaynak Android/Kotlin kütüphanelerini kullanır.
Hiçbiri reklam, analitik veya izleme içermez.

| Kütüphane | Lisans |
|---|---|
| AndroidX Core, Activity, Lifecycle, Navigation | Apache-2.0 |
| Jetpack Compose (UI, Material 3, Foundation) | Apache-2.0 |
| AndroidX Room | Apache-2.0 |
| AndroidX DataStore | Apache-2.0 |
| AndroidX WorkManager | Apache-2.0 |
| AndroidX Glance (widget'lar) | Apache-2.0 |
| Kotlin Coroutines | Apache-2.0 |
| Kotlinx Serialization | Apache-2.0 |
| JUnit 5 | EPL-2.0 |
| Turbine | Apache-2.0 |
| detekt | Apache-2.0 |
| ktlint | MIT |
| LeakCanary (yalnızca debug) | Apache-2.0 |
