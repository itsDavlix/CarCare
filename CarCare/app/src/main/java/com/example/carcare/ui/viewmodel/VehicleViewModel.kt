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
import java.util.*

class VehicleViewModel : ViewModel() {
    private val _vehicles = mutableStateListOf<Vehicle>(
        Vehicle(
            brand = "Toyota",
            model = "Hilux",
            year = 2025,
            plate = "M460800",
            fuelType = FuelType.DIESEL,
            mileage = 0,
            color = "Plata",
            chassisNumber = "TH213",
            engineNumber = "2.4 2GD-FTV",
            vehiclePhotoUri = "https://images.prd.kavak.io/eyJidWNrZXQiOiJrYXZhay1sdW1vcy1wcm9kLWltYWdlcyIsImtleSI6ImltYWdlcy9hZHMvMzc5NDU3L29wdGltaXplZC9pbWctMjAyNDEwMTUtMTEwNDMyLnBuZyIsImVkaXRzIjp7InJlc2l6ZSI6eyJ3aWR0aCI6NjQwLCJoZWlnaHQiOjQ4MH19fQ==",
            insuranceExpiryDate = Calendar.getInstance().apply { set(2027, Calendar.JANUARY, 1, 0, 0, 0) }.time,
            status = VehicleStatus.AVAILABLE
        ),
        Vehicle(
            brand = "Toyota",
            model = "Yaris E",
            year = 2024,
            plate = "M390789",
            fuelType = FuelType.GASOLINE,
            mileage = 20000,
            color = "Blanco",
            chassisNumber = "TY204",
            engineNumber = "1.5 2NR-FE",
            insuranceExpiryDate = Calendar.getInstance().apply { set(2027, Calendar.JANUARY, 1, 0, 0, 0) }.time,
            status = VehicleStatus.AVAILABLE
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
