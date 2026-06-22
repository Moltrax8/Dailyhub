package com.moltrax.personalnoteapp.ui.screen.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moltrax.personalnoteapp.data.local.preferences.AppPreferences
import com.moltrax.personalnoteapp.domain.model.Priority
import com.moltrax.personalnoteapp.domain.model.Task
import com.moltrax.personalnoteapp.domain.repository.SyncRepository
import com.moltrax.personalnoteapp.domain.repository.TaskRepository
import com.moltrax.personalnoteapp.service.NotificationService
import com.moltrax.personalnoteapp.widget.TaskWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TaskDetailState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val notes: String = "",
    val dueDate: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val isRecurring: Boolean = false,
    val intervalDays: Int? = null,
    val focusDurationSeconds: Int = 1500,
    val category: String = "",
    val isNew: Boolean = true,
    val isSaving: Boolean = false,
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepo: TaskRepository,
    private val syncRepo: SyncRepository,
    private val notifService: NotificationService,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(TaskDetailState())
    val state: StateFlow<TaskDetailState> = _state.asStateFlow()

    fun load(taskId: String) {
        if (taskId == "new") return
        viewModelScope.launch {
            taskRepo.getById(taskId)?.let { t ->
                _state.update {
                    it.copy(
                        id = t.id, title = t.title, notes = t.notes ?: "",
                        dueDate = t.dueDate, priority = t.priority,
                        isRecurring = t.isRecurring, intervalDays = t.intervalDays,
                        focusDurationSeconds = t.focusDurationSeconds,
                        category = t.category ?: "", isNew = false,
                    )
                }
            }
        }
    }

    fun update(block: TaskDetailState.() -> TaskDetailState) = _state.update(block)

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val s = _state.value
            val now = System.currentTimeMillis()
            val task = Task(
                id = s.id, title = s.title.trim(), notes = s.notes.takeIf { it.isNotBlank() },
                dueDate = s.dueDate, priority = s.priority, isRecurring = s.isRecurring,
                intervalDays = s.intervalDays, focusDurationSeconds = s.focusDurationSeconds,
                category = s.category.takeIf { it.isNotBlank() },
                createdAt = if (s.isNew) now else taskRepo.getById(s.id)?.createdAt ?: now,
                updatedAt = now,
            )
            taskRepo.upsert(task)
            val minutes = prefs.reminderMinutes.first()
            notifService.cancelReminder(task.id)
            if (task.dueDate != null) notifService.scheduleReminder(task, minutes)
            syncRepo.pushToDrive()
            TaskWidget.requestUpdate(context)
            _state.update { it.copy(isSaving = false) }
            onDone()
        }
    }
}
