package com.example.carcare.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.carcare.model.Assignment
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.model.Driver
import com.example.carcare.model.Vehicle
import java.util.*

class AssignmentViewModel : ViewModel() {
    private val _assignments = mutableStateListOf<Assignment>()
    val assignments: List<Assignment> get() = _assignments

    var searchQuery by mutableStateOf("")
        private set

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    fun getFilteredAssignments(vehicles: List<Vehicle>, drivers: List<Driver>): List<Assignment> {
        if (searchQuery.isBlank()) return _assignments
        return _assignments.filter { a ->
            val vehicle = vehicles.find { it.id == a.vehicleId }
            val driver = drivers.find { it.id == a.driverId }
            vehicle?.plate?.contains(searchQuery, ignoreCase = true) == true ||
            driver?.fullName?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    fun addAssignment(assignment: Assignment) {
        _assignments.add(assignment)
    }

    fun completeAssignment(assignmentId: String, returnDate: Date, finalMileage: Long, observations: String) {
        val index = _assignments.indexOfFirst { it.id == assignmentId }
        if (index != -1) {
            _assignments[index] = _assignments[index].copy(
                returnDate = returnDate,
                finalMileage = finalMileage,
                returnObservations = observations,
                status = AssignmentStatus.COMPLETED
            )
        }
    }

    fun deleteAssignment(assignmentId: String) {
        _assignments.removeAll { it.id == assignmentId }
    }
}
