package com.moltrax.personalnoteapp.domain.model

import java.util.UUID

data class WorkoutGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val workouts: List<Workout> = emptyList(),
    val currentIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

data class Workout(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val exercises: List<WorkoutExercise> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class WorkoutExercise(
    val id: String = UUID.randomUUID().toString(),
    val exerciseId: String,
    val exerciseName: String,
    val plannedSets: List<PlannedSet> = emptyList(),
    val orderIndex: Int = 0,
)

data class PlannedSet(
    val reps: Int,
    val weightKg: Double? = null,
    val durationSeconds: Int? = null,
)

data class Exercise(
    val id: String,
    val name: String,
    val bodyPart: String,
    val equipment: String? = null,
    val isUnilateral: Boolean = false,
    val description: String? = null,
    val mediaUrl: String? = null,
    val localMediaPath: String? = null,
)
