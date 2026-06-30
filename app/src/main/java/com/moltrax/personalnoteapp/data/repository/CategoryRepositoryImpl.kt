package com.moltrax.personalnoteapp.data.repository

import com.moltrax.personalnoteapp.data.local.db.dao.CategoryDao
import com.moltrax.personalnoteapp.data.local.db.dao.TaskDao
import com.moltrax.personalnoteapp.data.local.db.entity.CategoryEntity
import com.moltrax.personalnoteapp.data.local.db.entity.toDomain
import com.moltrax.personalnoteapp.data.local.db.entity.toEntity
import com.moltrax.personalnoteapp.domain.model.Category
import com.moltrax.personalnoteapp.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val taskDao: TaskDao,
) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAll(): List<Category> = dao.getAll().map { it.toDomain() }

    override suspend fun replaceAll(categories: List<Category>) {
        dao.deleteAll()
        dao.upsertAll(categories.map { it.toEntity() })
    }

    override suspend fun ensureExists(name: String, isPermanent: Boolean) {
        val n = name.trim()
        if (n.isBlank()) return
        val existing = dao.getByName(n)
        when {
            existing == null -> dao.upsert(CategoryEntity(n, isPermanent))
            isPermanent && !existing.isPermanent -> dao.upsert(existing.copy(isPermanent = true))
            else -> Unit
        }
    }

    override suspend fun rename(oldName: String, newName: String) {
        val old = oldName.trim()
        val new = newName.trim()
        if (new.isBlank() || new == old) return
        val existing = dao.getByName(old) ?: return

        val now = System.currentTimeMillis()
        // Bağlı görevleri yeni ada taşı
        taskDao.reassignCategory(old, new, now)

        // Hedef ad zaten varsa kalıcılığı koru/yükselt, yoksa eskinin kalıcılığıyla oluştur
        val target = dao.getByName(new)
        if (target == null) {
            dao.upsert(CategoryEntity(new, existing.isPermanent))
        } else if (existing.isPermanent && !target.isPermanent) {
            dao.upsert(target.copy(isPermanent = true))
        }
        dao.delete(old)
    }

    override suspend fun delete(name: String) {
        val now = System.currentTimeMillis()
        taskDao.clearCategory(name, now)
        dao.delete(name)
    }

    override suspend fun cleanupTemporary() = dao.deleteOrphanTemporary()
}
