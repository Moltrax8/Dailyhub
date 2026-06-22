package com.moltrax.personalnoteapp.ui.screen.aicoach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moltrax.personalnoteapp.data.local.preferences.AppPreferences
import com.moltrax.personalnoteapp.domain.repository.WorkoutRepository
import com.moltrax.personalnoteapp.service.GeminiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiCoachState(
    val feedback: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val geminiService: GeminiService,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(AiCoachState())
    val state: StateFlow<AiCoachState> = _state.asStateFlow()

    fun loadAndCoach(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val session = workoutRepo.getSessions().find { it.id == sessionId }
            if (session == null) {
                _state.update { it.copy(isLoading = false, error = "Antrenman bulunamadı.") }
                return@launch
            }
            val key = prefs.geminiApiKey.first()
            if (key.isBlank()) {
                _state.update { it.copy(isLoading = false, error = "Gemini API anahtarı ayarlanmamış.") }
                return@launch
            }
            runCatching { geminiService.coachWorkout(session, key) }
                .onSuccess { f -> _state.update { it.copy(isLoading = false, feedback = f) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
