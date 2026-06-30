package com.moltrax.personalnoteapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moltrax.personalnoteapp.data.local.preferences.AppPreferences
import com.moltrax.personalnoteapp.domain.repository.TaskRepository
import com.moltrax.personalnoteapp.service.NotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class RescheduleNotificationsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskRepo: TaskRepository,
    private val notifService: NotificationService,
    private val prefs: AppPreferences,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Sistem uyarıları kapalıysa hiçbir hatırlatma kurma.
        if (!prefs.systemAlertsEnabled.first()) return Result.success()
        val minutes = prefs.reminderMinutes.first()
        taskRepo.getAll()
            .filter { !it.isDone && it.dueDate != null && it.dueDate > System.currentTimeMillis() }
            .forEach { notifService.scheduleReminder(it, minutes) }
        return Result.success()
    }
}
