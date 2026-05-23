package com.example.carcare.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.carcare.model.Assignment
import com.example.carcare.model.AssignmentStatus
import java.util.*

class AssignmentViewModel : ViewModel() {
    private val _assignments = mutableStateListOf<Assignment>()
    val assignments: List<Assignment> get() = _assignments

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
