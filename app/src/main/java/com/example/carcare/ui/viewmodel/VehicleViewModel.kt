package com.example.carcare.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.repository.VehicleRepository
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus
import kotlinx.coroutines.launch

class VehicleViewModel : ViewModel() {

    private val repository = VehicleRepository()

    private val _vehicles = mutableStateListOf<Vehicle>()
    val vehicles: List<Vehicle> get() = _vehicles

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadVehicles()
    }

    /** Carga completa desde la API. Solo en el arranque o refresco manual. */
    fun loadVehicles() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = repository.getAll()
                _vehicles.clear()
                _vehicles.addAll(result)
            } catch (e: Exception) {
                errorMessage = "Error al cargar vehículos: ${e.message}"
                Log.e("VehicleVM", "loadVehicles", e)
            } finally {
                isLoading = false
            }
        }
    }

    /** Crea en el backend y agrega el vehiculo devuelto (con id real). Sin recargar todo. */
    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                val created = repository.create(vehicle)
                _vehicles.add(created)
            } catch (e: Exception) {
                errorMessage = "Error al crear vehículo: ${e.message}"
                Log.e("VehicleVM", "addVehicle", e)
            }
        }
    }

    /** Optimista: refleja el cambio al instante, revierte si la red falla. */
    fun updateVehicle(updatedVehicle: Vehicle) {
        val index = _vehicles.indexOfFirst { it.id == updatedVehicle.id }
        val previous = if (index != -1) _vehicles[index] else null
        if (index != -1) _vehicles[index] = updatedVehicle

        viewModelScope.launch {
            try {
                val saved = repository.update(updatedVehicle)
                val i = _vehicles.indexOfFirst { it.id == saved.id }
                if (i != -1) _vehicles[i] = saved
            } catch (e: Exception) {
                if (previous != null) {
                    val i = _vehicles.indexOfFirst { it.id == previous.id }
                    if (i != -1) _vehicles[i] = previous
                }
                errorMessage = "Error al actualizar vehículo: ${e.message}"
                Log.e("VehicleVM", "updateVehicle", e)
            }
        }
    }

    /** Optimista: lo saca de la lista al instante, lo reinserta si falla. */
    fun deleteVehicle(vehicleId: String) {
        val index = _vehicles.indexOfFirst { it.id == vehicleId }
        val backup = if (index != -1) _vehicles[index] else null
        if (index != -1) _vehicles.removeAt(index)

        viewModelScope.launch {
            try {
                repository.delete(vehicleId)
            } catch (e: Exception) {
                if (backup != null) {
                    _vehicles.add(index.coerceIn(0, _vehicles.size), backup)
                }
                errorMessage = "Error al eliminar vehículo: ${e.message}"
                Log.e("VehicleVM", "deleteVehicle", e)
            }
        }
    }

    /** Optimista. */
    fun changeStatus(vehicleId: String, newStatus: VehicleStatus) {
        val index = _vehicles.indexOfFirst { it.id == vehicleId }
        val previous = if (index != -1) _vehicles[index] else null
        if (index != -1) _vehicles[index] = _vehicles[index].copy(status = newStatus)

        viewModelScope.launch {
            try {
                val saved = repository.changeStatus(vehicleId, newStatus)
                val i = _vehicles.indexOfFirst { it.id == saved.id }
                if (i != -1) _vehicles[i] = saved
            } catch (e: Exception) {
                if (previous != null) {
                    val i = _vehicles.indexOfFirst { it.id == previous.id }
                    if (i != -1) _vehicles[i] = previous
                }
                errorMessage = "Error al cambiar estado: ${e.message}"
                Log.e("VehicleVM", "changeStatus", e)
            }
        }
    }

    /** Optimista. */
    fun updateMileage(vehicleId: String, newMileage: Long) {
        val index = _vehicles.indexOfFirst { it.id == vehicleId }
        val previous = if (index != -1) _vehicles[index] else null
        if (index != -1) _vehicles[index] = _vehicles[index].copy(mileage = newMileage)

        viewModelScope.launch {
            try {
                val saved = repository.updateMileage(vehicleId, newMileage)
                val i = _vehicles.indexOfFirst { it.id == saved.id }
                if (i != -1) _vehicles[i] = saved
            } catch (e: Exception) {
                if (previous != null) {
                    val i = _vehicles.indexOfFirst { it.id == previous.id }
                    if (i != -1) _vehicles[i] = previous
                }
                errorMessage = "Error al actualizar kilometraje: ${e.message}"
                Log.e("VehicleVM", "updateMileage", e)
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}