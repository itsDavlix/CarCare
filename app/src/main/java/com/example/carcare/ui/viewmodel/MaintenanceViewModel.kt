package com.example.carcare.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.repository.MaintenanceRepository
import com.example.carcare.model.Maintenance
import com.example.carcare.model.MaintenanceStatus
import kotlinx.coroutines.launch

class MaintenanceViewModel : ViewModel() {

    private val repository = MaintenanceRepository()

    private val _maintenances = mutableStateListOf<Maintenance>()
    val maintenances: List<Maintenance> get() = _maintenances

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadMaintenances()
    }

    fun loadMaintenances() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = repository.getAll()
                _maintenances.clear()
                _maintenances.addAll(result)
            } catch (e: Exception) {
                errorMessage = "Error al cargar mantenimientos: ${e.message}"
                Log.e("MaintenanceVM", "loadMaintenances", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun addMaintenance(maintenance: Maintenance) {
        viewModelScope.launch {
            try {
                val created = repository.create(maintenance)
                _maintenances.add(created)
            } catch (e: Exception) {
                errorMessage = "Error al crear mantenimiento: ${e.message}"
                Log.e("MaintenanceVM", "addMaintenance", e)
            }
        }
    }

    fun updateMaintenance(updatedMaintenance: Maintenance) {
        val index = _maintenances.indexOfFirst { it.id == updatedMaintenance.id }
        val previous = if (index != -1) _maintenances[index] else null
        if (index != -1) _maintenances[index] = updatedMaintenance

        viewModelScope.launch {
            try {
                val saved = repository.update(updatedMaintenance)
                val i = _maintenances.indexOfFirst { it.id == saved.id }
                if (i != -1) _maintenances[i] = saved
            } catch (e: Exception) {
                if (previous != null) {
                    val i = _maintenances.indexOfFirst { it.id == previous.id }
                    if (i != -1) _maintenances[i] = previous
                }
                errorMessage = "Error al actualizar mantenimiento: ${e.message}"
                Log.e("MaintenanceVM", "updateMaintenance", e)
            }
        }
    }

    fun deleteMaintenance(maintenanceId: String) {
        val index = _maintenances.indexOfFirst { it.id == maintenanceId }
        val backup = if (index != -1) _maintenances[index] else null
        if (index != -1) _maintenances.removeAt(index)

        viewModelScope.launch {
            try {
                repository.delete(maintenanceId)
            } catch (e: Exception) {
                if (backup != null) {
                    _maintenances.add(index.coerceIn(0, _maintenances.size), backup)
                }
                errorMessage = "Error al eliminar mantenimiento: ${e.message}"
                Log.e("MaintenanceVM", "deleteMaintenance", e)
            }
        }
    }

    fun updateStatus(maintenanceId: String, newStatus: MaintenanceStatus) {
        val index = _maintenances.indexOfFirst { it.id == maintenanceId }
        val previous = if (index != -1) _maintenances[index] else null
        if (index != -1) _maintenances[index] = _maintenances[index].copy(status = newStatus)

        viewModelScope.launch {
            try {
                val saved = repository.changeStatus(maintenanceId, newStatus)
                val i = _maintenances.indexOfFirst { it.id == saved.id }
                if (i != -1) _maintenances[i] = saved
            } catch (e: Exception) {
                if (previous != null) {
                    val i = _maintenances.indexOfFirst { it.id == previous.id }
                    if (i != -1) _maintenances[i] = previous
                }
                errorMessage = "Error al cambiar estado: ${e.message}"
                Log.e("MaintenanceVM", "updateStatus", e)
            }
        }
    }

    /** Filtro local sobre los mantenimientos ya cargados. Sincrono, como antes. */
    fun getHistoryForVehicle(vehicleId: String): List<Maintenance> =
        _maintenances.filter { it.vehicleId == vehicleId }

    fun clearError() {
        errorMessage = null
    }
}