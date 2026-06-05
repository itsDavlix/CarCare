package com.example.carcare.data.network.dto

/**
 * Respuesta del backend para un vehiculo. Mapea 1:1 con VehiculoResponseDTO del servidor.
 * Las fechas vienen como String ISO "YYYY-MM-DD". La conversion a Date se hace en el mapper.
 */
data class VehiculoResponseDto(
    val id: Long,
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
    val estado: String,
    val descripcion: String? = null
)