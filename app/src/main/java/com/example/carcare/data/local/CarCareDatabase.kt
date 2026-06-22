package com.example.carcare.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base de datos local (Room) que respalda los listados como caché offline-first.
 * exportSchema=false: es un caché desechable, no se versiona el esquema.
 */
@Database(
    entities = [
        VehiculoEntity::class,
        ConductorEntity::class,
        MantenimientoEntity::class,
        AsignacionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CarCareDatabase : RoomDatabase() {
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun conductorDao(): ConductorDao
    abstract fun mantenimientoDao(): MantenimientoDao
    abstract fun asignacionDao(): AsignacionDao
}
