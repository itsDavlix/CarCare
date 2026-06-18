package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MantenimientoRequestDto(
    val vehiculoId: Long,
    val tipo: String,
    val fecha: String?,
    val fechaCompletado: String? = null,
    val kilometrajeActual: Long,
    val descripcion: String,
    val responsable: String,
    val fechaProxima: String? = null,
    val kilometrajeProximo: Long? = null,
    val estado: String? = null,
    // Reporte del conductor: el backend pone el vehículo EN REVISIÓN + actualiza km (atómico).
    val ponerVehiculoEnRevision: Boolean? = null
)