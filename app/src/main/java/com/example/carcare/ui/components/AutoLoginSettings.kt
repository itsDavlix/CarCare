package com.example.carcare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.carcare.R
import com.example.carcare.data.SessionStore
import kotlinx.coroutines.launch

/**
 * Ajuste de auto-login para el perfil (admin y conductor): on/off de mantener la sesión.
 * Con el auto-login activo, si el usuario vuelve dentro de los 30 min de salir de la app,
 * entra sin re-loguear; pasada esa ventana debe iniciar sesión. Persiste en [SessionStore]
 * y sobrevive al cierre de sesión.
 */
@Composable
fun AutoLoginSettings(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val enabled by SessionStore.autoLoginEnabledFlow()
        .collectAsState(initial = SessionStore.DEFAULT_ENABLED)

    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.settings_autologin),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                stringResource(R.string.settings_autologin_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = enabled,
            onCheckedChange = { scope.launch { SessionStore.setAutoLoginEnabled(it) } }
        )
    }
}
