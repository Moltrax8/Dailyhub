package com.moltrax.personalnoteapp.data.remote.drive.model

import com.moltrax.personalnoteapp.domain.model.Category
import com.moltrax.personalnoteapp.domain.model.Priority
import com.moltrax.personalnoteapp.domain.model.RecurrenceType
import com.moltrax.personalnoteapp.domain.model.SubTask
import com.moltrax.personalnoteapp.domain.model.Task
import com.moltrax.personalnoteapp.domain.model.WorkoutGroup
import com.moltrax.personalnoteapp.domain.model.WorkoutSession
import kotlinx.serialization.Serializable

@Serializable
data class SyncMetadata(
    val version: Int = 3,
    val lastModifiedUtc: String,
    // Tüm alanların varsayılanı boş liste: eski (v1/v2) yedek dosyaları da sorunsuz çözülür
    val tasks: List<TaskJson> = emptyList(),
    val categories: List<CategoryJson> = emptyList(),
    val workoutGroups: List<WorkoutGroup> = emptyList(),
    val workoutSessions: List<WorkoutSession> = emptyList(),
)

@Serializable
data class CategoryJson(
    val name: String,
    val isPermanent: Boolean = false,
)

fun Category.toJson() = CategoryJson(name = name, isPermanent = isPermanent)

fun CategoryJson.toDomain() = Category(name = name, isPermanent = isPermanent)

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
    // Zengin tekrar (eski yedeklerde alan yoksa: null / boş → intervalDays davranışı korunur)
    val recurrenceType: String? = null,
    val recurrenceDaysOfWeek: List<Int> = emptyList(),
    val focusDurationSeconds: Int = 1500,
    val category: String? = null,
    // Alt görevler (checklist) — senkronizasyonda görevle birlikte taşınır (eski yedeklerde boş)
    val subtasks: List<SubTask> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long? = null,
    // Varsayılan 0: eski yedeklerde alan yoksa sorunsuz çözülür (geriye dönük uyumlu)
    val sortOrder: Long = 0L,
    // Antrenman/program bağları — senkronizasyonda korunur (eski yedeklerde null/0)
    val linkedWorkoutId: String? = null,
    val linkedProgramId: String? = null,
    val programStartIndex: Int = 0,
    // Kullanımdan kaldırıldı: eski yedeklerle uyum için alan korunur, artık okunmaz.
    val isPenalty: Boolean = false,
)

fun Task.toJson() = TaskJson(
    id = id, title = title, notes = notes, dueDate = dueDate, priority = priority.name,
    isDone = isDone, isRecurring = isRecurring, intervalDays = intervalDays,
    recurrenceType = recurrenceType?.name, recurrenceDaysOfWeek = recurrenceDaysOfWeek,
    focusDurationSeconds = focusDurationSeconds, category = category, subtasks = subtasks,
    createdAt = createdAt, updatedAt = updatedAt, completedAt = completedAt, sortOrder = sortOrder,
    linkedWorkoutId = linkedWorkoutId, linkedProgramId = linkedProgramId,
    programStartIndex = programStartIndex,
)

fun TaskJson.toDomain() = Task(
    id = id, title = title, notes = notes, dueDate = dueDate,
    priority = runCatching { Priority.valueOf(priority) }.getOrDefault(Priority.MEDIUM),
    isDone = isDone, isRecurring = isRecurring, intervalDays = intervalDays,
    recurrenceType = recurrenceType?.let { runCatching { RecurrenceType.valueOf(it) }.getOrNull() },
    recurrenceDaysOfWeek = recurrenceDaysOfWeek,
    focusDurationSeconds = focusDurationSeconds, category = category, subtasks = subtasks,
    createdAt = createdAt, updatedAt = updatedAt, completedAt = completedAt,
    sortOrder = sortOrder,
    linkedWorkoutId = linkedWorkoutId, linkedProgramId = linkedProgramId,
    programStartIndex = programStartIndex,
)
