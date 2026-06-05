package com.example.carcare.data.network.dto

/**
 * Cuerpo de POST/PUT. (lo asigna el backend). Mapea a VehiculoRequestDTO del servidor.
 */
data class VehiculoRequestDto(
    val marca: String,
    val modelo: String,
    val anio: Int,
    val placa: String,
    val combustible: String,
    val kilometraje: Long,
    val color: String? = null,
    val numeroChasis: String? = null,
    val numeroMotor: String? = null,
    val polizaSeguro: String? = null,
    val fechaVencimientoSeguro: String? = null,
    val fechaVencimientoCirculacion: String? = null,
    val fotoUri: String? = null,
    val fotoCirculacionUri: String? = null,
    val fotoSeguroUri: String? = null,
    val estado: String? = null,
    val descripcion: String? = null
)