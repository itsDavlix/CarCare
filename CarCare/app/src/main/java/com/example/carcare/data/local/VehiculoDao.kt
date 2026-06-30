package com.example.carcare.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface VehiculoDao {

    @Query("SELECT * FROM vehiculos_cache")
    suspend fun getAll(): List<VehiculoEntity>

    @Upsert
    suspend fun upsertAll(items: List<VehiculoEntity>)

    @Upsert
    suspend fun upsert(item: VehiculoEntity)

    @Query("DELETE FROM vehiculos_cache WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM vehiculos_cache")
    suspend fun clear()

    /** Reemplaza el caché con la lista fresca de la API (refleja también los borrados del server). */
    @Transaction
    suspend fun replaceAll(items: List<VehiculoEntity>) {
        clear()
        upsertAll(items)
    }
}
