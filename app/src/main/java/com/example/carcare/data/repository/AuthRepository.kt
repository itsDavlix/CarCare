package com.example.carcare.data.repository

import com.example.carcare.data.AuthSession
import com.example.carcare.data.network.ApiClient
import com.example.carcare.data.network.api.AuthApiService
import com.example.carcare.data.network.dto.CambioPasswordDto
import com.example.carcare.data.network.dto.LoginRequestDto
import com.example.carcare.data.network.dto.LoginResponseDto
import com.example.carcare.model.Role

class AuthRepository(
    private val api: AuthApiService = ApiClient.authApi
) {

    /**
     * Autentica contra el backend y deja la sesión iniciada en [AuthSession].
     * Lanza HttpException (400/401) con credenciales inválidas: el ViewModel
     * lo traduce al mensaje genérico de la UI.
     */
    suspend fun login(cedula: String, password: String): LoginResponseDto {
        val response = api.login(LoginRequestDto(cedula = cedula, password = password))
        val role = if (response.rol == "ADMIN") Role.ADMIN else Role.DRIVER
        AuthSession.start(
            token = response.token,
            role = role,
            nombre = response.nombre,
            cedula = response.cedula,
            conductorId = response.conductorId
        )
        return response
    }

    /** Cambio de contraseña self-service del conductor (requiere la contraseña actual). */
    suspend fun changePassword(cedula: String, actual: String, nueva: String) {
        api.cambiarPassword(CambioPasswordDto(cedula = cedula, passwordActual = actual, passwordNueva = nueva))
    }

    fun logout() = AuthSession.clear()
}