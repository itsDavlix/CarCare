package com.example.carcare.ui.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.example.carcare.data.repository.DriverRepository
import com.example.carcare.model.Driver
import com.example.carcare.model.DriverStatus

class DriverViewModel :
    BaseListViewModel<Driver, DriverRepository>(DriverRepository(), "DriverVM") {

    val drivers: List<Driver> get() = items

    val filteredDrivers by derivedStateOf {
        items.filter { matchesQuery(it.fullName, it.idCardNumber) }
    }

    fun loadDrivers() = load()

    fun addDriver(driver: Driver) = create(driver)

    fun updateDriver(updatedDriver: Driver) =
        optimisticReplace(updatedDriver) { repository.update(updatedDriver) }

    fun deleteDriver(driverId: String) = optimisticDelete(driverId)

    fun updateStatus(driverId: String, newStatus: DriverStatus) {
        val current = items.firstOrNull { it.id == driverId } ?: return
        optimisticReplace(current.copy(status = newStatus)) {
            repository.changeStatus(driverId, newStatus)
        }
    }
}