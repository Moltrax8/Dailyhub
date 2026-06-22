package com.moltrax.personalnoteapp.domain.repository

import com.moltrax.personalnoteapp.domain.model.VaultEntry
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun observeAll(): Flow<List<VaultEntry>>
    suspend fun upsert(entry: VaultEntry)
    suspend fun delete(id: String)
}
