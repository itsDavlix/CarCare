package com.example.carcare.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.repository.NotificacionRepository
import com.example.carcare.model.Notificacion
import kotlinx.coroutines.launch

/**
 * Feed de notificaciones. Carga el del admin o el de un conductor según quién entró;
 * se refresca por el evento SSE "notificaciones". Los errores son silenciosos: el
 * feed nunca debe romper la pantalla.
 */
class NotificacionViewModel(
    private val repository: NotificacionRepository = NotificacionRepository()
) : ViewModel() {

    var items by mutableStateOf<List<Notificacion>>(emptyList())
        private set

    private var conductorId: Long? = null

    val unreadCount: Int get() = items.count { !it.read }

    fun loadForAdmin() {
        conductorId = null
        reload()
    }

    fun loadForConductor(id: Long) {
        conductorId = id
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            try {
                items = conductorId?.let { repository.listConductor(it) } ?: repository.listAdmin()
            } catch (_: Exception) {
                // feed silencioso
            }
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch {
            try {
                repository.markRead(id)
                reload()
            } catch (_: Exception) {
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            try {
                repository.markAll(conductorId)
                reload()
            } catch (_: Exception) {
            }
        }
    }
}
