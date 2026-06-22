package com.moltrax.personalnoteapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moltrax.personalnoteapp.domain.model.LoggedExercise
import com.moltrax.personalnoteapp.domain.model.LoggedSet
import com.moltrax.personalnoteapp.domain.model.WorkoutSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val workoutName: String,
    val startedAt: Long,
    val completedAt: Long?,
    val loggedExercisesJson: String,
)

@Serializable
private data class LoggedSetJson(val reps: Int, val weightKg: Double?, val durationSeconds: Int?, val completedAt: Long)

@Serializable
private data class LoggedExerciseJson(val exerciseId: String, val exerciseName: String, val sets: List<LoggedSetJson>)

private val json = Json { ignoreUnknownKeys = true }

fun WorkoutSessionEntity.toDomain(): WorkoutSession {
    val exercises = json.decodeFromString<List<LoggedExerciseJson>>(loggedExercisesJson)
        .map { ex ->
            LoggedExercise(ex.exerciseId, ex.exerciseName,
                ex.sets.map { LoggedSet(it.reps, it.weightKg, it.durationSeconds, it.completedAt) })
        }
    return WorkoutSession(id, workoutId, workoutName, startedAt, completedAt, exercises)
}

fun WorkoutSession.toEntity() = WorkoutSessionEntity(
    id = id,
    workoutId = workoutId,
    workoutName = workoutName,
    startedAt = startedAt,
    completedAt = completedAt,
    loggedExercisesJson = json.encodeToString(loggedExercises.map { ex ->
        LoggedExerciseJson(ex.exerciseId, ex.exerciseName,
            ex.sets.map { LoggedSetJson(it.reps, it.weightKg, it.durationSeconds, it.completedAt) })
    }),
)
