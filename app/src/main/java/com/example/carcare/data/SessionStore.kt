package com.example.carcare.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.sessionDataStore by preferencesDataStore(name = "carcare_session")

/**
 * Persistencia de la sesión (token JWT + datos del usuario) en DataStore, para
 * el auto-login al reabrir la app. [AuthSession] sigue siendo la fuente de verdad
 * en memoria; este store solo la respalda en disco.
 *
 * - Las escrituras son fire-and-forget (no bloquean el hilo que loguea/cierra).
 * - [load] descarta sesiones de más de 7 días (= vida del JWT): un token vencido
 *   no sirve para auto-login y dispararía 401 en cada request.
 *
 * Hay que llamar [init] una vez al arrancar (MainActivity) antes de usarlo.
 */
object SessionStore {

    private lateinit var appContext: Context
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sevenDaysMs = 7L * 24 * 60 * 60 * 1000

    private val kToken = stringPreferencesKey("token")
    private val kRole = stringPreferencesKey("role")
    private val kNombre = stringPreferencesKey("nombre")
    private val kCedula = stringPreferencesKey("cedula")
    private val kConductorId = longPreferencesKey("conductorId")
    private val kDebe = booleanPreferencesKey("debeCambiarPassword")
    private val kSavedAt = longPreferencesKey("savedAt")

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

    /** Borra la sesión persistida (lo llama AuthSession.clear al cerrar sesión). */
    fun clear() {
        if (!ready) return
        io.launch { appContext.sessionDataStore.edit { it.clear() } }
    }

    /** Lee la sesión persistida, o null si no hay o está vencida (>7 días). */
    suspend fun load(): Persisted? {
        if (!ready) return null
        val p = appContext.sessionDataStore.data.first()
        val token = p[kToken] ?: return null
        val savedAt = p[kSavedAt] ?: 0L
        if (System.currentTimeMillis() - savedAt > sevenDaysMs) {
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

    data class Persisted(
        val token: String,
        val role: String,
        val nombre: String?,
        val cedula: String,
        val conductorId: Long?,
        val debeCambiarPassword: Boolean
    )
}
