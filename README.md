# DailyHub

Kişisel üretkenlik uygulaması — görevler, alt görevler, tekrarlı görevler, takvim,
antrenman takibi ve ilerleme istatistikleri tek yerde. Native Android (Kotlin +
Jetpack Compose) olarak geliştirildi.

> Not: Proje başlangıçta Flutter ile yazılmış, sonradan tamamen native Android'e
> taşınmıştır. Paket kimliği eski adı (`com.moltrax.personalnoteapp`) korur.

## Özellikler

- **Görevler** — alt görevler, tekrarlama (recurrence), kategoriler ve hatırlatma bildirimleri
- **Takvim** — görevleri tarihe göre görüntüleme
- **Antrenman** — oturum takibi, antrenman özeti ve egzersiz demo videoları
- **Profil** — gelişim/ilerleme istatistikleri
- **Ana ekran widget'ı** — görevleri tamamlama, geri alma ve alt görev işaretlemeyi
  uygulamayı açmadan yapma (Glance)
- **Google Drive yedekleme & senkronizasyon** — `appdata` kapsamında otomatik yedek
- **Çoklu dil** — Türkçe / İngilizce
- **Koyu / neon tema**

## Teknolojiler

| Alan | Kullanılan |
|------|-----------|
| Dil | Kotlin |
| UI | Jetpack Compose, Material 3 |
| DI | Hilt |
| Veritabanı | Room |
| Arka plan işleri | WorkManager |
| Widget | Glance |
| Ağ | Retrofit + OkHttp + kotlinx.serialization |
| Görseller | Coil, Media3 (ExoPlayer) |
| Bulut | Google Drive API, Google Sign-In |

- **minSdk** 26 · **targetSdk** 35 · **compileSdk** 35

## Kurulum

1. Depoyu klonla ve Android Studio ile aç.
2. `local.properties.example` dosyasını `local.properties` olarak kopyala ve
   değerleri doldur (bu dosya `.gitignore`'dadır, **asla commit edilmez**):

   ```properties
   sdk.dir=C:\\Users\\<kullanıcı>\\AppData\\Local\\Android\\Sdk
   GOOGLE_CLIENT_ID=your_web_client_id.apps.googleusercontent.com
   DRIVE_FOLDER_NAME=DailyHubBackup
   DRIVE_SCOPE=https://www.googleapis.com/auth/drive.appdata
   ```

3. Google Sign-In / Drive için `app/google-services.json` dosyanı ekle
   (Firebase/Google Cloud konsolundan; bu dosya da gitignore'dadır).

## Derleme

Debug APK:

```bash
./gradlew :app:assembleDebug
```

İmzalı release APK için `local.properties` içine keystore bilgilerini ekle:

```properties
RELEASE_STORE_FILE=release.jks
RELEASE_STORE_PASSWORD=********
RELEASE_KEY_ALIAS=********
RELEASE_KEY_PASSWORD=********
```

Sonra:

```bash
./gradlew :app:assembleRelease
```

Çıktı: `app/build/outputs/apk/release/app-release.apk`

> ⚠️ Keystore dosyasını ve şifrelerini güvenli bir yerde yedekle. Aynı keystore
> olmadan yayınlanmış uygulamayı güncelleyemezsin.

## Test

```bash
./gradlew :app:testDebugUnitTest
```

## Lisans

Özel proje — tüm hakları saklıdır.
