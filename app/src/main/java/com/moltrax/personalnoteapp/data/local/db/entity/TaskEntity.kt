package com.moltrax.personalnoteapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moltrax.personalnoteapp.domain.model.Priority
import com.moltrax.personalnoteapp.domain.model.RecurrenceType
import com.moltrax.personalnoteapp.domain.model.SubTask
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
    val recurrenceType: String? = null,
    val recurrenceDaysOfWeek: List<Int> = emptyList(),
    val focusDurationSeconds: Int,
    val category: String?,
    val subtasks: List<SubTask> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?,
    val linkedWorkoutId: String? = null,
    val linkedProgramId: String? = null,
    val programStartIndex: Int = 0,
    val sortOrder: Long = 0L,
    // Kullanımdan kaldırıldı (oyunlaştırma/"Ceza Bölgesi" silindi). Sütun geriye dönük uyumluluk
    // için şemada bırakıldı; her zaman false yazılır.
    val isPenalty: Boolean = false,
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
    recurrenceType = recurrenceType?.let { runCatching { RecurrenceType.valueOf(it) }.getOrNull() },
    recurrenceDaysOfWeek = recurrenceDaysOfWeek,
    focusDurationSeconds = focusDurationSeconds,
    category = category,
    subtasks = subtasks,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
    linkedWorkoutId = linkedWorkoutId,
    linkedProgramId = linkedProgramId,
    programStartIndex = programStartIndex,
    sortOrder = sortOrder,
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
    recurrenceType = recurrenceType?.name,
    recurrenceDaysOfWeek = recurrenceDaysOfWeek,
    focusDurationSeconds = focusDurationSeconds,
    category = category,
    subtasks = subtasks,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
    linkedWorkoutId = linkedWorkoutId,
    linkedProgramId = linkedProgramId,
    programStartIndex = programStartIndex,
    sortOrder = sortOrder,
    isPenalty = false,
)
