package com.moltrax.personalnoteapp.domain.repository

import com.moltrax.personalnoteapp.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    val syncStatus: Flow<SyncStatus>
    suspend fun pushToDrive()
    suspend fun pullFromDrive()
}
