package com.moltrax.personalnoteapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object Login
@Serializable object Home
@Serializable data class TaskDetail(val taskId: String = "new")
@Serializable object Settings
@Serializable data class FocusTimer(val taskId: String)
@Serializable object Vault
@Serializable object WorkoutList
@Serializable data class WorkoutDetail(val groupId: String)
@Serializable data class LiveWorkout(val workoutId: String, val groupId: String)
@Serializable data class AiCoach(val sessionId: String)
@Serializable object FoodScanner
