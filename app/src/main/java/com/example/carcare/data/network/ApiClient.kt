package com.example.carcare.data.network

import com.example.carcare.BuildConfig
import com.example.carcare.data.AuthSession
import com.example.carcare.data.network.api.AsignacionApiService
import com.example.carcare.data.network.api.AuthApiService
import com.example.carcare.data.network.api.ConductorApiService
import com.example.carcare.data.network.api.MantenimientoApiService
import com.example.carcare.data.network.api.NotificacionApiService
import com.example.carcare.data.network.api.VehiculoApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Singleton de Retrofit. Apunta al backend desplegado en Render.
 * Tiempos largos por el "cold start" del free tier de Render.
 */
object ApiClient {

    const val BASE_URL = "https://api-carcare.onrender.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Sin logging en release: evita el overhead por request y no expone datos en producción.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
        else HttpLoggingInterceptor.Level.NONE
    }

    /**
     * Agrega "Authorization: Bearer <jwt>" a cada request cuando hay sesión.
     * Sin sesión (login, warmUp) la request sale tal cual: el backend decide
     * qué exige según su SecurityConfig.
     */
    private val authInterceptor = Interceptor { chain ->
        val token = AuthSession.token
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val vehiculoApi: VehiculoApiService by lazy { retrofit.create(VehiculoApiService::class.java) }
    val conductorApi: ConductorApiService by lazy { retrofit.create(ConductorApiService::class.java) }
    val mantenimientoApi: MantenimientoApiService by lazy { retrofit.create(MantenimientoApiService::class.java) }
    val asignacionApi: AsignacionApiService by lazy { retrofit.create(AsignacionApiService::class.java) }
    val authApi: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val notificacionApi: NotificacionApiService by lazy { retrofit.create(NotificacionApiService::class.java) }

    /**
     * Despierta el dyno de Render de forma anticipada (fire-and-forget).
     * Se llama al inicio (durante el splash) para que el cold start se solape con
     * el splash + login, en vez de empezar recién al abrir el panel. Calienta también
     * la conexión a la BD. Cualquier error se ignora: solo busca arrancar el servidor.
     */
    fun warmUp() {
        val request = Request.Builder().url(BASE_URL + "api/health").build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { /* ignorar */ }
            override fun onResponse(call: Call, response: Response) { response.close() }
        })
    }
}