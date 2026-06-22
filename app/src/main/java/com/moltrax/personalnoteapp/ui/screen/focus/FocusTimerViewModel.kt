package com.moltrax.personalnoteapp.ui.screen.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moltrax.personalnoteapp.domain.model.Task
import com.moltrax.personalnoteapp.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusState(
    val task: Task? = null,
    val totalSeconds: Int = 1500,
    val remainingSeconds: Int = 1500,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
) {
    val progress: Float get() = if (totalSeconds == 0) 0f else 1f - remainingSeconds.toFloat() / totalSeconds
    val minutesLeft: Int get() = remainingSeconds / 60
    val secondsLeft: Int get() = remainingSeconds % 60
}

@HiltViewModel
class FocusTimerViewModel @Inject constructor(
    private val taskRepo: TaskRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun load(taskId: String) {
        viewModelScope.launch {
            val task = taskRepo.getById(taskId) ?: return@launch
            _state.update { it.copy(task = task, totalSeconds = task.focusDurationSeconds, remainingSeconds = task.focusDurationSeconds) }
        }
    }

    fun start() {
        if (_state.value.isRunning) return
        _state.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 && _state.value.isRunning) {
                delay(1_000)
                _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            if (_state.value.remainingSeconds == 0) {
                _state.update { it.copy(isRunning = false, isCompleted = true) }
                markDone()
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        _state.update { it.copy(isRunning = false) }
    }

    fun reset() {
        timerJob?.cancel()
        _state.update { it.copy(isRunning = false, isCompleted = false, remainingSeconds = it.totalSeconds) }
    }

    private suspend fun markDone() {
        val task = _state.value.task ?: return
        taskRepo.upsert(
            task.copy(isDone = true, completedAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        )
    }

    override fun onCleared() { timerJob?.cancel(); super.onCleared() }
}
