package com.example.carcare.data.network

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

/**
 * Cliente SSE (Server-Sent Events). Escucha /api/events y avisa que entidad
 * cambio ("vehiculos", "conductores", "mantenimientos", "asignaciones").
 *
 * readTimeout = 0 -> el stream queda abierto sin limite. El backend manda un
 * "ping" cada 20s para mantenerlo vivo. Si la conexion se cae, reconecta solo.
 */
object SseClient {

    private const val TAG = "SseClient"
    private val EVENTS_URL = ApiClient.BASE_URL + "api/events"
    private const val RECONNECT_DELAY_MS = 3_000L

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // sin timeout: stream persistente
        .build()

    /** Abre la conexion. Llama [Connection.cancel] para cerrarla (en onDispose). */
    fun connect(onEntity: (String) -> Unit): Connection {
        val conn = Connection(client, EVENTS_URL, onEntity)
        conn.start()
        return conn
    }

    class Connection(
        private val client: OkHttpClient,
        private val url: String,
        private val onEntity: (String) -> Unit
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        @Volatile private var eventSource: EventSource? = null

        fun start() = openStream()

        private fun openStream() {
            if (!scope.isActive) return
            val request = Request.Builder().url(url).build()
            eventSource = EventSources.createFactory(client).newEventSource(request, listener)
        }

        private val listener = object : EventSourceListener() {
            override fun onOpen(es: EventSource, response: Response) {
                Log.d(TAG, "Conectado a $url")
            }

            override fun onEvent(es: EventSource, id: String?, type: String?, data: String) {
                val entidad = data.trim()
                if (entidad.isNotEmpty()) {
                    Log.d(TAG, "Evento recibido: $entidad")
                    onEntity(entidad)
                }
            }

            override fun onClosed(es: EventSource) {
                reconnect("cerrado por el servidor")
            }

            override fun onFailure(es: EventSource, t: Throwable?, response: Response?) {
                reconnect(t?.message ?: "fallo de conexion")
            }
        }

        private fun reconnect(motivo: String) {
            eventSource?.cancel()
            eventSource = null
            if (!scope.isActive) return
            Log.d(TAG, "Reconectando ($motivo) en ${RECONNECT_DELAY_MS}ms")
            scope.launch {
                delay(RECONNECT_DELAY_MS)
                openStream()
            }
        }

        fun cancel() {
            scope.cancel()
            eventSource?.cancel()
            eventSource = null
            Log.d(TAG, "Conexion SSE cerrada")
        }
    }
}