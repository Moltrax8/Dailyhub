package com.moltrax.personalnoteapp.data.repository

import com.moltrax.personalnoteapp.data.local.db.dao.TaskDao
import com.moltrax.personalnoteapp.data.local.db.entity.toDomain
import com.moltrax.personalnoteapp.data.local.db.entity.toEntity
import com.moltrax.personalnoteapp.domain.model.Task
import com.moltrax.personalnoteapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(private val dao: TaskDao) : TaskRepository {

    override fun observeAll(): Flow<List<Task>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAll(): List<Task> = dao.getAll().map { it.toDomain() }

    override suspend fun getById(id: String): Task? = dao.getById(id)?.toDomain()

    override suspend fun upsert(task: Task) = dao.upsert(task.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)

    override suspend fun replaceAll(tasks: List<Task>) {
        dao.deleteAll()
        dao.upsertAll(tasks.map { it.toEntity() })
    }
}
