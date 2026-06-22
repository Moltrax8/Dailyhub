package com.moltrax.personalnoteapp.ui.screen.aicoach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachScreen(nav: NavController, sessionId: String, vm: AiCoachViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(sessionId) { vm.loadAndCoach(sessionId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Koç") },
                navigationIcon = { IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { vm.loadAndCoach(sessionId) }) { Text("Tekrar Dene") }
                }
                state.feedback != null -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
                ) {
                    Text("Antrenman Analizi", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(state.feedback!!, modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
