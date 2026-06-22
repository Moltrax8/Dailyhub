package com.moltrax.personalnoteapp.ui.screen.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moltrax.personalnoteapp.data.remote.exercisedb.ExerciseDbApi
import com.moltrax.personalnoteapp.data.remote.exercisedb.ExerciseDbItem
import com.moltrax.personalnoteapp.domain.model.Exercise
import com.moltrax.personalnoteapp.domain.model.LoggedExercise
import com.moltrax.personalnoteapp.domain.model.LoggedSet
import com.moltrax.personalnoteapp.domain.model.PlannedSet
import com.moltrax.personalnoteapp.domain.model.Workout
import com.moltrax.personalnoteapp.domain.model.WorkoutExercise
import com.moltrax.personalnoteapp.domain.model.WorkoutGroup
import com.moltrax.personalnoteapp.domain.model.WorkoutSession
import com.moltrax.personalnoteapp.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val exerciseDbApi: ExerciseDbApi,
) : ViewModel() {

    val groups: StateFlow<List<WorkoutGroup>> = workoutRepo.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _exerciseResults = MutableStateFlow<List<Exercise>>(emptyList())
    val exerciseResults: StateFlow<List<Exercise>> = _exerciseResults.asStateFlow()

    private val _bodyParts = MutableStateFlow<List<String>>(emptyList())
    val bodyParts: StateFlow<List<String>> = _bodyParts.asStateFlow()

    private val _liveSession = MutableStateFlow<WorkoutSession?>(null)
    val liveSession: StateFlow<WorkoutSession?> = _liveSession.asStateFlow()

    init { fetchBodyParts() }

    fun addGroup(name: String) {
        viewModelScope.launch {
            workoutRepo.upsertGroup(WorkoutGroup(name = name))
        }
    }

    fun deleteGroup(id: String) { viewModelScope.launch { workoutRepo.deleteGroup(id) } }

    fun addWorkout(group: WorkoutGroup, workoutName: String) {
        viewModelScope.launch {
            workoutRepo.upsertGroup(group.copy(workouts = group.workouts + Workout(name = workoutName)))
        }
    }

    fun searchExercises(query: String) {
        viewModelScope.launch {
            runCatching {
                val items = exerciseDbApi.searchByName(query)
                _exerciseResults.update { items.map { it.toExercise() } }
            }
        }
    }

    private fun fetchBodyParts() {
        viewModelScope.launch {
            runCatching { _bodyParts.update { exerciseDbApi.getBodyParts() } }
        }
    }

    fun startSession(workout: Workout) {
        _liveSession.update {
            WorkoutSession(
                workoutId = workout.id,
                workoutName = workout.name,
                loggedExercises = workout.exercises.map { ex ->
                    LoggedExercise(ex.exerciseId, ex.exerciseName)
                },
            )
        }
    }

    fun logSet(exerciseId: String, set: LoggedSet) {
        _liveSession.update { session ->
            session?.copy(
                loggedExercises = session.loggedExercises.map { ex ->
                    if (ex.exerciseId == exerciseId) ex.copy(sets = ex.sets + set) else ex
                }
            )
        }
    }

    fun finishSession(onDone: (String) -> Unit) {
        viewModelScope.launch {
            val session = _liveSession.value?.copy(completedAt = System.currentTimeMillis()) ?: return@launch
            workoutRepo.saveSession(session)
            _liveSession.update { null }
            onDone(session.id)
        }
    }

    private fun ExerciseDbItem.toExercise() = Exercise(
        id = id, name = name, bodyPart = bodyPart,
        equipment = equipment, mediaUrl = gifUrl,
    )
}
