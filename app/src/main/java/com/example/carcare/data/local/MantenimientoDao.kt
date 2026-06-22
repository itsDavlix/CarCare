package com.example.carcare.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface MantenimientoDao {

    @Query("SELECT * FROM mantenimientos_cache")
    suspend fun getAll(): List<MantenimientoEntity>

    @Upsert
    suspend fun upsertAll(items: List<MantenimientoEntity>)

    @Upsert
    suspend fun upsert(item: MantenimientoEntity)

    @Query("DELETE FROM mantenimientos_cache WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM mantenimientos_cache")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(items: List<MantenimientoEntity>) {
        clear()
        upsertAll(items)
    }
}
