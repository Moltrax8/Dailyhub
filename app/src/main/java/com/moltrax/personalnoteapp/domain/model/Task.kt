package com.moltrax.personalnoteapp.domain.model

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String? = null,
    val dueDate: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val isDone: Boolean = false,
    val isRecurring: Boolean = false,
    val intervalDays: Int? = null,
    val focusDurationSeconds: Int = 1500,
    val category: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
)
