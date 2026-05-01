package com.example.carcare.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data class VehicleDetail(val vehicleId: Int) : Screen

    @Serializable
    data class AddEditVehicle(val vehicleId: Int? = null) : Screen

    @Serializable
    data class MaintenanceList(val vehicleId: Int) : Screen

    @Serializable
    data class AddMaintenance(val vehicleId: Int) : Screen

    @Serializable
    data class ExpenseList(val vehicleId: Int) : Screen

    @Serializable
    data class AddExpense(val vehicleId: Int) : Screen
}
