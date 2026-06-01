package com.example.carcare.ui.viewmodel

import com.example.carcare.data.repository.MaintenanceRepository
import com.example.carcare.model.Maintenance
import com.example.carcare.model.MaintenanceStatus
import com.example.carcare.model.Vehicle

class MaintenanceViewModel :
    BaseListViewModel<Maintenance, MaintenanceRepository>(MaintenanceRepository(), "MaintenanceVM") {

    val maintenances: List<Maintenance> get() = items

    /** Filtro que cruza con vehículos (marca/placa) o por descripción. */
    fun getFilteredMaintenances(vehicles: List<Vehicle>): List<Maintenance> {
        if (searchQuery.isBlank()) return items
        return items.filter { m ->
            val vehicle = vehicles.firstOrNull { it.id == m.vehicleId }
            matchesQuery(vehicle?.brand, vehicle?.plate, m.description)
        }
    }

    /** Historial local (ya cargado) de un vehículo. Síncrono. */
    fun getHistoryForVehicle(vehicleId: String): List<Maintenance> =
        items.filter { it.vehicleId == vehicleId }

    fun loadMaintenances() = load()

    fun addMaintenance(maintenance: Maintenance) = create(maintenance)

    fun updateMaintenance(updatedMaintenance: Maintenance) =
        optimisticReplace(updatedMaintenance) { repository.update(updatedMaintenance) }

    fun deleteMaintenance(maintenanceId: String) = optimisticDelete(maintenanceId)

    fun updateStatus(maintenanceId: String, newStatus: MaintenanceStatus) {
        val current = items.firstOrNull { it.id == maintenanceId } ?: return
        optimisticReplace(current.copy(status = newStatus)) {
            repository.changeStatus(maintenanceId, newStatus)
        }
    }
}