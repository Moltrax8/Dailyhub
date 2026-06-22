package com.moltrax.personalnoteapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moltrax.personalnoteapp.domain.model.Priority
import com.moltrax.personalnoteapp.domain.model.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String?,
    val dueDate: Long?,
    val priority: String,
    val isDone: Boolean,
    val isRecurring: Boolean,
    val intervalDays: Int?,
    val focusDurationSeconds: Int,
    val category: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
)

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    notes = notes,
    dueDate = dueDate,
    priority = Priority.valueOf(priority),
    isDone = isDone,
    isRecurring = isRecurring,
    intervalDays = intervalDays,
    focusDurationSeconds = focusDurationSeconds,
    category = category,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    notes = notes,
    dueDate = dueDate,
    priority = priority.name,
    isDone = isDone,
    isRecurring = isRecurring,
    intervalDays = intervalDays,
    focusDurationSeconds = focusDurationSeconds,
    category = category,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
)
