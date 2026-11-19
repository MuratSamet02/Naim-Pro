# Gizlilik Politikası — Naim Pro

**Son güncelleme: 19 Kasım 2026**

## Kısa özet

Naim Pro **hiçbir kişisel veri toplamaz, saklamaz, işlemez veya iletmez.**
Uygulamanın kendi sunucusu yoktur, hesap sistemi yoktur ve hiçbir üçüncü taraf
analitik/reklam servisi kullanılmaz.

## Toplanan veriler

**Hiçbiri.**

- Ad, e-posta, telefon numarası, cihaz kimliği veya reklam kimliği istenmez.
- Analitik, çökme raporlama (crash reporting), reklam veya izleme SDK'sı yoktur.
- Google Play Services, Firebase veya benzeri bir bağımlılık yoktur.
- Uygulama hiçbir veriyi sizin dışınızda bir yere göndermez.

## Cihazda kalan veriler

Aşağıdaki bilgiler **yalnızca cihazınızda** saklanır ve uygulamayı sildiğinizde
onlarla birlikte silinir:

| Veri | Nerede | Neden |
|---|---|---|
| Seçili konum (il/ilçe veya yaklaşık koordinat) | DataStore | Vakit ve kıble hesabı için |
| Vakit düzeltmeleri, yöntem, tema, dil ayarları | DataStore | Tercihlerinizi hatırlamak için |
| Namaz takibi kayıtları | Room (yerel SQLite) | Seri ve istatistik göstermek için |
| Zikirmatik geçmişi | Room (yerel SQLite) | Sayaç geçmişini göstermek için |

Bu veriler cihaz yedeğine dahil edilmez (`android:allowBackup="false"`). Yalnızca
siz "Yedeği dışa aktar" derseniz, seçtiğiniz konuma bir JSON dosyası yazılır.

## İzinler ve gerekçeleri

Her izin uygulama içinde **Ayarlar → İzinler** ekranında açıklanır. Uygulama bu
izinlerin hiçbiri verilmeden de çalışır.

| İzin | Neden | Zorunlu mu |
|---|---|---|
| `ACCESS_COARSE_LOCATION` | Bulunduğunuz yere göre vakit hesabı. Yalnızca **yaklaşık** konum istenir; kesin konum istenmez. | Hayır |
| `POST_NOTIFICATIONS` | Vakit bildirimlerini göstermek | Hayır |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Vaktin saniyesinde bildirim | Hayır |
| `RECEIVE_BOOT_COMPLETED` | Yeniden başlatmadan sonra alarmları yeniden kurmak | Hayır |
| `VIBRATE` | Titreşimli bildirim ve kıble hizalaması | Hayır |
| `WAKE_LOCK` | Ezan çalarken ekranı kısa süre uyanık tutmak | Hayır |
| `ACCESS_NOTIFICATION_POLICY` | Cami modu: vakitte telefonu sessize almak | Hayır |

Konum verisi **hiçbir zaman** cihazdan çıkmaz; hesaplama cihazda yapılır.
Uygulama hiçbir ağ bağlantısı kurmaz ve bu nedenle `INTERNET` izni istemez.

## Çocuklar

Uygulama hiçbir kişisel veri toplamadığı için çocuklardan da veri toplamaz.

## Değişiklikler

Bu politika değişirse, güncel sürüm bu dosyada yayımlanır ve sürüm notlarında
belirtilir.

## İletişim

Sorularınız için GitHub üzerinden issue açabilirsiniz:
<https://github.com/MuratSamet02/naim-pro/issues>
