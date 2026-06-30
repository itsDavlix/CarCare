package com.example.carcare.ui.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.example.carcare.data.repository.VehicleRepository
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus

@dagger.hilt.android.lifecycle.HiltViewModel
class VehicleViewModel @javax.inject.Inject constructor(repository: VehicleRepository) :
    BaseListViewModel<Vehicle, VehicleRepository>(repository, "VehicleVM") {

    val vehicles: List<Vehicle> get() = items

    val filteredVehicles by derivedStateOf {
        items.filter { matchesQuery(it.brand, it.plate, it.model) }
    }

    fun loadVehicles() = load()

    fun addVehicle(vehicle: Vehicle) = create(vehicle)

    fun updateVehicle(updatedVehicle: Vehicle) =
        optimisticReplace(updatedVehicle) { repository.update(updatedVehicle) }

    fun deleteVehicle(vehicleId: String) = optimisticDelete(vehicleId)

    fun changeStatus(vehicleId: String, newStatus: VehicleStatus) {
        val current = items.firstOrNull { it.id == vehicleId } ?: return
        optimisticReplace(current.copy(status = newStatus)) {
            repository.changeStatus(vehicleId, newStatus)
        }
    }

    fun updateMileage(vehicleId: String, newMileage: Long) {
        val current = items.firstOrNull { it.id == vehicleId } ?: return
        optimisticReplace(current.copy(mileage = newMileage)) {
            repository.updateMileage(vehicleId, newMileage)
        }
    }
}