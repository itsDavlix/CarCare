package com.example.carcare.data.network

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formato de fecha del backend: ISO "yyyy-MM-dd" (ej. "2025-11-30"). */
private fun apiDateFormatter() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

/** String ISO del backend -> Date. Null o invalido -> null. */
fun parseApiDate(value: String?): Date? =
    if (value.isNullOrBlank()) null
    else runCatching { apiDateFormatter().parse(value) }.getOrNull()

/** Date -> String ISO para enviar al backend. Null -> null. */
fun formatApiDate(date: Date?): String? =
    date?.let { apiDateFormatter().format(it) }