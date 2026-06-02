package com.example.carcare.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.network.toUserMessage
import com.example.carcare.data.repository.CrudRepository
import com.example.carcare.model.Identifiable
import kotlinx.coroutines.launch

/**
 * ViewModel base para listas CRUD respaldadas por la API.
 *
 * Centraliza el estado reactivo (lista, carga, error, búsqueda) y la mecánica
 * repetida de las 4 entidades: carga completa, alta con id real, y
 * updates/borrados OPTIMISTAS con rollback automático si la red falla.
 *
 * @param T tipo de dominio, identificable por Identifiable.id.
 * @param R repositorio concreto; expone el CRUD común vía CrudRepository y,
 *          en cada subclase, sus operaciones propias (update, estado, etc.).
 */
abstract class BaseListViewModel<T : Identifiable, R : CrudRepository<T>>(
    protected val repository: R,
    private val logTag: String
) : ViewModel() {

    private val _items = mutableStateListOf<T>()
    val items: List<T> get() = _items

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

    /** Carga completa desde la API. Solo en el arranque o en un refresco manual. */
    fun load() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = repository.getAll()
                _items.clear()
                _items.addAll(result)
            } catch (e: Exception) {
                fail("load", e)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Recarga desde la API SIN mostrar el indicador de carga ni pisar la UI con
     * un error. Para refrescos por evento SSE (cambios hechos en otro dispositivo).
     */
    fun reloadSilently() {
        viewModelScope.launch {
            try {
                val result = repository.getAll()
                _items.clear()
                _items.addAll(result)
            } catch (e: Exception) {
                Log.e(logTag, "reloadSilently", e)
            }
        }
    }

    /** Crea en el backend y agrega la entidad devuelta (con su id real). Sin recargar todo. */
    protected fun create(item: T, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _items.add(repository.create(item))
                onSuccess()
            } catch (e: Exception) {
                fail("create", e)
            }
        }
    }

    /**
     * Reemplazo OPTIMISTA por id: aplica [updated] al instante y revierte al valor
     * anterior si [networkCall] falla. Tras el éxito sincroniza con lo que devuelve el backend.
     */
    protected fun optimisticReplace(updated: T, networkCall: suspend () -> T) {
        val previous = _items.firstOrNull { it.id == updated.id }
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
     * Ejecuta una operación de red que devuelve la entidad actualizada y la refleja
     * en la lista (NO optimista: espera la respuesta). Para flujos como "completar".
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
        val index = _items.indexOfFirst { it.id == id }
        val backup = _items.getOrNull(index)
        if (index >= 0) _items.removeAt(index)
        viewModelScope.launch {
            try {
                repository.delete(id)
                onSuccess()
            } catch (e: Exception) {
                if (backup != null) _items.add(index.coerceIn(0, _items.size), backup)
                fail("delete", e)
            }
        }
    }

    /** Búsqueda case-insensitive reutilizable; true cuando la query está vacía. */
    protected fun matchesQuery(vararg fields: String?): Boolean =
        searchQuery.isBlank() || fields.any { it?.contains(searchQuery, ignoreCase = true) == true }

    private fun replaceById(item: T) {
        val i = _items.indexOfFirst { it.id == item.id }
        if (i >= 0) _items[i] = item
    }

    private fun fail(op: String, e: Throwable) {
        errorMessage = e.toUserMessage()
        Log.e(logTag, op, e)
    }
}