package com.example.carcare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carcare.R
import com.example.carcare.data.SessionStore
import kotlinx.coroutines.launch

/**
 * Ajuste de auto-login para el perfil (admin y conductor). Cada usuario decide si
 * la app mantiene su sesión iniciada y por cuántos días. Lee/escribe en [SessionStore],
 * que persiste en DataStore y sobrevive al cierre de sesión.
 */
@Composable
fun AutoLoginSettings(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val enabled by SessionStore.autoLoginEnabledFlow()
        .collectAsState(initial = SessionStore.DEFAULT_ENABLED)
    val days by SessionStore.autoLoginDaysFlow()
        .collectAsState(initial = SessionStore.DEFAULT_DAYS)

    // El campo muestra lo que el usuario tipeó; si no tocó nada, el valor persistido.
    // Así un chip o el valor guardado se reflejan, pero al editar no se pisa lo tipeado.
    var customText by remember { mutableStateOf<String?>(null) }
    val fieldText = customText ?: days.toString()

    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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

        if (enabled) {
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.settings_autologin_expires),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SessionStore.DAY_OPTIONS.forEach { option ->
                    FilterChip(
                        selected = customText == null && days == option,
                        onClick = {
                            customText = null // volver a reflejar el valor persistido
                            scope.launch { SessionStore.setAutoLoginDays(option) }
                        },
                        label = { Text(pluralStringResource(R.plurals.settings_days, option, option)) }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            // Tiempo personalizado: el usuario escribe los días (1–365).
            OutlinedTextField(
                value = fieldText,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(3)
                    customText = digits
                    digits.toIntOrNull()?.let { n ->
                        if (n in 1..365) scope.launch { SessionStore.setAutoLoginDays(n) }
                    }
                },
                label = { Text(stringResource(R.string.settings_days_custom)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(190.dp)
            )
        }
    }
}
