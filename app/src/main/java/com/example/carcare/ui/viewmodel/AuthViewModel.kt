package com.example.carcare.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcare.data.network.toUserMessage
import com.example.carcare.data.repository.AuthRepository
import com.example.carcare.model.Role
import com.example.carcare.util.ValidationResult
import com.example.carcare.util.Validators
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var cedula by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var showPassword by mutableStateOf(false)
        private set

    var attempted by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** Sube en cada fallo de login: dispara el shake aunque el mensaje se repita. */
    var errorNonce by mutableIntStateOf(0)
        private set

    /** La intro splash→login se reproduce una sola vez por proceso. */
    var introPlayed by mutableStateOf(false)
        private set

    // El campo guarda la cédula NORMALIZADA (sin guiones); los guiones los pinta
    // la VisualTransformation. Solo dígitos/letras, mayúsculas, máx 14.
    fun onCedulaChange(value: String) {
        cedula = value.uppercase().filter { it.isLetterOrDigit() }.take(14)
        errorMessage = null
    }

    fun onPasswordChange(value: String) {
        password = value
        errorMessage = null
    }

    fun togglePasswordVisibility() {
        showPassword = !showPassword
    }

    fun markIntroPlayed() {
        introPlayed = true
    }

    // Solo formato NI (la unicidad no aplica en login).
    val cedulaValidation: ValidationResult
        get() = Validators.validateIdCard(cedula)

    val passwordValidation: ValidationResult
        get() = if (password.isBlank()) {
            ValidationResult.invalid("La contraseña es obligatoria")
        } else ValidationResult.Valid

    fun login(onSuccess: (Role, String) -> Unit) {
        attempted = true
        if (!cedulaValidation.isValid || !passwordValidation.isValid) return
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.login(cedula, password)
                val role = com.example.carcare.data.AuthSession.role ?: Role.DRIVER
                val sessionCedula = com.example.carcare.data.AuthSession.cedula ?: cedula
                password = ""
                attempted = false
                onSuccess(role, sessionCedula)
            } catch (e: HttpException) {
                errorMessage = if (e.code() in 400..403) {
                    "Cédula o contraseña incorrecta."
                } else {
                    e.toUserMessage()
                }
                errorNonce++
            } catch (e: Exception) {
                errorMessage = e.toUserMessage()
                errorNonce++
            } finally {
                isLoading = false
            }
        }
    }

    /** Cambio de contraseña self-service (perfil del conductor); no toca el estado del login. */
    fun changePassword(
        cedula: String,
        actual: String,
        nueva: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.changePassword(cedula, actual, nueva)
                onSuccess()
            } catch (e: Exception) {
                onError(e.toUserMessage())
            }
        }
    }

    /** Al cerrar sesión: limpia credenciales sensibles, conserva la cédula tecleada. */
    fun onLoggedOut() {
        repository.logout()
        password = ""
        attempted = false
        errorMessage = null
    }
}