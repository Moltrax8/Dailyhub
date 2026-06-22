package com.moltrax.personalnoteapp.ui.screen.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.moltrax.personalnoteapp.domain.model.LoggedSet
import com.moltrax.personalnoteapp.ui.navigation.AiCoach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveWorkoutScreen(nav: NavController, workoutId: String, groupId: String, vm: WorkoutViewModel = hiltViewModel()) {
    val session by vm.liveSession.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.workoutName ?: "Antrenman") },
                navigationIcon = { IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    TextButton(onClick = {
                        vm.finishSession { sessionId -> nav.navigate(AiCoach(sessionId)) }
                    }) { Text("Bitir") }
                }
            )
        }
    ) { padding ->
        val exercises = session?.loggedExercises ?: emptyList()
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            itemsIndexed(exercises) { _, ex ->
                var repsInput by remember { mutableStateOf("") }
                var weightInput by remember { mutableStateOf("") }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(ex.exerciseName, style = MaterialTheme.typography.titleMedium)
                        Text("${ex.sets.size} set tamamlandı", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(repsInput, { repsInput = it }, label = { Text("Tekrar") },
                                modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(weightInput, { weightInput = it }, label = { Text("Kg") },
                                modifier = Modifier.weight(1f), singleLine = true)
                            IconButton(onClick = {
                                val reps = repsInput.toIntOrNull() ?: return@IconButton
                                val weight = weightInput.toDoubleOrNull()
                                vm.logSet(ex.exerciseId, LoggedSet(reps, weight))
                                repsInput = ""; weightInput = ""
                            }) { Icon(Icons.Default.Add, null) }
                        }
                        ex.sets.forEachIndexed { i, set ->
                            Text("Set ${i+1}: ${set.reps} tekrar${set.weightKg?.let { " – ${it}kg" } ?: ""}",
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
