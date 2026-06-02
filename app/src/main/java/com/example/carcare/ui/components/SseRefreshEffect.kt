package com.example.carcare.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.example.carcare.data.network.SseClient

/**
 * Mantiene una conexion SSE viva mientras el composable este en pantalla.
 * Por cada evento del backend llama [onEntityChanged] con la entidad que cambio.
 * Cierra la conexion al salir de la pantalla.
 */
@Composable
fun SseRefreshEffect(onEntityChanged: (String) -> Unit) {
    val current by rememberUpdatedState(onEntityChanged)
    DisposableEffect(Unit) {
        val connection = SseClient.connect { entidad -> current(entidad) }
        onDispose { connection.cancel() }
    }
}