package com.example.carcare.data.network.api

import com.example.carcare.data.network.dto.ConductorRequestDto
import com.example.carcare.data.network.dto.ConductorResponseDto
import retrofit2.Response
import retrofit2.http.*

interface ConductorApiService {

    @GET("api/conductores")
    suspend fun listar(): List<ConductorResponseDto>

    @GET("api/conductores/{id}")
    suspend fun obtener(@Path("id") id: Long): ConductorResponseDto

    @POST("api/conductores")
    suspend fun crear(@Body dto: ConductorRequestDto): ConductorResponseDto

    @PUT("api/conductores/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body dto: ConductorRequestDto): ConductorResponseDto

    @DELETE("api/conductores/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>

    @PATCH("api/conductores/{id}/estado")
    suspend fun cambiarEstado(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): ConductorResponseDto
}