package com.example.carcare.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface ConductorDao {

    @Query("SELECT * FROM conductores_cache")
    suspend fun getAll(): List<ConductorEntity>

    @Upsert
    suspend fun upsertAll(items: List<ConductorEntity>)

    @Upsert
    suspend fun upsert(item: ConductorEntity)

    @Query("DELETE FROM conductores_cache WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM conductores_cache")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(items: List<ConductorEntity>) {
        clear()
        upsertAll(items)
    }
}
