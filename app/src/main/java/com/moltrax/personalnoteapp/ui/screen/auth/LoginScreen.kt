package com.moltrax.personalnoteapp.ui.screen.auth

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.moltrax.personalnoteapp.R
import com.moltrax.personalnoteapp.ui.navigation.Home
import com.moltrax.personalnoteapp.ui.navigation.Login

@Composable
fun LoginScreen(nav: NavController, vm: AuthViewModel = hiltViewModel()) {
    val isSignedIn by vm.isSignedIn.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.checkExistingSignIn() }
    // Giriş yapıldıysa doğrudan ana ekrana git (onboarding/fiziksel bilgi adımı kaldırıldı).
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            nav.navigate(Home) { popUpTo<Login> { inclusive = true } }
        }
    }

    val context = LocalContext.current

    // Drive onay ekranından dönüş; sonuç bir sonraki senkronizasyonda görünür olur
    val consentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vm.handleSignInResult(result.data)
            // Hassas drive.appdata izni sign-in ile gelmemiş olabilir; gerekirse onay ekranını aç
            vm.ensureDriveConsent(
                onConsentRequired = { intent -> consentLauncher.launch(intent) },
                onError = { msg -> Toast.makeText(context, context.getString(R.string.login_drive_permission_error, msg), Toast.LENGTH_LONG).show() },
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = null,
            modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.app_name), fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground)

        Spacer(Modifier.height(8.dp))

        Text(stringResource(R.string.login_subtitle), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = { launcher.launch(vm.signInIntent) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(stringResource(R.string.login_button), fontSize = 16.sp)
        }
    }
}
