package com.example.carcare.data.repository

import com.example.carcare.model.Identifiable

/**
 * Contrato CRUD común a los cuatro repositorios.
 * Solo declara lo universal (listar/crear/borrar); cada repo concreto añade
 * lo suyo (update, cambiar estado, completar, etc.). Permite que
 * BaseListViewModel dependa de la abstracción y no del repo concreto.
 */
interface CrudRepository<T : Identifiable> {
    suspend fun getAll(): List<T>
    suspend fun create(item: T): T
    suspend fun delete(id: String)
}