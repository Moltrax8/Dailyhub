package com.moltrax.personalnoteapp.data.remote.drive

import com.moltrax.personalnoteapp.BuildConfig
import com.moltrax.personalnoteapp.data.remote.drive.model.SyncMetadata
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private const val DRIVE_BASE = "https://www.googleapis.com"
private const val UPLOAD_BASE = "https://www.googleapis.com/upload/drive/v3"
private const val MIME = "application/json"
private const val FILE_NAME = "tasks_sync.json"

data class DriveFile(val id: String, val etag: String)

@Singleton
class DriveApiService @Inject constructor(private val http: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    private fun auth(token: String) = "Bearer $token"

    suspend fun findOrNull(token: String): DriveFile? {
        val spaces = BuildConfig.DRIVE_SCOPE.contains("appdata").let {
            if (it) "appDataFolder" else BuildConfig.DRIVE_FOLDER_NAME
        }
        val url = "$DRIVE_BASE/drive/v3/files?spaces=$spaces&q=name='$FILE_NAME'&fields=files(id,etag)"
        val req = Request.Builder().url(url).header("Authorization", auth(token)).get().build()
        val resp = http.newCall(req).execute()
        if (!resp.isSuccessful) return null
        val body = resp.body?.string() ?: return null
        val files = JSONObject(body).getJSONArray("files")
        if (files.length() == 0) return null
        val file = files.getJSONObject(0)
        return DriveFile(file.getString("id"), file.optString("etag", ""))
    }

    suspend fun download(token: String, fileId: String): SyncMetadata? {
        val url = "$DRIVE_BASE/drive/v3/files/$fileId?alt=media"
        val req = Request.Builder().url(url).header("Authorization", auth(token)).get().build()
        val resp = http.newCall(req).execute()
        if (!resp.isSuccessful) return null
        val body = resp.body?.string() ?: return null
        return runCatching { json.decodeFromString<SyncMetadata>(body) }.getOrNull()
    }

    data class UploadResult(val fileId: String, val etag: String)

    /** Returns null on 412 Precondition Failed (ETag mismatch) */
    suspend fun upload(
        token: String,
        metadata: SyncMetadata,
        existingFileId: String?,
        existingEtag: String?,
    ): UploadResult? {
        val content = json.encodeToString(metadata)
        val mediaType = MIME.toMediaType()

        return if (existingFileId == null) {
            // Multipart new file upload to appDataFolder
            val metaJson = """{"name":"$FILE_NAME","parents":["appDataFolder"]}"""
            val boundary = "===boundary==="
            val body = "--$boundary\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n$metaJson\r\n" +
                "--$boundary\r\nContent-Type: $MIME\r\n\r\n$content\r\n--$boundary--"
            val req = Request.Builder()
                .url("$UPLOAD_BASE/files?uploadType=multipart&fields=id,etag")
                .header("Authorization", auth(token))
                .header("Content-Type", "multipart/related; boundary=$boundary")
                .post(body.toRequestBody())
                .build()
            val resp = http.newCall(req).execute()
            if (!resp.isSuccessful) return null
            val obj = JSONObject(resp.body?.string() ?: return null)
            UploadResult(obj.getString("id"), obj.optString("etag", ""))
        } else {
            // Update existing file with ETag check
            val reqBuilder = Request.Builder()
                .url("$UPLOAD_BASE/files/$existingFileId?uploadType=media&fields=id,etag")
                .header("Authorization", auth(token))
                .header("Content-Type", MIME)
            if (existingEtag != null) reqBuilder.header("If-Match", existingEtag)
            val req = reqBuilder.patch(content.toRequestBody(mediaType)).build()
            val resp = http.newCall(req).execute()
            if (resp.code == 412) return null // ETag conflict
            if (!resp.isSuccessful) return null
            val obj = JSONObject(resp.body?.string() ?: return null)
            UploadResult(obj.getString("id"), obj.optString("etag", ""))
        }
    }
}
