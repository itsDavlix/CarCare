package com.example.carcare.data.network.api

import com.example.carcare.data.network.dto.MantenimientoRequestDto
import com.example.carcare.data.network.dto.MantenimientoResponseDto
import retrofit2.Response
import retrofit2.http.*

interface MantenimientoApiService {

    @GET("api/mantenimientos")
    suspend fun listar(): List<MantenimientoResponseDto>

    @GET("api/mantenimientos/{id}")
    suspend fun obtener(@Path("id") id: Long): MantenimientoResponseDto

    @GET("api/mantenimientos/vehiculo/{vehiculoId}")
    suspend fun listarPorVehiculo(@Path("vehiculoId") vehiculoId: Long): List<MantenimientoResponseDto>

    @POST("api/mantenimientos")
    suspend fun crear(@Body dto: MantenimientoRequestDto): MantenimientoResponseDto

    @PUT("api/mantenimientos/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body dto: MantenimientoRequestDto): MantenimientoResponseDto

    @DELETE("api/mantenimientos/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>

    @PATCH("api/mantenimientos/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): MantenimientoResponseDto
}