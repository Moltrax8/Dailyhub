package com.moltrax.personalnoteapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.moltrax.personalnoteapp.data.local.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getByName(name: String): CategoryEntity?

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE name = :name")
    suspend fun delete(name: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    /**
     * Geçici (isPermanent = 0) olup hiçbir göreve bağlı olmayan kategorileri siler.
     * Görev silindiğinde / kategorisi değiştiğinde çağrılır.
     */
    @Query(
        """
        DELETE FROM categories
        WHERE isPermanent = 0
          AND name NOT IN (SELECT DISTINCT category FROM tasks WHERE category IS NOT NULL)
        """
    )
    suspend fun deleteOrphanTemporary()
}
