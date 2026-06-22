package com.moltrax.personalnoteapp.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moltrax.personalnoteapp.data.local.preferences.AppPreferences
import com.moltrax.personalnoteapp.domain.model.Priority
import com.moltrax.personalnoteapp.domain.model.SyncStatus
import com.moltrax.personalnoteapp.domain.model.Task
import com.moltrax.personalnoteapp.domain.repository.SyncRepository
import com.moltrax.personalnoteapp.domain.repository.TaskRepository
import com.moltrax.personalnoteapp.service.NotificationService
import com.moltrax.personalnoteapp.widget.TaskWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TaskFilter(
    val showDone: Boolean = false,
    val priority: Priority? = null,
    val category: String? = null,
    val search: String = "",
)

data class HomeUiState(
    val allTasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter(),
    val syncStatus: SyncStatus = SyncStatus.Idle,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepo: TaskRepository,
    private val syncRepo: SyncRepository,
    private val notifService: NotificationService,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _filter = MutableStateFlow(TaskFilter())

    val uiState: StateFlow<HomeUiState> = combine(
        taskRepo.observeAll(),
        syncRepo.syncStatus,
        _filter,
    ) { tasks, sync, filter ->
        val filtered = tasks.filter { task ->
            (filter.showDone || !task.isDone) &&
            (filter.priority == null || task.priority == filter.priority) &&
            (filter.category == null || task.category == filter.category) &&
            (filter.search.isBlank() || task.title.contains(filter.search, ignoreCase = true))
        }
        HomeUiState(tasks, filtered, filter, sync)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch { syncRepo.pushToDrive() }
    }

    fun updateFilter(f: TaskFilter) = _filter.update { f }

    fun toggleDone(task: Task) {
        viewModelScope.launch {
            val done = !task.isDone
            val updated = task.copy(
                isDone = done,
                completedAt = if (done) System.currentTimeMillis() else null,
                updatedAt = System.currentTimeMillis(),
            )
            taskRepo.upsert(updated)
            if (done) notifService.cancelReminder(task.id)
            syncRepo.pushToDrive()
            TaskWidget.requestUpdate(context)
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            notifService.cancelReminder(id)
            taskRepo.delete(id)
            syncRepo.pushToDrive()
            TaskWidget.requestUpdate(context)
        }
    }

    fun sync() { viewModelScope.launch { syncRepo.pushToDrive() } }
}
