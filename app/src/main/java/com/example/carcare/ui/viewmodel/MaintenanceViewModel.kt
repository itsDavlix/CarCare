package com.example.carcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.Maintenance
import com.example.carcare.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MaintenanceViewModel(private val repository: VehicleRepository) : ViewModel() {

    fun getMaintenanceForVehicle(vehicleId: Int): Flow<List<Maintenance>> = 
        repository.getMaintenanceForVehicle(vehicleId)
    
    fun getLatestMaintenance(vehicleId: Int): Flow<Maintenance?> = 
        repository.getLatestMaintenance(vehicleId)
    
    fun insertMaintenance(maintenance: Maintenance) = viewModelScope.launch { 
        repository.insertMaintenance(maintenance) 
    }

    fun updateMaintenance(maintenance: Maintenance) = viewModelScope.launch { 
        repository.updateMaintenance(maintenance) 
    }
    
    fun deleteMaintenance(maintenance: Maintenance) = viewModelScope.launch { 
        repository.deleteMaintenance(maintenance) 
    }
}

class MaintenanceViewModelFactory(private val repository: VehicleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaintenanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MaintenanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
