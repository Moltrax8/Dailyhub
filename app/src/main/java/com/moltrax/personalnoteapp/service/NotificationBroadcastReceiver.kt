package com.moltrax.personalnoteapp.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.moltrax.personalnoteapp.MainActivity
import com.moltrax.personalnoteapp.R
import com.moltrax.personalnoteapp.ui.i18n.localizedFor
import com.moltrax.personalnoteapp.widget.TaskWidget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId    = intent.getStringExtra("task_id") ?: return
        val title     = intent.getStringExtra("task_title") ?: "Task"
        val minutes   = intent.getIntExtra("reminder_minutes", 60)

        // Seçili dile göre yerelleştir. DataStore okuması tek seferlik ve kısa olduğundan onReceive
        // içinde runBlocking ile alınır (alarm tetiklemesi nadir ve anlık).
        val lang = runCatching {
            runBlocking { TaskWidget.entryPoint(context).appPreferences().language.first() }
        }.getOrDefault("tr")
        val ctx = context.localizedFor(lang)

        val tapIntent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, taskId.hashCode(), tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Bitiş saatine kalan süreye göre standart hatırlatma metni.
        val timeLeft = when {
            minutes <= 0   -> ctx.getString(R.string.notif_time_very_short)
            minutes < 60   -> ctx.getString(R.string.notif_time_minutes, minutes)
            minutes == 60  -> ctx.getString(R.string.notif_time_one_hour)
            else           -> ctx.getString(R.string.notif_time_hours, minutes / 60)
        }
        val body = ctx.getString(R.string.notif_body, title, timeLeft)

        val notif = NotificationCompat.Builder(context, "task_reminders")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(ctx.getString(R.string.notif_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(taskId.hashCode(), notif)
    }
}
