package com.example.carcare.ui.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.carcare.model.Driver
import com.example.carcare.model.DriverStatus
import java.util.*

class DriverViewModel : ViewModel() {
    private val _drivers = mutableStateListOf<Driver>(
        Driver(
            firstName = "David Alejandro",
            lastName = "Espinoza Largaespada",
            idCardNumber = "000-250805-0000N",
            age = 20,
            phone = "88804533",
            licenseNumber = "N/A",
            licenseExpiryDate = Calendar.getInstance().apply { set(2029, Calendar.AUGUST, 25, 0, 0, 0) }.time,
            status = DriverStatus.ACTIVE
        )
    )
    val drivers: List<Driver> get() = _drivers

    var searchQuery by mutableStateOf("")
        private set

    val filteredDrivers by derivedStateOf {
        if (searchQuery.isBlank()) {
            _drivers
        } else {
            _drivers.filter {
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.idCardNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    fun addDriver(driver: Driver) {
        _drivers.add(driver)
    }

    fun updateDriver(updatedDriver: Driver) {
        val index = _drivers.indexOfFirst { it.id == updatedDriver.id }
        if (index != -1) {
            _drivers[index] = updatedDriver
        }
    }

    fun deleteDriver(driverId: String) {
        _drivers.removeAll { it.id == driverId }
    }

    fun updateStatus(driverId: String, newStatus: DriverStatus) {
        val index = _drivers.indexOfFirst { it.id == driverId }
        if (index != -1) {
            _drivers[index] = _drivers[index].copy(status = newStatus)
        }
    }
}
