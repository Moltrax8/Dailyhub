package com.moltrax.personalnoteapp.ui.screen.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.moltrax.personalnoteapp.ui.navigation.Home
import com.moltrax.personalnoteapp.ui.navigation.Login

@Composable
fun LoginScreen(nav: NavController, vm: AuthViewModel = hiltViewModel()) {
    val isSignedIn by vm.isSignedIn.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.checkExistingSignIn() }
    LaunchedEffect(isSignedIn) { if (isSignedIn) nav.navigate(Home) { popUpTo<Login> { inclusive = true } } }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) vm.handleSignInResult(result.data)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = null,
            modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(24.dp))

        Text("PersonalNoteApp", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(8.dp))

        Text("Google hesabınla giriş yap", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = { launcher.launch(vm.signInIntent) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Google ile Giriş Yap", fontSize = 16.sp)
        }
    }
}
