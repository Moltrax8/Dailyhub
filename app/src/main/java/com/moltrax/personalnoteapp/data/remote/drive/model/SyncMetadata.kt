package com.moltrax.personalnoteapp.data.remote.drive.model

import com.moltrax.personalnoteapp.domain.model.Priority
import com.moltrax.personalnoteapp.domain.model.Task
import kotlinx.serialization.Serializable

@Serializable
data class SyncMetadata(
    val version: Int = 1,
    val lastModifiedUtc: String,
    val tasks: List<TaskJson>,
)

@Serializable
data class TaskJson(
    val id: String,
    val title: String,
    val notes: String? = null,
    val dueDate: Long? = null,
    val priority: String = "MEDIUM",
    val isDone: Boolean = false,
    val isRecurring: Boolean = false,
    val intervalDays: Int? = null,
    val focusDurationSeconds: Int = 1500,
    val category: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long? = null,
)

fun Task.toJson() = TaskJson(
    id, title, notes, dueDate, priority.name, isDone, isRecurring,
    intervalDays, focusDurationSeconds, category, createdAt, updatedAt, completedAt,
)

fun TaskJson.toDomain() = Task(
    id = id, title = title, notes = notes, dueDate = dueDate,
    priority = runCatching { Priority.valueOf(priority) }.getOrDefault(Priority.MEDIUM),
    isDone = isDone, isRecurring = isRecurring, intervalDays = intervalDays,
    focusDurationSeconds = focusDurationSeconds, category = category,
    createdAt = createdAt, updatedAt = updatedAt, completedAt = completedAt,
)
