package com.example.carcare.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getMaintenanceForVehicle(vehicleId: Int): Flow<List<Maintenance>>

    @Query("SELECT * FROM maintenance WHERE vehicleId = :vehicleId ORDER BY date DESC LIMIT 1")
    fun getLatestMaintenance(vehicleId: Int): Flow<Maintenance?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(maintenance: Maintenance)

    @Update
    suspend fun updateMaintenance(maintenance: Maintenance)

    @Delete
    suspend fun deleteMaintenance(maintenance: Maintenance)
}
