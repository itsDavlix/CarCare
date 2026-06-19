package com.example.carcare.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.carcare.R
import com.example.carcare.data.SessionStore
import kotlinx.coroutines.launch

/**
 * Selector de tema (Sistema / Claro / Oscuro) para el perfil. La preferencia se guarda
 * en [SessionStore] y la aplica MainActivity al armar el tema. Sobrevive al logout.
 */
@Composable
fun ThemeSettings(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val mode by SessionStore.themeModeFlow().collectAsState(initial = SessionStore.THEME_SYSTEM)

    val options = listOf(
        SessionStore.THEME_SYSTEM to stringResource(R.string.settings_theme_system),
        SessionStore.THEME_LIGHT to stringResource(R.string.settings_theme_light),
        SessionStore.THEME_DARK to stringResource(R.string.settings_theme_dark),
    )

    Column(modifier.fillMaxWidth()) {
        Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (value, label) ->
                FilterChip(
                    selected = mode == value,
                    onClick = { scope.launch { SessionStore.setThemeMode(value) } },
                    label = { Text(label) }
                )
            }
        }
    }
}
