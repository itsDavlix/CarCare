package com.example.carcare.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.sessionDataStore by preferencesDataStore(name = "carcare_session")

/**
 * Persistencia de la sesión (token JWT + datos del usuario) en DataStore, para
 * el auto-login al reabrir la app. [AuthSession] sigue siendo la fuente de verdad
 * en memoria; este store solo la respalda en disco.
 *
 * El usuario controla el auto-login desde su perfil:
 *  - [autoLoginEnabledFlow] (default ON): si lo apaga, [load] no restaura nada.
 *  - [autoLoginDaysFlow] (default 7): a cuántos días caduca la sesión guardada.
 * Estas preferencias **sobreviven al logout** (clear solo borra las claves de sesión).
 *
 * - Las escrituras de sesión son fire-and-forget (no bloquean al loguear/cerrar).
 * - [load] descarta sesiones más viejas que el plazo elegido: un token vencido no
 *   sirve para auto-login y dispararía 401 en cada request.
 *
 * Hay que llamar [init] una vez al arrancar (MainActivity) antes de usarlo.
 */
object SessionStore {

    private lateinit var appContext: Context
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dayMs = 24L * 60 * 60 * 1000

    /** Opciones de duración ofrecidas en el perfil (días). */
    val DAY_OPTIONS = listOf(1, 7, 30)
    const val DEFAULT_DAYS = 7
    const val DEFAULT_ENABLED = true

    // Claves de sesión (las borra clear()).
    private val kToken = stringPreferencesKey("token")
    private val kRole = stringPreferencesKey("role")
    private val kNombre = stringPreferencesKey("nombre")
    private val kCedula = stringPreferencesKey("cedula")
    private val kConductorId = longPreferencesKey("conductorId")
    private val kDebe = booleanPreferencesKey("debeCambiarPassword")
    private val kSavedAt = longPreferencesKey("savedAt")

    // Claves de preferencia (NO las borra clear(): son ajustes del usuario, no la sesión).
    private val kAutoLoginEnabled = booleanPreferencesKey("autoLoginEnabled")
    private val kAutoLoginDays = intPreferencesKey("autoLoginDays")

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val ready get() = ::appContext.isInitialized

    /** Respalda la sesión completa (lo llama AuthSession.start tras un login real). */
    fun persist(
        token: String,
        role: String,
        nombre: String?,
        cedula: String,
        conductorId: Long?,
        debeCambiarPassword: Boolean
    ) {
        if (!ready) return
        io.launch {
            appContext.sessionDataStore.edit { p ->
                p[kToken] = token
                p[kRole] = role
                p[kCedula] = cedula
                p[kDebe] = debeCambiarPassword
                p[kSavedAt] = System.currentTimeMillis()
                if (nombre != null) p[kNombre] = nombre else p.remove(kNombre)
                if (conductorId != null) p[kConductorId] = conductorId else p.remove(kConductorId)
            }
        }
    }

    /** Actualiza solo la bandera (tras el cambio de contraseña forzado). */
    fun updateDebeCambiarPassword(value: Boolean) {
        if (!ready) return
        io.launch { appContext.sessionDataStore.edit { it[kDebe] = value } }
    }

    /**
     * Borra la sesión persistida (lo llama AuthSession.clear al cerrar sesión).
     * Solo quita las claves de sesión: las preferencias de auto-login se conservan.
     */
    fun clear() {
        if (!ready) return
        io.launch {
            appContext.sessionDataStore.edit { p ->
                listOf(kToken, kRole, kNombre, kCedula, kConductorId, kDebe, kSavedAt)
                    .forEach { p.remove(it) }
            }
        }
    }

    /** Lee la sesión persistida, o null si no hay, está vencida, o el usuario apagó el auto-login. */
    suspend fun load(): Persisted? {
        if (!ready) return null
        val p = appContext.sessionDataStore.data.first()
        if (!(p[kAutoLoginEnabled] ?: DEFAULT_ENABLED)) return null
        val token = p[kToken] ?: return null
        val savedAt = p[kSavedAt] ?: 0L
        val days = (p[kAutoLoginDays] ?: DEFAULT_DAYS).coerceAtLeast(1)
        if (System.currentTimeMillis() - savedAt > days * dayMs) {
            clear()
            return null
        }
        return Persisted(
            token = token,
            role = p[kRole] ?: "CONDUCTOR",
            nombre = p[kNombre],
            cedula = p[kCedula] ?: "",
            conductorId = p[kConductorId],
            debeCambiarPassword = p[kDebe] ?: false
        )
    }

    // --- Preferencias de auto-login (observables + setters para el perfil) ---

    fun autoLoginEnabledFlow(): Flow<Boolean> =
        prefsFlow { it[kAutoLoginEnabled] ?: DEFAULT_ENABLED }

    fun autoLoginDaysFlow(): Flow<Int> =
        prefsFlow { (it[kAutoLoginDays] ?: DEFAULT_DAYS).coerceAtLeast(1) }

    suspend fun setAutoLoginEnabled(enabled: Boolean) {
        if (!ready) return
        appContext.sessionDataStore.edit { it[kAutoLoginEnabled] = enabled }
    }

    suspend fun setAutoLoginDays(days: Int) {
        if (!ready) return
        appContext.sessionDataStore.edit { it[kAutoLoginDays] = days.coerceAtLeast(1) }
    }

    private fun <T> prefsFlow(read: (Preferences) -> T): Flow<T> =
        appContext.sessionDataStore.data.map(read)

    data class Persisted(
        val token: String,
        val role: String,
        val nombre: String?,
        val cedula: String,
        val conductorId: Long?,
        val debeCambiarPassword: Boolean
    )
}
