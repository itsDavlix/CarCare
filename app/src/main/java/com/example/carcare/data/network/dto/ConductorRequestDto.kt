package com.example.carcare.data.network.dto

data class ConductorRequestDto(
    val nombre: String,
    val apellido: String,
    val cedula: String,
    val edad: Int,
    val telefono: String,
    val numeroLicencia: String,
    val fechaVencimientoLicencia: String?,
    val fotoPerfilUri: String? = null,
    val fotoLicenciaUri: String? = null,
    val estado: String? = null
)