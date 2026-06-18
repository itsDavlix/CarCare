package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AsignacionRequestDto(
    val vehiculoId: Long,
    val conductorId: Long,
    val fechaSalida: String?,
    val fechaRetornoPlanificada: String?,
    val kilometrajeInicial: Long,
    val observacionesSalida: String? = null
)