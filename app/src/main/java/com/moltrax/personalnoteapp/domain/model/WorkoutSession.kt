package com.moltrax.personalnoteapp.domain.model

import java.util.UUID

data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val workoutId: String,
    val workoutName: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val loggedExercises: List<LoggedExercise> = emptyList(),
)

data class LoggedExercise(
    val exerciseId: String,
    val exerciseName: String,
    val sets: List<LoggedSet> = emptyList(),
)

data class LoggedSet(
    val reps: Int,
    val weightKg: Double? = null,
    val durationSeconds: Int? = null,
    val completedAt: Long = System.currentTimeMillis(),
)
