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

    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                repository.create(vehicle)
                loadVehicles()
            } catch (e: Exception) {
                errorMessage = "Error al crear vehículo: ${e.message}"
                Log.e("VehicleVM", "addVehicle", e)
            }
        }
    }

    fun updateVehicle(updatedVehicle: Vehicle) {
        viewModelScope.launch {
            try {
                repository.update(updatedVehicle)
                loadVehicles()
            } catch (e: Exception) {
                errorMessage = "Error al actualizar vehículo: ${e.message}"
                Log.e("VehicleVM", "updateVehicle", e)
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                repository.delete(vehicleId)
                loadVehicles()
            } catch (e: Exception) {
                errorMessage = "Error al eliminar vehículo: ${e.message}"
                Log.e("VehicleVM", "deleteVehicle", e)
            }
        }
    }

    fun changeStatus(vehicleId: String, newStatus: VehicleStatus) {
        viewModelScope.launch {
            try {
                repository.changeStatus(vehicleId, newStatus)
                loadVehicles()
            } catch (e: Exception) {
                errorMessage = "Error al cambiar estado: ${e.message}"
                Log.e("VehicleVM", "changeStatus", e)
            }
        }
    }

    fun updateMileage(vehicleId: String, newMileage: Long) {
        viewModelScope.launch {
            try {
                repository.updateMileage(vehicleId, newMileage)
                loadVehicles()
            } catch (e: Exception) {
                errorMessage = "Error al actualizar kilometraje: ${e.message}"
                Log.e("VehicleVM", "updateMileage", e)
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}