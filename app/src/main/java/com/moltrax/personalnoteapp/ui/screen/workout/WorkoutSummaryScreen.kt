package com.moltrax.personalnoteapp.ui.screen.workout

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.moltrax.personalnoteapp.R
import com.moltrax.personalnoteapp.domain.model.ExerciseType
import com.moltrax.personalnoteapp.domain.model.LoggedExercise
import com.moltrax.personalnoteapp.domain.model.LoggedSet
import com.moltrax.personalnoteapp.domain.model.WorkoutSession
import com.moltrax.personalnoteapp.ui.i18n.label
import com.moltrax.personalnoteapp.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Tamamlanan bir spor görevinin antrenman sonuç/özet sayfası: o gün yapılan tüm hareketleri ve her
 * hareket için girilen set/tekrar/ağırlık (veya süre/adım) detaylarını tek ekranda gösterir. Spor
 * görevi tamamlandıktan hemen sonra otomatik açılır; ayrıca tamamlananlar listesinde göreve tıklayınca.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(
    nav: NavController,
    sessionId: String,
    vm: WorkoutSummaryViewModel = hiltViewModel(),
) {
    LaunchedEffect(sessionId) { vm.load(sessionId) }
    val session by vm.session.collectAsStateWithLifecycle()
    val loaded by vm.loaded.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workout_summary_title)) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        },
    ) { padding ->
        val s = session
        when {
            !loaded -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            s == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.workout_summary_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> SummaryContent(s, ctx, Modifier.fillMaxSize().padding(padding))
        }
    }
}

private val summaryDateFmt = SimpleDateFormat("d MMM yyyy · HH:mm", Locale.getDefault())

@Composable
private fun SummaryContent(session: WorkoutSession, ctx: Context, modifier: Modifier) {
    val exercises = session.loggedExercises
    val totalSets = exercises.sumOf { it.sets.size }
    val volume = exercises
        .filter { it.type == ExerciseType.WEIGHTLIFTING }
        .flatMap { it.sets }
        .sumOf { (it.weightKg ?: 0.0) * it.reps }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = AppColors.Accent)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(session.workoutName, style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    val ts = session.completedAt ?: session.startedAt
                    Text(summaryDateFmt.format(Date(ts)), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        // Özet metrikler (toplam set + ağırlık hacmi)
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryStat(stringResource(R.string.workout_summary_total_sets), "$totalSets", Modifier.weight(1f))
                if (volume > 0) {
                    SummaryStat(stringResource(R.string.workout_summary_volume),
                        "${volume.roundToInt()} ${stringResource(R.string.unit_kg)}", Modifier.weight(1f))
                }
            }
        }
        item {
            Text(stringResource(R.string.workout_summary_exercises),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(exercises.size) { i ->
            ExerciseSummaryCard(exercises[i], ctx)
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(vertical = 14.dp, horizontal = 12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ExerciseSummaryCard(exercise: LoggedExercise, ctx: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(exercise.exerciseName, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                AssistChip(onClick = {}, enabled = false, label = { Text(exercise.type.label()) })
            }
            Spacer(Modifier.height(8.dp))
            if (exercise.sets.isEmpty()) {
                Text(stringResource(R.string.workout_summary_no_logged),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                exercise.sets.forEachIndexed { i, set ->
                    Text(
                        stringResource(R.string.set_index, i + 1, formatLoggedSetSummary(ctx, exercise.type, set)),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}

/** Tek bir kaydedilen setin tipe göre okunur metni (özet ekranı için). */
private fun formatLoggedSetSummary(ctx: Context, type: ExerciseType, set: LoggedSet): String {
    val kg = ctx.getString(R.string.unit_kg)
    return when (type) {
        ExerciseType.WEIGHTLIFTING ->
            ctx.getString(R.string.logged_reps, set.reps) + (set.weightKg?.takeIf { it > 0 }?.let { " – ${trimKg(it)}$kg" } ?: "")
        ExerciseType.BODYWEIGHT ->
            ctx.getString(R.string.logged_reps, set.reps) +
                (set.weightKg?.takeIf { it > 0 }?.let { " (+${trimKg(it)}$kg)" }
                    ?: " (${ctx.getString(R.string.logged_bodyweight)})")
        ExerciseType.DURATION -> set.durationSeconds?.let { ctx.getString(R.string.logged_seconds, it) }
            ?: ctx.getString(R.string.logged_entry)
        ExerciseType.CARDIO -> buildString {
            set.durationSeconds?.let { append(ctx.getString(R.string.logged_minutes, it / 60)) }
            set.steps?.let { if (isNotEmpty()) append(" · "); append(ctx.getString(R.string.logged_steps, it)) }
        }.ifBlank { ctx.getString(R.string.logged_entry) }
    }
}

private fun trimKg(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(v)
