package com.moltrax.personalnoteapp.data.repository

import com.moltrax.personalnoteapp.data.local.preferences.AppPreferences
import com.moltrax.personalnoteapp.data.remote.drive.DriveApiService
import com.moltrax.personalnoteapp.data.remote.drive.DriveAuthService
import com.moltrax.personalnoteapp.data.remote.drive.model.SyncMetadata
import com.moltrax.personalnoteapp.data.remote.drive.model.toDomain
import com.moltrax.personalnoteapp.data.remote.drive.model.toJson
import com.moltrax.personalnoteapp.domain.model.SyncStatus
import com.moltrax.personalnoteapp.domain.model.Task
import com.moltrax.personalnoteapp.domain.repository.SyncRepository
import com.moltrax.personalnoteapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val taskRepo: TaskRepository,
    private val driveApi: DriveApiService,
    private val driveAuth: DriveAuthService,
    private val prefs: AppPreferences,
) : SyncRepository {

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: Flow<SyncStatus> = _status

    override suspend fun pushToDrive() {
        val token = driveAuth.getFreshToken() ?: return
        _status.value = SyncStatus.Syncing
        runCatching {
            val tasks = taskRepo.getAll()
            val meta  = buildMetadata(tasks)
            var fileId = prefs.driveFileId.first()
            var etag   = prefs.driveEtag.first()

            // Discover existing file on first push from a new device
            if (fileId == null) {
                val found = driveApi.findOrNull(token)
                fileId = found?.id
                etag   = found?.etag
            }

            val result = driveApi.upload(token, meta, fileId, etag)

            if (result == null && fileId != null) {
                // 412 conflict — pull, merge, retry once
                pullInternal(token)
                val merged = taskRepo.getAll()
                val mergedMeta = buildMetadata(merged)
                val retry = driveApi.upload(token, mergedMeta, prefs.driveFileId.first(), prefs.driveEtag.first())
                if (retry != null) {
                    prefs.setDriveFileId(retry.fileId)
                    prefs.setDriveEtag(retry.etag)
                }
            } else if (result != null) {
                prefs.setDriveFileId(result.fileId)
                prefs.setDriveEtag(result.etag)
            }

            prefs.setLastSyncAt(Instant.now().toString())
            _status.value = SyncStatus.Synced
        }.onFailure {
            _status.value = SyncStatus.Error(it.message ?: "Sync failed")
        }
    }

    override suspend fun pullFromDrive() {
        val token = driveAuth.getFreshToken() ?: return
        _status.value = SyncStatus.Syncing
        runCatching {
            pullInternal(token)
            _status.value = SyncStatus.Synced
        }.onFailure {
            _status.value = SyncStatus.Error(it.message ?: "Pull failed")
        }
    }

    private suspend fun pullInternal(token: String) {
        val fileId = prefs.driveFileId.first() ?: driveApi.findOrNull(token)?.id ?: return
        val remote = driveApi.download(token, fileId) ?: return
        val local  = taskRepo.getAll()
        val merged = mergeLww(local, remote.tasks.map { it.toDomain() })
        taskRepo.replaceAll(merged)
    }

    // Last Write Wins: per-task comparison by updatedAt
    private fun mergeLww(local: List<Task>, remote: List<Task>): List<Task> {
        val map = mutableMapOf<String, Task>()
        local.forEach  { map[it.id] = it }
        remote.forEach { r -> map.merge(r.id, r) { l, rem -> if (rem.updatedAt > l.updatedAt) rem else l } }
        return map.values.toList()
    }

    private fun buildMetadata(tasks: List<Task>) = SyncMetadata(
        lastModifiedUtc = Instant.now().toString(),
        tasks = tasks.map { it.toJson() },
    )
}
