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
import com.moltrax.personalnoteapp.ui.navigation.WorkoutDetail
import com.moltrax.personalnoteapp.ui.screen.home.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(nav: NavController, vm: WorkoutViewModel = hiltViewModel()) {
    val groups by vm.groups.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Yeni Program") },
            text = {
                OutlinedTextField(newGroupName, { newGroupName = it }, label = { Text("Program adı") },
                    modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newGroupName.isNotBlank()) { vm.addGroup(newGroupName); newGroupName = ""; showAddDialog = false }
                }) { Text("Ekle") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("İptal") } },
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Antrenmanlarım") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, null) }
        },
        bottomBar = { BottomNavBar(nav) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(groups, key = { it.id }) { group ->
                GroupCard(group, onTap = { nav.navigate(WorkoutDetail(group.id)) }, onDelete = { vm.deleteGroup(group.id) })
            }
            if (groups.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Henüz program yok", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupCard(group: WorkoutGroup, onTap: () -> Unit, onDelete: () -> Unit) {
    Card(onClick = onTap, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FitnessCenter, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(group.name, style = MaterialTheme.typography.titleMedium)
                Text("${group.workouts.size} antrenman", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null) }
        }
    }
}
