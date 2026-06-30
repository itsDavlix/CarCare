package com.example.carcare.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface AsignacionDao {

    @Query("SELECT * FROM asignaciones_cache")
    suspend fun getAll(): List<AsignacionEntity>

    @Upsert
    suspend fun upsertAll(items: List<AsignacionEntity>)

    @Upsert
    suspend fun upsert(item: AsignacionEntity)

    @Query("DELETE FROM asignaciones_cache WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM asignaciones_cache")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(items: List<AsignacionEntity>) {
        clear()
        upsertAll(items)
    }
}
