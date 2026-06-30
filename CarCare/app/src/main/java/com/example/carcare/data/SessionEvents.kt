package com.example.carcare.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Señales globales de sesión que la UI observa. Hoy: "la sesión expiró".
 *
 * La emite el interceptor de red (o el SSE) cuando una request CON token recibe 401
 * (token ausente/inválido/vencido). MainActivity la observa para cerrar sesión y
 * volver al login. El 403 (sin permiso) NO la dispara: ese caso no desloguea.
 */
object SessionEvents {

    private val _expired = MutableStateFlow(false)
    val expired: StateFlow<Boolean> = _expired

    /** Marca la sesión como expirada (idempotente). */
    fun signalExpired() {
        _expired.value = true
    }

    /** La UI lo resetea después de navegar al login. */
    fun reset() {
        _expired.value = false
    }
}
