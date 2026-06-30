package com.example.carcare.data.network

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formato de fecha del backend: ISO "yyyy-MM-dd" (ej. "2025-11-30"). */
private fun apiDateFormatter() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

/** 
 * Formato de fecha y hora del backend para transacciones: "yyyy-MM-dd'T'HH:mm:ss".
 * Se usa para registrar el momento exacto de salida, retorno o mantenimiento.
 */
private fun apiDateTimeFormatter() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

/** String ISO del backend -> Date. Null o invalido -> null. */
fun parseApiDate(value: String?): Date? =
    if (value.isNullOrBlank()) null
    else {
        // Intenta parsear con hora primero (ISO LocalDateTime), si falla intenta solo fecha
        val cleaned = value.take(19)
        runCatching { apiDateTimeFormatter().parse(cleaned) }
            .getOrNull() ?: runCatching { apiDateFormatter().parse(value) }.getOrNull()
    }

/** Date -> String ISO (con hora) para enviar al backend. Null -> null. */
fun formatApiDate(date: Date?): String? =
    date?.let { apiDateTimeFormatter().format(it) }
