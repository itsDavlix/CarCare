package com.example.carcare.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.repository.AssignmentRepository
import com.example.carcare.model.Assignment
import com.example.carcare.model.Driver
import com.example.carcare.model.Vehicle
import com.example.carcare.model.VehicleStatus
import kotlinx.coroutines.launch
import java.util.Date

class AssignmentViewModel : ViewModel() {

    private val repository = AssignmentRepository()

    private val _assignments = mutableStateListOf<Assignment>()
    val assignments: List<Assignment> get() = _assignments

    var searchQuery by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadAssignments()
    }

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

    fun loadAssignments() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = repository.getAll()
                _assignments.clear()
                _assignments.addAll(result)
            } catch (e: Exception) {
                errorMessage = "Error al cargar asignaciones: ${e.message}"
                Log.e("AssignmentVM", "loadAssignments", e)
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Crea la asignacion. El backend pone el vehiculo en IN_USE solo;
     * onSuccess deberia refrescar la lista de vehiculos para reflejarlo.
     */
    fun addAssignment(assignment: Assignment, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val created = repository.create(assignment)
                _assignments.add(created)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al crear asignación: ${e.message}"
                Log.e("AssignmentVM", "addAssignment", e)
            }
        }
    }

    /**
     * Completa (retorno). El backend actualiza km + estado del vehiculo;
     * onSuccess deberia refrescar la lista de vehiculos.
     */
    fun completeAssignment(
        assignmentId: String,
        returnDate: Date,
        finalMileage: Long,
        observations: String,
        nextStatus: VehicleStatus,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val updated = repository.complete(assignmentId, returnDate, finalMileage, observations, nextStatus)
                val i = _assignments.indexOfFirst { it.id == updated.id }
                if (i != -1) _assignments[i] = updated
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al completar asignación: ${e.message}"
                Log.e("AssignmentVM", "completeAssignment", e)
            }
        }
    }

    /**
     * Borra. Si estaba activa, el backend libera el vehiculo;
     * onSuccess deberia refrescar la lista de vehiculos.
     */
    fun deleteAssignment(assignmentId: String, onSuccess: () -> Unit = {}) {
        val index = _assignments.indexOfFirst { it.id == assignmentId }
        val backup = if (index != -1) _assignments[index] else null
        if (index != -1) _assignments.removeAt(index)

        viewModelScope.launch {
            try {
                repository.delete(assignmentId)
                onSuccess()
            } catch (e: Exception) {
                if (backup != null) {
                    _assignments.add(index.coerceIn(0, _assignments.size), backup)
                }
                errorMessage = "Error al eliminar asignación: ${e.message}"
                Log.e("AssignmentVM", "deleteAssignment", e)
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}