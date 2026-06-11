package com.example.carcare.data.network.api

import com.example.carcare.data.network.dto.AsignacionRequestDto
import com.example.carcare.data.network.dto.AsignacionResponseDto
import com.example.carcare.data.network.dto.CompletarAsignacionDto
import retrofit2.Response
import retrofit2.http.*

interface AsignacionApiService {

    @GET("api/asignaciones")
    suspend fun listar(): List<AsignacionResponseDto>

    @GET("api/asignaciones/{id}")
    suspend fun obtener(@Path("id") id: Long): AsignacionResponseDto

    @GET("api/asignaciones/activas")
    suspend fun listarActivas(): List<AsignacionResponseDto>

    @GET("api/asignaciones/vehiculo/{vehiculoId}")
    suspend fun listarPorVehiculo(@Path("vehiculoId") vehiculoId: Long): List<AsignacionResponseDto>

    @GET("api/asignaciones/conductor/{conductorId}")
    suspend fun listarPorConductor(@Path("conductorId") conductorId: Long): List<AsignacionResponseDto>

    @POST("api/asignaciones")
    suspend fun crear(@Body dto: AsignacionRequestDto): AsignacionResponseDto

    @PUT("api/asignaciones/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body dto: AsignacionRequestDto): AsignacionResponseDto

    @PATCH("api/asignaciones/{id}/completar")
    suspend fun completar(@Path("id") id: Long, @Body dto: CompletarAsignacionDto): AsignacionResponseDto

    @DELETE("api/asignaciones/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}