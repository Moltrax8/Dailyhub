package com.moltrax.personalnoteapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moltrax.personalnoteapp.domain.model.PlannedSet
import com.moltrax.personalnoteapp.domain.model.Workout
import com.moltrax.personalnoteapp.domain.model.WorkoutExercise
import com.moltrax.personalnoteapp.domain.model.WorkoutGroup
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "workout_groups")
data class WorkoutGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val currentIndex: Int,
    val createdAt: Long,
)

@Entity(
    tableName = "workouts",
    foreignKeys = [ForeignKey(
        entity = WorkoutGroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("groupId")],
)
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val name: String,
    val orderIndex: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [ForeignKey(
        entity = WorkoutEntity::class,
        parentColumns = ["id"],
        childColumns = ["workoutId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("workoutId")],
)
data class WorkoutExerciseEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val exerciseId: String,
    val exerciseName: String,
    val plannedSetsJson: String,
    val orderIndex: Int,
)

@Serializable
private data class PlannedSetJson(
    val reps: Int,
    val weightKg: Double? = null,
    val durationSeconds: Int? = null,
)

private val json = Json { ignoreUnknownKeys = true }

fun WorkoutExerciseEntity.toDomain() = WorkoutExercise(
    id = id,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    plannedSets = json.decodeFromString<List<PlannedSetJson>>(plannedSetsJson)
        .map { PlannedSet(it.reps, it.weightKg, it.durationSeconds) },
    orderIndex = orderIndex,
)

fun WorkoutExercise.toEntity(workoutId: String) = WorkoutExerciseEntity(
    id = id,
    workoutId = workoutId,
    exerciseId = exerciseId,
    exerciseName = exerciseName,
    plannedSetsJson = json.encodeToString(
        plannedSets.map { PlannedSetJson(it.reps, it.weightKg, it.durationSeconds) }
    ),
    orderIndex = orderIndex,
)

fun WorkoutGroupEntity.toDomain(workouts: List<Workout>) = WorkoutGroup(
    id = id,
    name = name,
    workouts = workouts,
    currentIndex = currentIndex,
    createdAt = createdAt,
)

fun WorkoutGroup.toEntity() = WorkoutGroupEntity(
    id = id,
    name = name,
    currentIndex = currentIndex,
    createdAt = createdAt,
)

fun WorkoutEntity.toDomain(exercises: List<WorkoutExercise>) = Workout(
    id = id,
    name = name,
    exercises = exercises,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Workout.toEntity(groupId: String, orderIndex: Int) = WorkoutEntity(
    id = id,
    groupId = groupId,
    name = name,
    orderIndex = orderIndex,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
