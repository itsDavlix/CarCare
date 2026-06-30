package com.example.carcare.model

/**
 * Contrato para entidades de dominio con identidad estable.
 * Permite que BaseListViewModel y CrudRepository operen de forma genérica
 * (buscar, reemplazar o borrar por id) sin conocer el tipo concreto.
 */
interface Identifiable {
    val id: String
}