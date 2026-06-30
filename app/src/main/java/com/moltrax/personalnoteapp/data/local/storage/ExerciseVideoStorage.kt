package com.moltrax.personalnoteapp.data.local.storage

import android.content.Context
import com.moltrax.personalnoteapp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Egzersiz demo medyasını (ExerciseDB GIF'i veya gerçek .mp4 videosu) uygulamanın iç
 * depolamasına indirir; böylece hareket çevrimdışıyken de oynatılabilir.
 *
 * Dosyalar [filesDir]/exercise_media altında, kaynak egzersiz id'si ile adlandırılır
 * (örn. `0001.gif`). Aynı dosya tekrar indirilmez. İlgili egzersiz/program silindiğinde
 * [delete] ile fiziksel dosya da temizlenir (Repository katmanı çağırır).
 */
@Singleton
class ExerciseVideoStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val http: OkHttpClient,
) {
    private val dir: File by lazy {
        File(context.filesDir, "exercise_media").apply { if (!exists()) mkdirs() }
    }

    /**
     * [url] adresindeki medyayı indirip mutlak yolunu döner. Dosya zaten varsa yeniden
     * indirmeden mevcut yolu döner. Hata olursa null (UI yine de uzak URL'den oynatabilir).
     */
    suspend fun download(exerciseId: String, url: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            // ExerciseDB resim ucu (".../image?...") her zaman GIF döndürür; uzantı buradan çıkarılamaz.
            // Diğer (eski/doğrudan) URL'lerde uzantı adresten alınır.
            val ext = if (url.contains("/image", ignoreCase = true)) "gif"
                else url.substringBefore('?').substringAfterLast('.', "mp4")
                    .takeIf { it.length in 1..5 } ?: "mp4"
            val file = File(dir, "$exerciseId.$ext")
            if (file.exists() && file.length() > 0) return@withContext file.absolutePath

            // ExerciseDB demo'su yalnızca X-RapidAPI-Key header'ı ile indirilebilir (aksi halde 401).
            val request = Request.Builder().url(url).apply {
                if (url.contains("rapidapi.com", ignoreCase = true) && BuildConfig.EXERCISEDB_KEY.isNotEmpty()) {
                    header("X-RapidAPI-Key", BuildConfig.EXERCISEDB_KEY)
                }
            }.build()
            http.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body ?: return@withContext null
                body.byteStream().use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
            }
            file.absolutePath
        }.getOrNull()
    }

    /** Egzersiz/program silindiğinde lokal medya dosyasını temizler. */
    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }
}
