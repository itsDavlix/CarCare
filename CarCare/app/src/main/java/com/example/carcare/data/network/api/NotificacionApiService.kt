package com.example.carcare.data.network.api

import com.example.carcare.data.network.dto.NotificacionResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificacionApiService {

    @GET("api/notificaciones")
    suspend fun listar(): List<NotificacionResponseDto>

    @GET("api/notificaciones/conductor/{id}")
    suspend fun listarConductor(@Path("id") id: Long): List<NotificacionResponseDto>

    @PATCH("api/notificaciones/{id}/leida")
    suspend fun marcarLeida(@Path("id") id: Long): Response<Unit>

    @PATCH("api/notificaciones/leer-todas")
    suspend fun leerTodas(@Query("conductorId") conductorId: Long?): Response<Unit>
}
