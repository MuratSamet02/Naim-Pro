# Güvenlik Politikası

## Desteklenen sürümler

| Sürüm | Destek |
|---|---|
| 1.x (en son) | ✅ |

## Açık bildirimi

Bir güvenlik açığı bulduysanız lütfen **herkese açık issue açmayın**.
Bunun yerine GitHub'ın
[özel güvenlik bildirimi](https://github.com/MuratSamet02/naim-pro/security/advisories/new)
özelliğini kullanın.

Bildiriminizde şunları belirtin:

- Etkilenen sürüm
- Sorunun türü (ör. dosya yolu geçişi, niyet yönlendirme, izin kötüye kullanımı)
- Yeniden üretme adımları
- Olası etki

## Kapsam

Naim Pro'nun saldırı yüzeyi bilinçli olarak küçüktür:

- Sunucu, hesap sistemi veya bulut bileşeni **yoktur**
- Hiçbir veri cihazdan çıkmaz
- Üçüncü taraf analitik/reklam SDK'sı yoktur
- `FileProvider` yalnızca uygulamanın kendi cache dosyalarını paylaşır
- Dışa açık `BroadcastReceiver`'lar yalnızca sistem yayınlarını dinler

Yine de aşağıdaki konular kapsam içindedir:

- Yedekleme içe aktarma sırasında hatalı biçimlendirilmiş JSON ile çökme veya
  yol geçişi
- Bildirim/alarm yoluyla izin atlatma
- Özel ses dosyası seçiminde yol doğrulaması
- Bağımlılıklarda bilinen güvenlik açıkları (Dependabot izler)

## Yanıt süresi

Bildirimler genellikle 7 gün içinde yanıtlanır. Düzeltme yayımlandığında
sürüm notlarında ve bu dosyada belirtilir.
