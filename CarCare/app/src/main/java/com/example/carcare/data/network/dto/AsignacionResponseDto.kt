package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AsignacionResponseDto(
    val id: Long,
    // Null si el vehículo fue eliminado (FK ON DELETE SET NULL conserva el historial)
    val vehiculoId: Long? = null,
    val vehiculoPlaca: String? = null,
    val vehiculoMarca: String? = null,
    val vehiculoModelo: String? = null,
    val conductorId: Long,
    val conductorNombreCompleto: String? = null,
    val conductorTelefono: String? = null,
    val fechaSalida: String?,
    val fechaRetornoPlanificada: String?,
    val kilometrajeInicial: Long,
    val fechaRetorno: String? = null,
    val kilometrajeFinal: Long? = null,
    val observacionesSalida: String? = null,
    val observacionesRetorno: String? = null,
    val estado: String,
    // Check-out / check-in
    val nivelCombustibleInicial: String? = null,
    val condicionOptimaInicial: Boolean? = null,
    val fechaAceptacion: String? = null,
    val nivelCombustibleFinal: String? = null,
    val condicionOptimaFinal: Boolean? = null,
    val motivoRechazo: String? = null,
    val tieneFotoInicial: Boolean = false,
    val tieneFotoFinal: Boolean = false,
    // Derivados: atraso de devolución
    val vencida: Boolean = false,
    val diasAtraso: Long = 0
)