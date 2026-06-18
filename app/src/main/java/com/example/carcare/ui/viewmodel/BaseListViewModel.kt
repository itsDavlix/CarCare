package com.example.carcare.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.AuthSession
import com.example.carcare.data.network.toUserMessage
import com.example.carcare.data.repository.CrudRepository
import com.example.carcare.model.Identifiable
import kotlinx.coroutines.launch

/**
 * ViewModel base para listas CRUD respaldadas por la API.
 *
 * El estado de la lista vive en un `mutableStateOf<List<T>>` INMUTABLE: cada cambio
 * reemplaza la referencia por una lista nueva. Es clave para que CUALQUIER lector
 * recomponga de forma confiable —incluido el dashboard, que deriva sus datos dentro
 * de `remember(lista)`—. (Con `mutableStateListOf` la referencia no cambiaba al mutar
 * en sitio, así que esos `remember` quedaban congelados en la lista vacía.)
 *
 * Centraliza carga, error, búsqueda y los updates/borrados OPTIMISTAS con rollback.
 * Todas las mutaciones son funcionales (producen una lista nueva).
 */
abstract class BaseListViewModel<T : Identifiable, R : CrudRepository<T>>(
    protected val repository: R,
    private val logTag: String
) : ViewModel() {

    var items by mutableStateOf<List<T>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    init {
        load()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun clearError() {
        errorMessage = null
    }

    /**
     * Carga completa desde la API. Solo en el arranque o en un refresco manual.
     *
     * Con la API cerrada (Fase 2) un GET sin token da 403: como estos ViewModels se
     * crean ANTES del login (en el arranque), se omite la carga mientras no haya sesión.
     * Los paneles vuelven a llamar a [load] al entrar, ya con el token puesto.
     */
    fun load() {
        if (!AuthSession.isLoggedIn) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                items = repository.getAll()
            } catch (e: Exception) {
                fail("load", e)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Recarga desde la API SIN indicador de carga ni pisar la UI con error.
     * Para refrescos por evento SSE (cambios hechos en otro dispositivo).
     */
    fun reloadSilently() {
        if (!AuthSession.isLoggedIn) return
        viewModelScope.launch {
            try {
                items = repository.getAll()
            } catch (e: Exception) {
                Log.e(logTag, "reloadSilently", e)
            }
        }
    }

    /** Crea en el backend y agrega la entidad devuelta (con su id real). Sin recargar todo. */
    protected fun create(item: T, onSuccess: () -> Unit = {}) =
        createWith(onSuccess) { repository.create(item) }

    /**
     * Como [create] pero con una llamada de red propia (p. ej. el "reportar" del conductor,
     * que crea el mantenimiento y deja el vehículo en revisión en una sola operación).
     */
    protected fun createWith(onSuccess: () -> Unit = {}, networkCall: suspend () -> T) {
        viewModelScope.launch {
            try {
                items = items + networkCall()
                onSuccess()
            } catch (e: Exception) {
                fail("create", e)
            }
        }
    }

    /**
     * Reemplazo OPTIMISTA por id: aplica [updated] al instante y revierte al valor
     * anterior si [networkCall] falla. Tras el éxito sincroniza con la respuesta del backend.
     */
    protected fun optimisticReplace(updated: T, networkCall: suspend () -> T) {
        val previous = items.firstOrNull { it.id == updated.id }
        replaceById(updated)
        viewModelScope.launch {
            try {
                replaceById(networkCall())
            } catch (e: Exception) {
                previous?.let { replaceById(it) }
                fail("update", e)
            }
        }
    }

    /**
     * Operación de red que devuelve la entidad actualizada y la refleja en la lista
     * (NO optimista: espera la respuesta). Para flujos como "completar".
     */
    protected fun replaceFromNetwork(onSuccess: () -> Unit = {}, networkCall: suspend () -> T) {
        viewModelScope.launch {
            try {
                replaceById(networkCall())
                onSuccess()
            } catch (e: Exception) {
                fail("update", e)
            }
        }
    }

    /** Borrado OPTIMISTA por id: lo saca al instante y lo reinserta si la red falla. */
    protected fun optimisticDelete(id: String, onSuccess: () -> Unit = {}) {
        val index = items.indexOfFirst { it.id == id }
        val backup = items.getOrNull(index)
        if (index >= 0) items = items.filterNot { it.id == id }
        viewModelScope.launch {
            try {
                repository.delete(id)
                onSuccess()
            } catch (e: Exception) {
                if (backup != null) {
                    items = items.toMutableList().apply { add(index.coerceIn(0, size), backup) }
                }
                fail("delete", e)
            }
        }
    }

    /** Búsqueda case-insensitive reutilizable; true cuando la query está vacía. */
    protected fun matchesQuery(vararg fields: String?): Boolean =
        searchQuery.isBlank() || fields.any { it?.contains(searchQuery, ignoreCase = true) == true }

    private fun replaceById(item: T) {
        items = items.map { if (it.id == item.id) item else it }
    }

    private fun fail(op: String, e: Throwable) {
        errorMessage = e.toUserMessage()
        Log.e(logTag, op, e)
    }
}