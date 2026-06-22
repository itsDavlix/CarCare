package com.example.carcare.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
 * Caducidad por AUSENCIA, no por tiempo de uso: la sesión guardada vale 30 min desde
 * que se SALE de la app ([touchLastActive] marca el momento). Si el usuario vuelve dentro
 * de esa ventana, entra sin re-loguear; pasada, [load] la descarta y debe iniciar sesión.
 * No corta al usuario mientras usa la app (eso lo limita la vida del token JWT en el backend).
 *
 * El usuario controla el auto-login desde su perfil con [autoLoginEnabledFlow] (default ON):
 * si lo apaga, [load] no restaura nada. Esa preferencia **sobrevive al logout**.
 *
 * Hay que llamar [init] una vez al arrancar (MainActivity) antes de usarlo.
 */
object SessionStore {

    private lateinit var appContext: Context
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Ventana de auto-login: la sesión guardada caduca a los 30 min de salir de la app. */
    private val resumeWindowMs = 30L * 60 * 1000

    const val DEFAULT_ENABLED = true

    /** Modo de tema elegido por el usuario (preferencia, sobrevive al logout). */
    const val THEME_SYSTEM = "SYSTEM"
    const val THEME_LIGHT = "LIGHT"
    const val THEME_DARK = "DARK"

    // Claves de sesión (las borra clear()).
    private val kToken = stringPreferencesKey("token")
    private val kRole = stringPreferencesKey("role")
    private val kNombre = stringPreferencesKey("nombre")
    private val kCedula = stringPreferencesKey("cedula")
    private val kConductorId = longPreferencesKey("conductorId")
    private val kDebe = booleanPreferencesKey("debeCambiarPassword")
    private val kLastActive = longPreferencesKey("lastActiveAt")

    // Claves de preferencia (NO las borra clear(): son ajustes del usuario, no la sesión).
    private val kAutoLoginEnabled = booleanPreferencesKey("autoLoginEnabled")
    private val kThemeMode = stringPreferencesKey("themeMode")

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
                p[kLastActive] = System.currentTimeMillis()
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
     * Marca "última actividad" = ahora. Se llama al SALIR de la app (background): a partir de
     * ese momento corre la ventana de 30 min del auto-login. Fire-and-forget.
     */
    fun touchLastActive() {
        if (!ready) return
        io.launch { appContext.sessionDataStore.edit { it[kLastActive] = System.currentTimeMillis() } }
    }

    /**
     * Borra la sesión persistida (lo llama AuthSession.clear al cerrar sesión).
     * Solo quita las claves de sesión: las preferencias de auto-login se conservan.
     */
    fun clear() {
        if (!ready) return
        io.launch {
            appContext.sessionDataStore.edit { p ->
                listOf(kToken, kRole, kNombre, kCedula, kConductorId, kDebe, kLastActive)
                    .forEach { p.remove(it) }
            }
        }
    }

    /** Lee la sesión persistida, o null si no hay, caducó (>30 min ausente) o el auto-login está apagado. */
    suspend fun load(): Persisted? {
        if (!ready) return null
        val p = appContext.sessionDataStore.data.first()
        if (!(p[kAutoLoginEnabled] ?: DEFAULT_ENABLED)) return null
        val token = p[kToken] ?: return null
        val lastActive = p[kLastActive] ?: 0L
        // Caduca si pasaron más de 30 min desde que se salió de la app.
        if (System.currentTimeMillis() - lastActive > resumeWindowMs) {
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

    /** ¿Caducó la sesión guardada por ausencia? (para el chequeo al volver del background). */
    suspend fun isExpiredByAbsence(): Boolean {
        if (!ready) return false
        val p = appContext.sessionDataStore.data.first()
        val lastActive = p[kLastActive] ?: return false
        return System.currentTimeMillis() - lastActive > resumeWindowMs
    }

    // --- Preferencias de auto-login (observable + setter para el perfil) ---

    fun autoLoginEnabledFlow(): Flow<Boolean> =
        prefsFlow { it[kAutoLoginEnabled] ?: DEFAULT_ENABLED }

    suspend fun setAutoLoginEnabled(enabled: Boolean) {
        if (!ready) return
        appContext.sessionDataStore.edit { it[kAutoLoginEnabled] = enabled }
    }

    fun themeModeFlow(): Flow<String> = prefsFlow { it[kThemeMode] ?: THEME_SYSTEM }

    suspend fun setThemeMode(mode: String) {
        if (!ready) return
        appContext.sessionDataStore.edit { it[kThemeMode] = mode }
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
