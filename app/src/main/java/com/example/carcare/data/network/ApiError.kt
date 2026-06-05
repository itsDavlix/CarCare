package com.example.carcare.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.HttpException
import java.io.IOException

/**
 * Cuerpo de error del backend (coincide con ErrorResponseDTO del servidor).
 * Moshi ignora las claves que no esten aca (timestamp, path), y todo es
 * nullable: si el JSON viene incompleto, no rompe.
 */
data class ApiErrorResponse(
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null
)

// Moshi propio para el cuerpo de error (reflexion, como el resto del proyecto).
private val errorMoshi: Moshi by lazy {
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
}
private val errorAdapter by lazy { errorMoshi.adapter(ApiErrorResponse::class.java) }

/**
 * Extrae un mensaje legible de una excepcion de red.
 * Para errores HTTP del backend, parsea el JSON con Moshi y toma "message".
 * Para fallos de conexion, devuelve un mensaje generico.
 */
fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> {
        val body = runCatching { response()?.errorBody()?.string() }.getOrNull()
        val parsedMessage = body
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { errorAdapter.fromJson(it) }.getOrNull() }
            ?.message
            ?.takeIf { it.isNotBlank() }
        parsedMessage ?: "Error ${code()} del servidor"
    }
    is IOException -> "Sin conexión. Revisá tu internet e intentá de nuevo."
    else -> message ?: "Error desconocido"
}