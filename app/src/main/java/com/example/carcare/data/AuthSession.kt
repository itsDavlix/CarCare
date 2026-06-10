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

    @Volatile
    var token: String? = null
        private set

    var role: Role? = null
        private set

    var nombre: String? = null
        private set

    var cedula: String? = null
        private set

    var conductorId: Long? = null
        private set

    val isLoggedIn: Boolean get() = token != null

    fun start(token: String, role: Role, nombre: String?, cedula: String, conductorId: Long?) {
        this.token = token
        this.role = role
        this.nombre = nombre
        this.cedula = cedula
        this.conductorId = conductorId
    }

    fun clear() {
        token = null
        role = null
        nombre = null
        cedula = null
        conductorId = null
    }
}