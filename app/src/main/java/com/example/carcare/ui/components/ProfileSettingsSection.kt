package com.example.carcare.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Sección de ajustes del perfil, compartida por admin y conductor: auto-login + tema,
 * entre divisores. Centraliza los ajustes para que sumar uno nuevo se haga en un solo lugar.
 */
@Composable
fun ProfileSettingsSection(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        AutoLoginSettings()
        Spacer(Modifier.height(20.dp))
        ThemeSettings()
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
    }
}
