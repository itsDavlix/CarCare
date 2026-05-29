package com.example.carcare.ui.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.carcare.model.Maintenance
import com.example.carcare.model.MaintenanceStatus
import com.example.carcare.model.MaintenanceType
import com.example.carcare.model.Vehicle
import java.util.*

class MaintenanceViewModel : ViewModel() {
    private val _maintenances = mutableStateListOf<Maintenance>()
    val maintenances: List<Maintenance> get() = _maintenances

    init {
        // Sample data
        _maintenances.add(
            Maintenance(
                vehicleId = "1", // Toyota Hilux from VehicleViewModel
                type = MaintenanceType.OIL_CHANGE,
                date = Date(),
                currentMileage = 15000,
                description = "Cambio de aceite y filtro sintético",
                responsible = "Taller Central",
                nextDate = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }.time,
                nextMileage = 20000,
                status = MaintenanceStatus.COMPLETED
            )
        )
    }

    var searchQuery by mutableStateOf("")
        private set

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    fun getFilteredMaintenances(vehicles: List<Vehicle>): List<Maintenance> {
        if (searchQuery.isBlank()) return _maintenances
        return _maintenances.filter { m ->
            val vehicle = vehicles.find { it.id == m.vehicleId }
            vehicle?.brand?.contains(searchQuery, ignoreCase = true) == true ||
            vehicle?.plate?.contains(searchQuery, ignoreCase = true) == true ||
            m.description.contains(searchQuery, ignoreCase = true)
        }
    }

    fun addMaintenance(maintenance: Maintenance) {
        _maintenances.add(maintenance)
    }

    fun updateMaintenance(updatedMaintenance: Maintenance) {
        val index = _maintenances.indexOfFirst { it.id == updatedMaintenance.id }
        if (index != -1) {
            _maintenances[index] = updatedMaintenance
        }
    }

    fun deleteMaintenance(maintenanceId: String) {
        _maintenances.removeAll { it.id == maintenanceId }
    }

    fun getHistoryForVehicle(vehicleId: String): List<Maintenance> {
        return _maintenances.filter { it.vehicleId == vehicleId }
    }

    fun updateStatus(maintenanceId: String, newStatus: MaintenanceStatus) {
        val index = _maintenances.indexOfFirst { it.id == maintenanceId }
        if (index != -1) {
            _maintenances[index] = _maintenances[index].copy(status = newStatus)
        }
    }
}
