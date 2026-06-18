package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MantenimientoResponseDto(
    val id: Long,
    // Null si el vehículo fue eliminado (FK ON DELETE SET NULL conserva el historial)
    val vehiculoId: Long? = null,
    // Campos denormalizados que manda el backend (evitan otro GET)
    val vehiculoPlaca: String? = null,
    val vehiculoMarca: String? = null,
    val vehiculoModelo: String? = null,
    val tipo: String,
    val fecha: String?,
    val fechaCompletado: String? = null,
    val kilometrajeActual: Long,
    val descripcion: String,
    val responsable: String,
    val fechaProxima: String? = null,
    val kilometrajeProximo: Long? = null,
    val estado: String
)