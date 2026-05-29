package com.example.carcare.ui.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.carcare.model.FuelType
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus

class VehicleViewModel : ViewModel() {
    private val _vehicles = mutableStateListOf<Vehicle>(
        Vehicle(
            id = "1",
            brand = "Toyota",
            model = "Hilux",
            year = 2022,
            plate = "ABC123",
            fuelType = FuelType.DIESEL,
            mileage = 15000,
            color = "Blanco",
            status = VehicleStatus.AVAILABLE,
            description = "Vehículo en buen estado"
        ),
        Vehicle(
            id = "2",
            brand = "Ford",
            model = "Ranger",
            year = 2021,
            plate = "XYZ789",
            fuelType = FuelType.GASOLINE,
            mileage = 32000,
            color = "Gris",
            status = VehicleStatus.MAINTENANCE,
            description = "Cambio de aceite pendiente"
        )
    )
    val vehicles: List<Vehicle> get() = _vehicles

    var searchQuery by mutableStateOf("")
        private set

    val filteredVehicles by derivedStateOf {
        if (searchQuery.isBlank()) {
            _vehicles
        } else {
            _vehicles.filter {
                it.brand.contains(searchQuery, ignoreCase = true) ||
                it.plate.contains(searchQuery, ignoreCase = true) ||
                it.model.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    fun addVehicle(vehicle: Vehicle) {
        _vehicles.add(vehicle)
    }

    fun updateVehicle(updatedVehicle: Vehicle) {
        val index = _vehicles.indexOfFirst { it.id == updatedVehicle.id }
        if (index != -1) {
            _vehicles[index] = updatedVehicle
        }
    }

    fun deleteVehicle(vehicleId: String) {
        _vehicles.removeAll { it.id == vehicleId }
    }

    fun changeStatus(vehicleId: String, newStatus: VehicleStatus) {
        val index = _vehicles.indexOfFirst { it.id == vehicleId }
        if (index != -1) {
            _vehicles[index] = _vehicles[index].copy(status = newStatus)
        }
    }

    fun updateMileage(vehicleId: String, newMileage: Long) {
        val index = _vehicles.indexOfFirst { it.id == vehicleId }
        if (index != -1) {
            _vehicles[index] = _vehicles[index].copy(mileage = newMileage)
        }
    }
}
