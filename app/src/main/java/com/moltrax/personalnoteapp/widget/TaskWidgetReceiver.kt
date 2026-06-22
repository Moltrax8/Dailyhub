package com.moltrax.personalnoteapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import androidx.glance.appwidget.state.updateAppWidgetState
import com.moltrax.personalnoteapp.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class TaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TaskWidget()

    @Inject lateinit var taskRepo: TaskRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(Dispatchers.IO).launch {
            val titles = taskRepo.observeAll().first()
                .filter { !it.isDone }
                .take(5)
                .map { it.title }
            val json = Json.encodeToString(titles)
            updateAppWidgetState(context, TaskWidget.TASKS_KEY.name) {
                it[TaskWidget.TASKS_KEY] = json
            }
            glanceAppWidget.updateAll(context)
        }
    }
}
