package com.moltrax.personalnoteapp.data.repository

import com.moltrax.personalnoteapp.data.local.db.dao.VaultDao
import com.moltrax.personalnoteapp.data.local.db.entity.toDomain
import com.moltrax.personalnoteapp.data.local.db.entity.toEntity
import com.moltrax.personalnoteapp.domain.model.VaultEntry
import com.moltrax.personalnoteapp.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultRepositoryImpl @Inject constructor(private val dao: VaultDao) : VaultRepository {
    override fun observeAll(): Flow<List<VaultEntry>> = dao.observeAll().map { it.map { e -> e.toDomain() } }
    override suspend fun upsert(entry: VaultEntry) = dao.upsert(entry.toEntity())
    override suspend fun delete(id: String) = dao.delete(id)
}
