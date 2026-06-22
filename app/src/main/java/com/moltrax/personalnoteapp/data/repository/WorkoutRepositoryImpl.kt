package com.moltrax.personalnoteapp.data.repository

import com.moltrax.personalnoteapp.data.local.db.dao.ExerciseDao
import com.moltrax.personalnoteapp.data.local.db.dao.WorkoutDao
import com.moltrax.personalnoteapp.data.local.db.entity.toDomain
import com.moltrax.personalnoteapp.data.local.db.entity.toEntity
import com.moltrax.personalnoteapp.domain.model.Exercise
import com.moltrax.personalnoteapp.domain.model.WorkoutGroup
import com.moltrax.personalnoteapp.domain.model.WorkoutSession
import com.moltrax.personalnoteapp.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
) : WorkoutRepository {

    override fun observeGroups(): Flow<List<WorkoutGroup>> =
        workoutDao.observeGroups().map { groups ->
            groups.map { groupEntity ->
                val workouts = workoutDao.getWorkoutsForGroup(groupEntity.id).map { workoutEntity ->
                    val exercises = workoutDao.getExercisesForWorkout(workoutEntity.id).map { it.toDomain() }
                    workoutEntity.toDomain(exercises)
                }
                groupEntity.toDomain(workouts)
            }
        }

    override suspend fun upsertGroup(group: WorkoutGroup) {
        workoutDao.upsertGroup(group.toEntity())
        workoutDao.deleteWorkoutsForGroup(group.id)
        group.workouts.forEachIndexed { idx, workout ->
            workoutDao.upsertWorkout(workout.toEntity(group.id, idx))
            workoutDao.deleteExercisesForWorkout(workout.id)
            workout.exercises.forEach { workoutDao.upsertWorkoutExercise(it.toEntity(workout.id)) }
        }
    }

    override suspend fun deleteGroup(id: String) = workoutDao.deleteGroup(id)

    override suspend fun saveSession(session: WorkoutSession) = workoutDao.upsertSession(session.toEntity())

    override suspend fun getSessions(): List<WorkoutSession> =
        workoutDao.getSessions().map { it.toDomain() }

    override fun observeExercises(): Flow<List<Exercise>> =
        exerciseDao.observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun upsertExercise(exercise: Exercise) = exerciseDao.upsert(exercise.toEntity())

    override suspend fun searchExercises(query: String): List<Exercise> =
        exerciseDao.search(query).map { it.toDomain() }

    override suspend fun getExercisesByBodyPart(bodyPart: String): List<Exercise> =
        exerciseDao.getByBodyPart(bodyPart).map { it.toDomain() }
}
