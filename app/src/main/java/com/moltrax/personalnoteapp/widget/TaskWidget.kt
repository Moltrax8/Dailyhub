package com.moltrax.personalnoteapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.moltrax.personalnoteapp.ui.theme.AppColors

class TaskWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
        val tasksJson = prefs[TASKS_KEY] ?: "[]"
        val tasks = parseTasksFromJson(tasksJson)

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier.fillMaxSize().background(ColorProvider(AppColors.BgSurface)),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = "Görevlerim",
                        style = TextStyle(color = ColorProvider(AppColors.TextPrimary)),
                        modifier = GlanceModifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                    tasks.take(5).forEach { title ->
                        Text(
                            text = "• $title",
                            style = TextStyle(color = ColorProvider(AppColors.TextSecondary)),
                            modifier = GlanceModifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        )
                    }
                    if (tasks.isEmpty()) {
                        Text("Bekleyen görev yok 🎉",
                            style = TextStyle(color = ColorProvider(AppColors.TextMuted)),
                            modifier = GlanceModifier.padding(12.dp))
                    }
                }
            }
        }
    }

    companion object {
        val TASKS_KEY = androidx.datastore.preferences.core.stringPreferencesKey("widget_tasks")

        suspend fun requestUpdate(context: Context) {
            TaskWidget().updateAll(context)
        }

        fun parseTasksFromJson(json: String): List<String> {
            return runCatching {
                kotlinx.serialization.json.Json.decodeFromString<List<String>>(json)
            }.getOrDefault(emptyList())
        }
    }
}

// Workaround — dp extension not available in Glance scope at top level
private val Int.dp get() = androidx.glance.unit.Dimension(this)
