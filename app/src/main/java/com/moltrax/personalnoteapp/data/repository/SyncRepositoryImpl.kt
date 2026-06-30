package com.moltrax.personalnoteapp.data.repository

import com.moltrax.personalnoteapp.data.local.preferences.AppPreferences
import com.moltrax.personalnoteapp.data.remote.drive.DriveApiService
import com.moltrax.personalnoteapp.data.remote.drive.DriveAuthService
import com.moltrax.personalnoteapp.data.remote.drive.model.SyncMetadata
import com.moltrax.personalnoteapp.data.remote.drive.model.toDomain
import com.moltrax.personalnoteapp.data.remote.drive.model.toJson
import com.moltrax.personalnoteapp.domain.model.SyncStatus
import com.moltrax.personalnoteapp.domain.repository.CategoryRepository
import com.moltrax.personalnoteapp.domain.repository.SyncRepository
import com.moltrax.personalnoteapp.domain.repository.TaskRepository
import com.moltrax.personalnoteapp.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val taskRepo: TaskRepository,
    private val categoryRepo: CategoryRepository,
    private val workoutRepo: WorkoutRepository,
    private val driveApi: DriveApiService,
    private val driveAuth: DriveAuthService,
    private val prefs: AppPreferences,
) : SyncRepository {

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: Flow<SyncStatus> = _status

    override suspend fun sync(manual: Boolean) = runSync(manual) { token ->
        // Önce çek + birleştir, sonra geri gönder: boş cihazın uzaktaki yedeği ezmesini önler
        pullInternal(token)
        pushInternal(token)
        prefs.setLastSyncAt(Instant.now().toString())
    }

    // Arka plan gönderimi: her zaman sessiz (manual = false). Hata olursa yine gösterilir.
    override suspend fun pushToDrive() = runSync(manual = false) { token ->
        pushInternal(token)
        prefs.setLastSyncAt(Instant.now().toString())
    }

    override suspend fun pullFromDrive(manual: Boolean) = runSync(manual) { token ->
        pullInternal(token)
    }

    override fun acknowledgeStatus() {
        // Yalnızca başarı durumunu temizle; hata kullanıcı çözene kadar görünür kalsın.
        if (_status.value is SyncStatus.Synced) _status.value = SyncStatus.Idle
    }

    /**
     * Ortak senkronizasyon iskeleti. Başarı durumu yalnızca [manual] tetiklemede ya da önceki
     * durum HATA iken (hatadan kurtarma) gösterilir; normal arka plan senkronizasyonu sessizce
     * Idle'a döner. Hatalar her durumda gösterilir.
     */
    private suspend fun runSync(manual: Boolean, block: suspend (token: String) -> Unit) {
        // Giriş yapılmamışsa sessizce geç (başlangıçtaki otomatik sync için).
        if (driveAuth.getLastSignedInAccount() == null) return
        val announce = manual || _status.value is SyncStatus.Error
        if (announce) _status.value = SyncStatus.Syncing
        runCatching {
            val token = driveAuth.getFreshToken()
                ?: throw IllegalStateException("Erişim jetonu alınamadı (oturum geçersiz olabilir)")
            block(token)
        }.onSuccess {
            _status.value = if (announce) SyncStatus.Synced else SyncStatus.Idle
        }.onFailure {
            _status.value = SyncStatus.Error(it.detail())
        }
    }

    // Hatanın tam detayını üretir: istisna türü + mesaj + (varsa) kök neden.
    private fun Throwable.detail(): String = buildString {
        append(this@detail::class.java.simpleName)
        message?.let { append(": "); append(it) }
        cause?.let { c ->
            append(" | neden: ").append(c::class.java.simpleName)
            c.message?.let { append(": "); append(it) }
        }
    }

    private suspend fun pushInternal(token: String) {
        val meta = buildMetadata()
        // Yeni cihazda ilk gönderimde uzaktaki mevcut dosyayı keşfet
        val fileId = prefs.driveFileId.first() ?: driveApi.findOrNull(token)?.id

        val result = driveApi.upload(token, meta, fileId)
        prefs.setDriveFileId(result.fileId)
    }

    private suspend fun pullInternal(token: String) {
        val fileId = prefs.driveFileId.first() ?: driveApi.findOrNull(token)?.id ?: return
        val remote = driveApi.download(token, fileId) ?: return

        // Tasks — LWW by updatedAt
        val mergedTasks = mergeById(taskRepo.getAll(), remote.tasks.map { it.toDomain() }, { it.id }) { l, r ->
            if (r.updatedAt > l.updatedAt) r else l
        }
        taskRepo.replaceAll(mergedTasks)

        // Kategoriler — ada göre birleşim; kalıcılık iki taraftan biri kalıcıysa korunur.
        val mergedCategories = mergeById(
            categoryRepo.getAll(), remote.categories.map { it.toDomain() }, { it.name },
        ) { l, r -> if (l.isPermanent || r.isPermanent) l.copy(isPermanent = true) else l }
        categoryRepo.replaceAll(mergedCategories)
        // Birleşim sonrası bağlı görevi kalmayan geçici kategorileri temizle
        categoryRepo.cleanupTemporary()

        // Workout grupları — LWW: grup updatedAt'ine göre. Mezar taşları (isDeleted) dahil edilir,
        // böylece bir tarafta silinen grup/antrenman karşı taraftan geri DİRİLTİLMEZ.
        val mergedGroups = mergeById(workoutRepo.getGroupsForSync(), remote.workoutGroups, { it.id }) { l, r ->
            if (r.updatedAt > l.updatedAt) r else l
        }
        workoutRepo.replaceGroups(mergedGroups)

        // Workout seansları — değişmez, id'ye göre birleşim
        val mergedSessions = mergeById(workoutRepo.getSessions(), remote.workoutSessions, { it.id }) { l, _ -> l }
        workoutRepo.replaceSessions(mergedSessions)
    }

    private suspend fun buildMetadata() = SyncMetadata(
        lastModifiedUtc = Instant.now().toString(),
        tasks           = taskRepo.getAll().map { it.toJson() },
        categories      = categoryRepo.getAll().map { it.toJson() },
        // Mezar taşları dahil — diğer cihazlar silmeleri öğrensin.
        workoutGroups   = workoutRepo.getGroupsForSync(),
        workoutSessions = workoutRepo.getSessions(),
    )

    /** Generic id-keyed merge; [resolve] picks the winner when an id exists on both sides. */
    private fun <T : Any> mergeById(
        local: List<T>,
        remote: List<T>,
        id: (T) -> String,
        resolve: (local: T, remote: T) -> T,
    ): List<T> {
        val map = LinkedHashMap<String, T>()
        local.forEach { map[id(it)] = it }
        remote.forEach { r ->
            val key = id(r)
            val existing = map[key]
            map[key] = if (existing == null) r else resolve(existing, r)
        }
        return map.values.toList()
    }
}
