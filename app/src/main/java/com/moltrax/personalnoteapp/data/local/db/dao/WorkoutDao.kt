package com.moltrax.personalnoteapp.data.local.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.moltrax.personalnoteapp.data.local.db.entity.WorkoutEntity
import com.moltrax.personalnoteapp.data.local.db.entity.WorkoutExerciseEntity
import com.moltrax.personalnoteapp.data.local.db.entity.WorkoutGroupEntity
import com.moltrax.personalnoteapp.data.local.db.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // Groups
    @Query("SELECT * FROM workout_groups ORDER BY createdAt DESC")
    fun observeGroups(): Flow<List<WorkoutGroupEntity>>

    @Upsert suspend fun upsertGroup(group: WorkoutGroupEntity)
    @Query("DELETE FROM workout_groups WHERE id = :id")
    suspend fun deleteGroup(id: String)

    // Workouts
    @Query("SELECT * FROM workouts WHERE groupId = :groupId ORDER BY orderIndex")
    suspend fun getWorkoutsForGroup(groupId: String): List<WorkoutEntity>

    @Upsert suspend fun upsertWorkout(workout: WorkoutEntity)
    @Upsert suspend fun upsertWorkouts(workouts: List<WorkoutEntity>)
    @Query("DELETE FROM workouts WHERE groupId = :groupId")
    suspend fun deleteWorkoutsForGroup(groupId: String)

    // Exercises within workouts
    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY orderIndex")
    suspend fun getExercisesForWorkout(workoutId: String): List<WorkoutExerciseEntity>

    @Upsert suspend fun upsertWorkoutExercise(exercise: WorkoutExerciseEntity)
    @Upsert suspend fun upsertWorkoutExercises(exercises: List<WorkoutExerciseEntity>)
    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesForWorkout(workoutId: String)

    // Sessions
    @Upsert suspend fun upsertSession(session: WorkoutSessionEntity)
    @Query("SELECT * FROM workout_sessions ORDER BY startedAt DESC")
    suspend fun getSessions(): List<WorkoutSessionEntity>
}
