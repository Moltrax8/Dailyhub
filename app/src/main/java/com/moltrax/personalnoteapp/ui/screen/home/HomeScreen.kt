package com.moltrax.personalnoteapp.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.moltrax.personalnoteapp.domain.model.Priority
import com.moltrax.personalnoteapp.domain.model.SyncStatus
import com.moltrax.personalnoteapp.domain.model.Task
import com.moltrax.personalnoteapp.ui.navigation.*
import com.moltrax.personalnoteapp.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(nav: NavController, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = { nav.navigate(TaskDetail("new")) },
                containerColor = AppColors.Accent) {
                Icon(Icons.Default.Add, contentDescription = "Yeni görev", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        bottomBar = { BottomNavBar(nav) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SyncBar(state.syncStatus) { vm.sync() }
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                item {
                    Row(Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Görevlerim", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                        val pending = state.allTasks.count { !it.isDone }
                        if (pending > 0) Badge(containerColor = AppColors.AccentGlow,
                            contentColor = AppColors.Accent) { Text("$pending bekliyor") }
                    }
                }
                items(state.filteredTasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onToggle = { vm.toggleDone(task) },
                        onTap = { nav.navigate(TaskDetail(task.id)) },
                        onFocus = { nav.navigate(FocusTimer(task.id)) },
                        onDelete = { vm.deleteTask(task.id) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (state.filteredTasks.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Görev yok 🎉", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncBar(status: SyncStatus, onSync: () -> Unit) {
    val (text, color) = when (status) {
        is SyncStatus.Syncing -> "Senkronize ediliyor…" to MaterialTheme.colorScheme.primary
        is SyncStatus.Synced  -> "Senkronize edildi"    to AppColors.Success
        is SyncStatus.Error   -> "Sync hatası"          to AppColors.Error
        else                  -> return
    }
    Surface(color = color.copy(alpha = 0.15f), modifier = Modifier.fillMaxWidth().clickable(onClick = onSync)) {
        Text(text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit, onTap: () -> Unit, onFocus: () -> Unit, onDelete: () -> Unit) {
    val priorityColor = when (task.priority) {
        Priority.HIGH   -> AppColors.PriorityHigh
        Priority.MEDIUM -> AppColors.PriorityMedium
        Priority.LOW    -> AppColors.PriorityLow
    }
    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(priorityColor))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.SemiBold,
                    color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                task.dueDate?.let {
                    val fmt = SimpleDateFormat("d MMM HH:mm", Locale.getDefault())
                    Text(fmt.format(Date(it)), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Checkbox(checked = task.isDone, onCheckedChange = { onToggle() })
            IconButton(onClick = onFocus, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Timer, contentDescription = "Odak", modifier = Modifier.size(18.dp),
                    tint = AppColors.Accent)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", modifier = Modifier.size(18.dp),
                    tint = AppColors.PriorityHigh)
            }
        }
    }
}

@Composable
fun BottomNavBar(nav: NavController) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.CheckCircle, null) }, label = { Text("Görevler") })
        NavigationBarItem(selected = false, onClick = { nav.navigate(WorkoutList) }, icon = { Icon(Icons.Default.FitnessCenter, null) }, label = { Text("Antrenman") })
        NavigationBarItem(selected = false, onClick = { nav.navigate(Vault) }, icon = { Icon(Icons.Default.Lock, null) }, label = { Text("Kasa") })
        NavigationBarItem(selected = false, onClick = { nav.navigate(FoodScanner) }, icon = { Icon(Icons.Default.CameraAlt, null) }, label = { Text("Besin") })
        NavigationBarItem(selected = false, onClick = { nav.navigate(Settings) }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Ayarlar") })
    }
}
