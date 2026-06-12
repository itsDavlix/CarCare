package com.example.carcare.data.network.dto

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
    val estado: String
)