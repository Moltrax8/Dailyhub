package com.moltrax.personalnoteapp.domain.repository

import com.moltrax.personalnoteapp.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeAll(): Flow<List<Task>>
    suspend fun getById(id: String): Task?
    suspend fun upsert(task: Task)
    suspend fun delete(id: String)
    suspend fun getAll(): List<Task>
    suspend fun replaceAll(tasks: List<Task>)
}
