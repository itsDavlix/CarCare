package com.example.carcare.repository

import com.example.carcare.data.*
import kotlinx.coroutines.flow.Flow

class VehicleRepository(
    private val vehicleDao: VehicleDao,
    private val maintenanceDao: MaintenanceDao,
    private val expenseDao: ExpenseDao
) {
    // Vehicle operations
    fun getAllVehicles(): Flow<List<Vehicle>> = vehicleDao.getAllVehicles()
    suspend fun getVehicleById(id: Int): Vehicle? = vehicleDao.getVehicleById(id)
    suspend fun insertVehicle(vehicle: Vehicle) = vehicleDao.insertVehicle(vehicle)
    suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.updateVehicle(vehicle)
    suspend fun deleteVehicle(vehicle: Vehicle) = vehicleDao.deleteVehicle(vehicle)

    // Maintenance operations
    fun getMaintenanceForVehicle(vehicleId: Int): Flow<List<Maintenance>> = 
        maintenanceDao.getMaintenanceForVehicle(vehicleId)
    
    fun getLatestMaintenance(vehicleId: Int): Flow<Maintenance?> = 
        maintenanceDao.getLatestMaintenance(vehicleId)
    
    suspend fun insertMaintenance(maintenance: Maintenance) = 
        maintenanceDao.insertMaintenance(maintenance)
    
    suspend fun updateMaintenance(maintenance: Maintenance) = 
        maintenanceDao.updateMaintenance(maintenance)
    
    suspend fun deleteMaintenance(maintenance: Maintenance) = 
        maintenanceDao.deleteMaintenance(maintenance)

    // Expense operations
    fun getExpensesForVehicle(vehicleId: Int): Flow<List<Expense>> = 
        expenseDao.getExpensesForVehicle(vehicleId)
    
    fun getTotalExpenses(vehicleId: Int): Flow<Double?> = 
        expenseDao.getTotalExpenses(vehicleId)
    
    suspend fun insertExpense(expense: Expense) = 
        expenseDao.insertExpense(expense)
    
    suspend fun updateExpense(expense: Expense) = 
        expenseDao.updateExpense(expense)
    
    suspend fun deleteExpense(expense: Expense) = 
        expenseDao.deleteExpense(expense)
}
