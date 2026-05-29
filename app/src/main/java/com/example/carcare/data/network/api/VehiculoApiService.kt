package com.example.carcare.data.network.api

import com.example.carcare.data.network.dto.VehiculoRequestDto
import com.example.carcare.data.network.dto.VehiculoResponseDto
import retrofit2.Response
import retrofit2.http.*

interface VehiculoApiService {

    @GET("api/vehiculos")
    suspend fun listar(): List<VehiculoResponseDto>

    @GET("api/vehiculos/{id}")
    suspend fun obtener(@Path("id") id: Long): VehiculoResponseDto

    @POST("api/vehiculos")
    suspend fun crear(@Body dto: VehiculoRequestDto): VehiculoResponseDto

    @PUT("api/vehiculos/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body dto: VehiculoRequestDto): VehiculoResponseDto

    @DELETE("api/vehiculos/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>

    @PATCH("api/vehiculos/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): VehiculoResponseDto

    @PATCH("api/vehiculos/{id}/kilometraje")
    suspend fun actualizarKilometraje(
        @Path("id") id: Long,
        @Body body: Map<String, Long>
    ): VehiculoResponseDto
}