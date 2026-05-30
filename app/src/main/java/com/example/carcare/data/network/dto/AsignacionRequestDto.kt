package com.example.carcare.data.network.dto

data class AsignacionRequestDto(
    val vehiculoId: Long,
    val conductorId: Long,
    val fechaSalida: String?,
    val fechaRetornoPlanificada: String?,
    val kilometrajeInicial: Long,
    val observacionesSalida: String? = null
)