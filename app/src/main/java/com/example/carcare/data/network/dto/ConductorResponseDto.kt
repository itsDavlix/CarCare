package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConductorResponseDto(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val nombreCompleto: String,
    val cedula: String,
    val edad: Int,
    val telefono: String,
    val numeroLicencia: String,
    val fechaVencimientoLicencia: String?,
    val fotoPerfilUri: String? = null,
    val fotoLicenciaUri: String? = null,
    val estado: String
)