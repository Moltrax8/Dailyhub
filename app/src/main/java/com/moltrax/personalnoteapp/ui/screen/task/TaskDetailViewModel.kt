package com.moltrax.personalnoteapp.ui.screen.task

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moltrax.personalnoteapp.data.local.preferences.AppPreferences
import com.moltrax.personalnoteapp.domain.model.Category
import com.moltrax.personalnoteapp.domain.model.Priority
import com.moltrax.personalnoteapp.domain.model.RecurrenceType
import com.moltrax.personalnoteapp.domain.model.SubTask
import com.moltrax.personalnoteapp.domain.model.Task
import com.moltrax.personalnoteapp.domain.model.WorkoutGroup
import com.moltrax.personalnoteapp.domain.repository.CategoryRepository
import com.moltrax.personalnoteapp.domain.repository.SyncRepository
import com.moltrax.personalnoteapp.domain.repository.TaskRepository
import com.moltrax.personalnoteapp.domain.repository.WorkoutRepository
import com.moltrax.personalnoteapp.service.NotificationService
import com.moltrax.personalnoteapp.widget.TaskWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
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
    val recurrenceType: RecurrenceType = RecurrenceType.DAILY,
    val recurrenceDaysOfWeek: List<Int> = emptyList(),
    val focusDurationSeconds: Int = 1500,
    val category: String = "",
    val subtasks: List<SubTask> = emptyList(),
    val linkedWorkoutId: String? = null,
    val linkedProgramId: String? = null,
    val programStartIndex: Int = 0,
    val isNew: Boolean = true,
    val isSaving: Boolean = false,
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskRepo: TaskRepository,
    private val categoryRepo: CategoryRepository,
    private val workoutRepo: WorkoutRepository,
    private val syncRepo: SyncRepository,
    private val notifService: NotificationService,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(TaskDetailState())
    val state: StateFlow<TaskDetailState> = _state.asStateFlow()

    val workoutGroups: StateFlow<List<WorkoutGroup>> = workoutRepo.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Mevcut bir kategoriyi seçer ya da seçimi kaldırır (null). */
    fun selectCategory(name: String?) = _state.update { it.copy(category = name?.trim().orEmpty()) }

    /** Yeni kategori oluşturur (kalıcı olabilir) ve görevde seçili hale getirir. */
    fun createCategory(name: String, isPermanent: Boolean) {
        val n = name.trim()
        if (n.isBlank()) return
        viewModelScope.launch {
            categoryRepo.ensureExists(n, isPermanent)
            _state.update { it.copy(category = n) }
        }
    }

    fun load(taskId: String) {
        if (taskId == "new") return
        viewModelScope.launch {
            taskRepo.getById(taskId)?.let { t ->
                _state.update {
                    it.copy(
                        id = t.id, title = t.title, notes = t.notes ?: "",
                        dueDate = t.dueDate, priority = t.priority,
                        isRecurring = t.isRecurring, intervalDays = t.intervalDays,
                        // Eski (recurrenceType=null) ama intervalDays'li görevler INTERVAL kabul edilir.
                        recurrenceType = t.recurrenceType
                            ?: if (t.intervalDays != null) RecurrenceType.INTERVAL else RecurrenceType.DAILY,
                        recurrenceDaysOfWeek = t.recurrenceDaysOfWeek,
                        focusDurationSeconds = t.focusDurationSeconds,
                        category = t.category ?: "",
                        subtasks = t.subtasks,
                        linkedWorkoutId = t.linkedWorkoutId,
                        linkedProgramId = t.linkedProgramId,
                        programStartIndex = t.programStartIndex,
                        isNew = false,
                    )
                }
            }
        }
    }

    fun update(block: TaskDetailState.() -> TaskDetailState) = _state.update(block)

    // --- Alt görev (checklist) düzenleme ---

    /** Yeni bir alt görev ekler (başlık boşsa yok sayılır). */
    fun addSubtask(title: String) {
        val t = title.trim()
        if (t.isBlank()) return
        _state.update { it.copy(subtasks = it.subtasks + SubTask(title = t)) }
    }

    /** Bir alt görevin tamamlanma durumunu değiştirir. */
    fun toggleSubtask(id: String) = _state.update { s ->
        s.copy(subtasks = s.subtasks.map { if (it.id == id) it.copy(isDone = !it.isDone) else it })
    }

    /** Bir alt görevi siler. */
    fun removeSubtask(id: String) = _state.update { s ->
        s.copy(subtasks = s.subtasks.filterNot { it.id == id })
    }

    /** Haftalık tekrarda bir günü (ISO 1..7) ekler/çıkarır. */
    fun toggleRecurrenceDay(dayIso: Int) = _state.update { s ->
        val days = if (dayIso in s.recurrenceDaysOfWeek) s.recurrenceDaysOfWeek - dayIso
                   else s.recurrenceDaysOfWeek + dayIso
        s.copy(recurrenceDaysOfWeek = days.sorted())
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val s = _state.value
            val now = System.currentTimeMillis()
            val existing = if (s.isNew) null else taskRepo.getById(s.id)
            // Yeni görev listenin başına gelsin: mevcut en küçük sortOrder'dan bir eksiği.
            // Düzenlemede mevcut sıralama korunur.
            val sortOrder = existing?.sortOrder
                ?: ((taskRepo.getAll().minOfOrNull { it.sortOrder } ?: 0L) - 1L)
            val task = Task(
                id = s.id, title = s.title.trim(), notes = s.notes.takeIf { it.isNotBlank() },
                dueDate = s.dueDate, priority = s.priority, isRecurring = s.isRecurring,
                // Tekrar kapalıysa biçim/aralık/gün bilgisini sıfırla; INTERVAL'da gün aralığı şart.
                intervalDays = if (s.isRecurring && s.recurrenceType == RecurrenceType.INTERVAL) s.intervalDays else null,
                recurrenceType = if (s.isRecurring) s.recurrenceType else null,
                recurrenceDaysOfWeek = if (s.isRecurring && s.recurrenceType == RecurrenceType.WEEKLY) s.recurrenceDaysOfWeek else emptyList(),
                focusDurationSeconds = s.focusDurationSeconds,
                category = s.category.takeIf { it.isNotBlank() },
                subtasks = s.subtasks,
                // Tek antrenman ile tüm program bağı birbirini dışlar.
                linkedWorkoutId = if (s.linkedProgramId != null) null else s.linkedWorkoutId,
                linkedProgramId = s.linkedProgramId,
                programStartIndex = s.programStartIndex,
                createdAt = if (s.isNew) now else existing?.createdAt ?: now,
                updatedAt = now,
                sortOrder = sortOrder,
            )
            taskRepo.upsert(task)
            // Program bağında döngü, seçilen başlangıç gününden başlasın: grubun currentIndex'ini ayarla.
            task.linkedProgramId?.let { pid ->
                workoutRepo.getGroups().find { it.id == pid }?.let { group ->
                    val start = task.programStartIndex.coerceIn(0, group.workouts.lastIndex.coerceAtLeast(0))
                    if (group.currentIndex != start) workoutRepo.upsertGroup(group.copy(currentIndex = start))
                }
            }
            // Kategori yaşam döngüsü: seçili kategoriyi kayıt altına al (yoksa geçici oluşur),
            // ardından kategori değişmişse boşa çıkan eski geçici kategorileri temizle.
            task.category?.let { categoryRepo.ensureExists(it) }
            categoryRepo.cleanupTemporary()
            notifService.cancelReminder(task.id)
            if (task.dueDate != null && prefs.systemAlertsEnabled.first()) {
                notifService.scheduleReminder(task, prefs.reminderMinutes.first())
            }
            syncRepo.pushToDrive()
            TaskWidget.requestUpdate(context)
            _state.update { it.copy(isSaving = false) }
            onDone()
        }
    }
}
