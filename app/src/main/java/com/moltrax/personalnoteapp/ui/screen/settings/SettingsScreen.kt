package com.moltrax.personalnoteapp.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.moltrax.personalnoteapp.ui.navigation.Login

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController, vm: SettingsViewModel = hiltViewModel()) {
    val themeMode       by vm.themeMode.collectAsStateWithLifecycle()
    val geminiKey       by vm.geminiApiKey.collectAsStateWithLifecycle()
    val reminderMinutes by vm.reminderMinutes.collectAsStateWithLifecycle()
    val lastSyncAt      by vm.lastSyncAt.collectAsStateWithLifecycle()

    var geminiKeyInput by remember(geminiKey) { mutableStateOf(geminiKey) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = { IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Theme
            Text("Tema", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system" to "Sistem", "light" to "Aydınlık", "dark" to "Karanlık").forEach { (value, label) ->
                    FilterChip(selected = themeMode == value, onClick = { vm.setTheme(value) }, label = { Text(label) })
                }
            }

            HorizontalDivider()

            // Gemini API Key
            Text("Gemini API Anahtarı", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = geminiKeyInput,
                onValueChange = { geminiKeyInput = it },
                label = { Text("API Anahtarı") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    TextButton(onClick = { vm.setGeminiKey(geminiKeyInput) }) { Text("Kaydet") }
                },
            )

            HorizontalDivider()

            // Reminder
            Text("Hatırlatma: $reminderMinutes dakika önce",
                style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = reminderMinutes.toFloat(),
                onValueChange = { vm.setReminderMinutes(it.toInt()) },
                valueRange = 5f..1440f, steps = 19,
            )

            HorizontalDivider()

            // Sync
            Text("Senkronizasyon", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            lastSyncAt?.let { Text("Son sync: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.syncNow() }, modifier = Modifier.weight(1f)) { Text("Drive'a Gönder") }
                OutlinedButton(onClick = { vm.pullNow() }, modifier = Modifier.weight(1f)) { Text("Drive'dan Al") }
            }

            HorizontalDivider()

            // Sign out
            Button(
                onClick = { vm.signOut { nav.navigate(Login) { popUpTo(0) { inclusive = true } } } },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Çıkış Yap") }
        }
    }
}
