package com.moltrax.personalnoteapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.moltrax.personalnoteapp.data.local.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC, createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY sortOrder ASC, createdAt DESC")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Upsert
    suspend fun upsert(task: TaskEntity)

    @Upsert
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    // Kategori yeniden adlandırıldığında: ilgili görevleri yeni ada taşı (sync için updatedAt'i de tazele)
    @Query("UPDATE tasks SET category = :newName, updatedAt = :now WHERE category = :oldName")
    suspend fun reassignCategory(oldName: String, newName: String, now: Long)

    // Kategori silindiğinde: bağlı görevlerin kategorisini boşalt
    @Query("UPDATE tasks SET category = NULL, updatedAt = :now WHERE category = :name")
    suspend fun clearCategory(name: String, now: Long)
}
