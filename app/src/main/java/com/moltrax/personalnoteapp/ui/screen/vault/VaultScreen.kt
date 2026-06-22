package com.moltrax.personalnoteapp.ui.screen.vault

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.moltrax.personalnoteapp.domain.model.VaultEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(nav: NavController, vm: VaultViewModel = hiltViewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Şifreli Kasa") },
                navigationIcon = { IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    if (uiState is VaultUiState.Unlocked)
                        IconButton(onClick = { vm.lock() }) { Icon(Icons.Default.Lock, "Kilitle") }
                }
            )
        },
        floatingActionButton = {
            if (uiState is VaultUiState.Unlocked)
                FloatingActionButton(onClick = { /* show add dialog */ }) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val s = uiState) {
                is VaultUiState.PinSetup -> PinSetupContent(onSetup = vm::setupPin)
                is VaultUiState.Locked   -> PinUnlockContent(onUnlock = vm::unlock)
                is VaultUiState.Unlocked -> UnlockedContent(entries = s.entries, onDecrypt = vm::decrypt, onDelete = vm::deleteEntry)
            }
        }
    }
}

@Composable
private fun PinSetupContent(onSetup: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("PIN Oluştur", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(pin, { pin = it }, label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(confirm, { confirm = it }, label = { Text("PIN tekrar") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            if (pin.length < 4) error = "PIN en az 4 karakter olmalı"
            else if (pin != confirm) error = "PIN'ler eşleşmiyor"
            else onSetup(pin)
        }, modifier = Modifier.fillMaxWidth()) { Text("Oluştur") }
    }
}

@Composable
private fun PinUnlockContent(onUnlock: (String) -> Boolean) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Lock, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(pin, { pin = it; error = false }, label = { Text("PIN") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(),
            isError = error)
        if (error) Text("Yanlış PIN", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (!onUnlock(pin)) error = true }, modifier = Modifier.fillMaxWidth()) { Text("Aç") }
    }
}

@Composable
private fun UnlockedContent(entries: List<VaultEntry>, onDecrypt: (VaultEntry) -> String?, onDelete: (String) -> Unit) {
    var selected by remember { mutableStateOf<VaultEntry?>(null) }

    if (selected != null) {
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(selected!!.title) },
            text = { Text(onDecrypt(selected!!) ?: "Şifre çözme hatası") },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Kapat") } },
        )
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.id }) { entry ->
            Card(onClick = { selected = entry }, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(entry.title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onDelete(entry.id) }) { Icon(Icons.Default.Delete, null) }
                }
            }
        }
        if (entries.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henüz not yok", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
