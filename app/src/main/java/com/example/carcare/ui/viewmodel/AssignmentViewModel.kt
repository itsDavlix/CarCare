package com.example.carcare.ui.viewmodel

import com.example.carcare.data.repository.AssignmentRepository
import com.example.carcare.model.Assignment
import com.example.carcare.model.Driver
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus
import java.util.Date

@dagger.hilt.android.lifecycle.HiltViewModel
class AssignmentViewModel @javax.inject.Inject constructor(repository: AssignmentRepository) :
    BaseListViewModel<Assignment, AssignmentRepository>(repository, "AssignmentVM") {

    val assignments: List<Assignment> get() = items

    fun getFilteredAssignments(vehicles: List<Vehicle>, drivers: List<Driver>): List<Assignment> {
        if (searchQuery.isBlank()) return items
        return items.filter { a ->
            val vehicle = vehicles.firstOrNull { it.id == a.vehicleId }
            val driver = drivers.firstOrNull { it.id == a.driverId }
            matchesQuery(vehicle?.plate, driver?.fullName)
        }
    }

    fun loadAssignments() = load()

    /** El backend pone el vehículo en IN_USE; onSuccess debe refrescar la lista de vehículos. */
    fun addAssignment(assignment: Assignment, onSuccess: () -> Unit = {}) =
        create(assignment, onSuccess)

    fun updateAssignment(assignment: Assignment, onSuccess: () -> Unit = {}) =
        replaceFromNetwork(onSuccess) { repository.update(assignment) }

    /** El backend actualiza km + estado del vehículo; onSuccess debe refrescar vehículos. */
    fun completeAssignment(
        assignmentId: String,
        returnDate: Date,
        finalMileage: Long,
        observations: String,
        nextStatus: VehicleStatus,
        onSuccess: () -> Unit = {}
    ) = replaceFromNetwork(onSuccess) {
        repository.complete(assignmentId, returnDate, finalMileage, observations, nextStatus)
    }

    /** Si estaba activa, el backend libera el vehículo; onSuccess debe refrescar vehículos. */
    fun deleteAssignment(assignmentId: String, onSuccess: () -> Unit = {}) =
        optimisticDelete(assignmentId, onSuccess)
}