package com.moltrax.personalnoteapp.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.moltrax.personalnoteapp.domain.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID      = "task_reminders"
private const val CHANNEL_NAME    = "Task Reminders"

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    init { createChannels() }

    private fun createChannels() {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            )
        }
        // Oyunlaştırma kaldırıldı: eski "Sistem Mesajları" (Ceza Bölgesi) kanalını temizle.
        nm.deleteNotificationChannel("system_messages")
    }

    fun scheduleReminder(task: Task, reminderMinutes: Int = 60) {
        if (task.dueDate == null) return
        val now = System.currentTimeMillis()
        val due = task.dueDate
        if (due <= now) return

        val triggerAt = due - reminderMinutes * 60_000L
        val fireAt    = if (triggerAt > now) triggerAt else due
        if (fireAt <= now) return

        val notifIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("reminder_minutes", reminderMinutes)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            notifIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val am = context.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt, pi)
        }
    }

    fun cancelReminder(taskId: String) {
        val pi = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            Intent(context, NotificationBroadcastReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pi)
        pi.cancel()
    }
}
