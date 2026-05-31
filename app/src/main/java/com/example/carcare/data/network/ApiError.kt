package com.example.carcare.data.network

import retrofit2.HttpException
import java.io.IOException

/**
 * Extrae un mensaje legible de una excepcion de red.
 * Para errores HTTP del backend, saca el campo "message" del JSON de error (ErrorResponseDTO).
 * Para fallos de conexion, devuelve un mensaje generico.
 */
fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> {
        val body = runCatching { response()?.errorBody()?.string() }.getOrNull()
        val parsed = body?.let {
            Regex("\"message\"\\s*:\\s*\"([^\"]*)\"").find(it)?.groupValues?.getOrNull(1)
        }
        parsed?.takeIf { it.isNotBlank() } ?: "Error ${code()} del servidor"
    }
    is IOException -> "Sin conexión. Revisá tu internet e intentá de nuevo."
    else -> message ?: "Error desconocido"
}