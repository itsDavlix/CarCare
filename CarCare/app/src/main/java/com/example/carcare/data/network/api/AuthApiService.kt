package com.example.carcare.data.network.api

import com.example.carcare.data.network.dto.CambioPasswordDto
import com.example.carcare.data.network.dto.LoginRequestDto
import com.example.carcare.data.network.dto.LoginResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body dto: LoginRequestDto): LoginResponseDto

    @POST("api/auth/cambiar-password")
    suspend fun cambiarPassword(@Body dto: CambioPasswordDto): Response<Unit>
}