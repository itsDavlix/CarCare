package com.example.carcare.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.repository.DriverRepository
import com.example.carcare.model.Driver
import com.example.carcare.model.DriverStatus
import kotlinx.coroutines.launch
import com.example.carcare.data.network.toUserMessage

class DriverViewModel : ViewModel() {

    private val repository = DriverRepository()

    private val _drivers = mutableStateListOf<Driver>()
    val drivers: List<Driver> get() = _drivers

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadDrivers()
    }

    fun loadDrivers() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = repository.getAll()
                _drivers.clear()
                _drivers.addAll(result)
            } catch (e: Exception) {
                errorMessage = "Error al cargar conductores: ${e.message}"
                Log.e("DriverVM", "loadDrivers", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun addDriver(driver: Driver) {
        viewModelScope.launch {
            try {
                val created = repository.create(driver)
                _drivers.add(created)
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
                Log.e("DriverVM", "addDriver: $errorMessage", e)
            }
        }
    }

    fun updateDriver(updatedDriver: Driver) {
        val index = _drivers.indexOfFirst { it.id == updatedDriver.id }
        val previous = if (index != -1) _drivers[index] else null
        if (index != -1) _drivers[index] = updatedDriver

        viewModelScope.launch {
            try {
                val saved = repository.update(updatedDriver)
                val i = _drivers.indexOfFirst { it.id == saved.id }
                if (i != -1) _drivers[i] = saved
            } catch (e: Exception) {
                if (previous != null) {
                    val i = _drivers.indexOfFirst { it.id == previous.id }
                    if (i != -1) _drivers[i] = previous
                }
                errorMessage = "Error al actualizar conductor: ${e.message}"
                Log.e("DriverVM", "updateDriver", e)
            }
        }
    }

    fun deleteDriver(driverId: String) {
        val index = _drivers.indexOfFirst { it.id == driverId }
        val backup = if (index != -1) _drivers[index] else null
        if (index != -1) _drivers.removeAt(index)

        viewModelScope.launch {
            try {
                repository.delete(driverId)
            } catch (e: Exception) {
                if (backup != null) {
                    _drivers.add(index.coerceIn(0, _drivers.size), backup)
                }
                errorMessage = "Error al eliminar conductor: ${e.message}"
                Log.e("DriverVM", "deleteDriver", e)
            }
        }
    }

    fun updateStatus(driverId: String, newStatus: DriverStatus) {
        val index = _drivers.indexOfFirst { it.id == driverId }
        val previous = if (index != -1) _drivers[index] else null
        if (index != -1) _drivers[index] = _drivers[index].copy(status = newStatus)

        viewModelScope.launch {
            try {
                val saved = repository.changeStatus(driverId, newStatus)
                val i = _drivers.indexOfFirst { it.id == saved.id }
                if (i != -1) _drivers[i] = saved
            } catch (e: Exception) {
                if (previous != null) {
                    val i = _drivers.indexOfFirst { it.id == previous.id }
                    if (i != -1) _drivers[i] = previous
                }
                errorMessage = "Error al cambiar estado: ${e.message}"
                Log.e("DriverVM", "updateStatus", e)
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}