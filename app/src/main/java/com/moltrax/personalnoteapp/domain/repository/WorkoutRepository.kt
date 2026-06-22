package com.moltrax.personalnoteapp.domain.repository

import com.moltrax.personalnoteapp.domain.model.Exercise
import com.moltrax.personalnoteapp.domain.model.WorkoutGroup
import com.moltrax.personalnoteapp.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    fun observeGroups(): Flow<List<WorkoutGroup>>
    suspend fun upsertGroup(group: WorkoutGroup)
    suspend fun deleteGroup(id: String)
    suspend fun saveSession(session: WorkoutSession)
    suspend fun getSessions(): List<WorkoutSession>

    // Exercise database
    fun observeExercises(): Flow<List<Exercise>>
    suspend fun upsertExercise(exercise: Exercise)
    suspend fun searchExercises(query: String): List<Exercise>
    suspend fun getExercisesByBodyPart(bodyPart: String): List<Exercise>
}
