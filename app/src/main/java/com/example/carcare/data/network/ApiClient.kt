package com.example.carcare.data.network

import com.example.carcare.data.network.api.AsignacionApiService
import com.example.carcare.data.network.api.ConductorApiService
import com.example.carcare.data.network.api.MantenimientoApiService
import com.example.carcare.data.network.api.VehiculoApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton de Retrofit. Apunta al backend desplegado en Render.
 * Tiempos largos por el "cold start" del free tier de Render.
 */
object ApiClient {

    private const val BASE_URL = "https://api-carcare.onrender.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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
}