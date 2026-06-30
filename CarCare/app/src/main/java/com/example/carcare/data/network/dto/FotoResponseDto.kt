package com.example.carcare.data.network.dto

import com.squareup.moshi.JsonClass

/** Respuesta de GET /api/asignaciones/{id}/foto-inicial|foto-final: la imagen en base64 (o null). */
@JsonClass(generateAdapter = true)
data class FotoResponseDto(
    val foto: String? = null
)
