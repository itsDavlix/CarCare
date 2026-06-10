package com.example.carcare.data.network.api

import com.example.carcare.data.network.dto.LoginRequestDto
import com.example.carcare.data.network.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body dto: LoginRequestDto): LoginResponseDto
}