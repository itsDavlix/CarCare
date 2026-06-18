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

    /** Historial local (ya cargado) de un vehículo. Síncrono. Ordenado por fecha descendente. */
    fun getHistoryForVehicle(vehicleId: String): List<Maintenance> =
        items.filter { it.vehicleId == vehicleId }.sortedByDescending { it.date }

    fun loadMaintenances() = load()

    fun addMaintenance(maintenance: Maintenance) = create(maintenance)

    /**
     * Reporte del conductor: una sola operación atómica en el backend (crea el
     * mantenimiento + deja el vehículo EN REVISIÓN + actualiza su km).
     */
    fun reportar(maintenance: Maintenance, onSuccess: () -> Unit = {}) =
        createWith(onSuccess) { repository.reportar(maintenance) }

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