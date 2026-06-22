package com.moltrax.personalnoteapp.ui.screen.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.moltrax.personalnoteapp.domain.model.WorkoutGroup
import com.moltrax.personalnoteapp.ui.navigation.LiveWorkout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(nav: NavController, groupId: String, vm: WorkoutViewModel = hiltViewModel()) {
    val groups by vm.groups.collectAsStateWithLifecycle()
    val group = remember(groups) { groups.find { it.id == groupId } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Program") },
                navigationIcon = { IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            )
        }
    ) { padding ->
        if (group == null) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return@Scaffold }
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(group.workouts, key = { it.id }) { workout ->
                Card(
                    onClick = {
                        vm.startSession(workout)
                        nav.navigate(LiveWorkout(workout.id, groupId))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(workout.name, style = MaterialTheme.typography.titleMedium)
                            Text("${workout.exercises.size} egzersiz", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
