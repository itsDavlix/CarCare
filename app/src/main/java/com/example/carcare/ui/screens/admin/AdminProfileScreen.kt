package com.example.carcare.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carcare.data.AuthSession
import com.example.carcare.ui.components.ChangeOwnPasswordDialog
import com.example.carcare.ui.components.ProfileSettingsSection
import kotlinx.coroutines.launch

/**
 * Perfil del admin (espejo del perfil del conductor). Se abre desde el avatar de la
 * TopBar. Muestra los datos de la sesión y permite cambiar la contraseña y cerrar sesión.
 *
 * @param onChangePassword (actual, nueva, onSuccess, onError) — delega en AuthViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangePassword: (actual: String, nueva: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showChangePassword by remember { mutableStateOf(false) }

    val nombre = AuthSession.nombre?.takeIf { it.isNotBlank() } ?: "Administrador"
    val cedula = AuthSession.cedula ?: "—"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(nombre, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "ADMINISTRADOR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            KeyValueRow("Cédula", cedula, mono = true)
            KeyValueRow("Rol", "Administrador")

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = { showChangePassword = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar contraseña")
            }

            Spacer(modifier = Modifier.height(20.dp))
            ProfileSettingsSection()

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }

    if (showChangePassword) {
        ChangeOwnPasswordDialog(
            onDismiss = { showChangePassword = false },
            onConfirm = { actual, nueva ->
                onChangePassword(
                    actual, nueva,
                    {
                        showChangePassword = false
                        scope.launch { snackbarHostState.showSnackbar("Contraseña actualizada") }
                    },
                    { msg ->
                        showChangePassword = false
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )
            }
        )
    }
}
