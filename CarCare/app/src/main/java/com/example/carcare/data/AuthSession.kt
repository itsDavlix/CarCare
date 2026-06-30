package com.example.carcare.data

import com.example.carcare.model.Role

/**
 * Sesión de la app en memoria: token JWT, rol y datos del usuario logueado.
 *
 * - ApiClient la lee en cada request para agregar "Authorization: Bearer <token>".
 * - MainActivity la consulta para navegar al panel correcto y la limpia al cerrar sesión.
 *
 * Es en memoria a propósito (v1): al cerrar la app hay que volver a iniciar sesión.
 * La persistencia (auto-login con DataStore) queda como siguiente paso.
 */
object AuthSession {

    // @Volatile en todos los campos: se escriben desde la coroutine de login y se
    // leen desde otros hilos (p. ej. el interceptor de OkHttp lee token).
    @Volatile
    var token: String? = null
        private set

    @Volatile
    var role: Role? = null
        private set

    @Volatile
    var nombre: String? = null
        private set

    @Volatile
    var cedula: String? = null
        private set

    @Volatile
    var conductorId: Long? = null
        private set

    /** true → la clave es la inicial (puesta por otro); la app fuerza cambiarla antes de entrar. */
    @Volatile
    var debeCambiarPassword: Boolean = false
        private set

    val isLoggedIn: Boolean get() = token != null

    /** Login real: setea la sesión EN MEMORIA y la respalda en disco (auto-login). */
    fun start(
        token: String,
        role: Role,
        nombre: String?,
        cedula: String,
        conductorId: Long?,
        debeCambiarPassword: Boolean = false
    ) {
        setFields(token, role, nombre, cedula, conductorId, debeCambiarPassword)
        SessionStore.persist(token, role.name, nombre, cedula, conductorId, debeCambiarPassword)
    }

    /** Auto-login: restaura la sesión desde disco SIN volver a persistirla. */
    fun restore(
        token: String,
        role: Role,
        nombre: String?,
        cedula: String,
        conductorId: Long?,
        debeCambiarPassword: Boolean
    ) {
        setFields(token, role, nombre, cedula, conductorId, debeCambiarPassword)
    }

    private fun setFields(
        token: String,
        role: Role,
        nombre: String?,
        cedula: String,
        conductorId: Long?,
        debeCambiarPassword: Boolean
    ) {
        this.token = token
        this.role = role
        this.nombre = nombre
        this.cedula = cedula
        this.conductorId = conductorId
        this.debeCambiarPassword = debeCambiarPassword
    }

    /** Tras cambiar la clave inicial con éxito: ya no hay que forzar nada. */
    fun markPasswordChanged() {
        debeCambiarPassword = false
        SessionStore.updateDebeCambiarPassword(false)
    }

    fun clear() {
        token = null
        role = null
        nombre = null
        cedula = null
        conductorId = null
        debeCambiarPassword = false
        SessionStore.clear()
    }
}