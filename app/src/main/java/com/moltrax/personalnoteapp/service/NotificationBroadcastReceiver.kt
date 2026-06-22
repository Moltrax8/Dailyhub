package com.moltrax.personalnoteapp.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.moltrax.personalnoteapp.MainActivity
import com.moltrax.personalnoteapp.R

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId    = intent.getStringExtra("task_id") ?: return
        val title     = intent.getStringExtra("task_title") ?: "Task"
        val minutes   = intent.getIntExtra("reminder_minutes", 60)

        val tapIntent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, taskId.hashCode(), tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val body = when {
            minutes <= 0   -> "Görevin şu an için planlanmış!"
            minutes < 60   -> "$minutes dakika kaldı"
            minutes == 60  -> "1 saat kaldı"
            else           -> "${minutes / 60} saat kaldı"
        }

        val notif = NotificationCompat.Builder(context, "task_reminders")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ $title")
            .setContentText(body)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(taskId.hashCode(), notif)
    }
}
