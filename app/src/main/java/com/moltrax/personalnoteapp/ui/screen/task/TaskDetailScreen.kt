package com.moltrax.personalnoteapp.ui.screen.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.moltrax.personalnoteapp.domain.model.Priority
import com.moltrax.personalnoteapp.ui.theme.AppColors
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(nav: NavController, taskId: String, vm: TaskDetailViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) { vm.load(taskId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "Yeni Görev" else "Görevi Düzenle") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    TextButton(
                        onClick = { vm.save { nav.popBackStack() } },
                        enabled = state.title.isNotBlank() && !state.isSaving,
                    ) { Text("Kaydet") }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { vm.update { copy(title = it) } },
                label = { Text("Başlık") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.notes,
                onValueChange = { vm.update { copy(notes = it) } },
                label = { Text("Notlar") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5,
            )
            OutlinedTextField(
                value = state.category,
                onValueChange = { vm.update { copy(category = it) } },
                label = { Text("Kategori") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Priority
            Text("Öncelik", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    val color = when (p) {
                        Priority.HIGH -> AppColors.PriorityHigh
                        Priority.MEDIUM -> AppColors.PriorityMedium
                        Priority.LOW -> AppColors.PriorityLow
                    }
                    FilterChip(
                        selected = state.priority == p,
                        onClick = { vm.update { copy(priority = p) } },
                        label = { Text(p.name) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.copy(.2f)),
                    )
                }
            }

            // Focus duration
            Text("Odak süresi: ${state.focusDurationSeconds / 60} dk",
                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = state.focusDurationSeconds.toFloat(),
                onValueChange = { vm.update { copy(focusDurationSeconds = it.toInt()) } },
                valueRange = 300f..7200f, steps = 23,
            )

            // Recurring
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tekrarlayan", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = state.isRecurring, onCheckedChange = { vm.update { copy(isRecurring = it) } })
            }
            if (state.isRecurring) {
                OutlinedTextField(
                    value = state.intervalDays?.toString() ?: "",
                    onValueChange = { vm.update { copy(intervalDays = it.toIntOrNull()) } },
                    label = { Text("Gün aralığı") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
    }
}
