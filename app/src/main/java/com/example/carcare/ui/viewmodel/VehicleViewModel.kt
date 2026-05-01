package com.example.carcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.Vehicle
import com.example.carcare.repository.VehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VehicleViewModel(private val repository: VehicleRepository) : ViewModel() {

    val allVehicles: StateFlow<List<Vehicle>> = repository.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertVehicle(vehicle: Vehicle) = viewModelScope.launch { repository.insertVehicle(vehicle) }
    fun updateVehicle(vehicle: Vehicle) = viewModelScope.launch { repository.updateVehicle(vehicle) }
    fun deleteVehicle(vehicle: Vehicle) = viewModelScope.launch { repository.deleteVehicle(vehicle) }
    suspend fun getVehicleById(id: Int): Vehicle? = repository.getVehicleById(id)
}

class VehicleViewModelFactory(private val repository: VehicleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VehicleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
