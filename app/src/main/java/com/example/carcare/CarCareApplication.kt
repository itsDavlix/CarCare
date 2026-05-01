package com.example.carcare

import android.app.Application
import com.example.carcare.data.AppDatabase
import com.example.carcare.repository.VehicleRepository

class CarCareApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: VehicleRepository by lazy { 
        VehicleRepository(
            database.vehicleDao(),
            database.maintenanceDao(),
            database.expenseDao()
        ) 
    }
}
