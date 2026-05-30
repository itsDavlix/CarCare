package com.example.carcare

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.carcare.data.network.ApiClient
import com.example.carcare.model.Role
import com.example.carcare.ui.screens.AdminScreen
import com.example.carcare.ui.screens.DriverScreen
import com.example.carcare.ui.screens.LoginScreen
import com.example.carcare.ui.screens.SplashScreen
import com.example.carcare.ui.theme.CarCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarCareTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    // ----- TEMPORAL: prueba de conectividad con la API -----
    // Se ejecuta una sola vez al abrir la app. Mira el Logcat (tag "ApiTest").
    // Borrar esta seccion cuando los ViewModels usen la API directamente.
    LaunchedEffect(Unit) {
        try {
            val vehiculos = ApiClient.vehiculoApi.listar()
            Log.d("ApiTest", "OK - ${vehiculos.size} vehiculos recibidos")
            val conductores = ApiClient.conductorApi.listar()
            Log.d("ApiTest", "Conductores: ${conductores.size}")
            val mantenimientos = ApiClient.mantenimientoApi.listar()
            Log.d("ApiTest", "Mantenimientos: ${mantenimientos.size}")
            val asignaciones = ApiClient.asignacionApi.listar()
            Log.d("ApiTest", "Asignaciones: ${asignaciones.size}")
            vehiculos.forEach {
                Log.d("ApiTest", "  id=${it.id} placa=${it.placa} ${it.marca} ${it.modelo} estado=${it.estado}")
            }
        } catch (e: Exception) {
            Log.e("ApiTest", "ERROR al llamar la API", e)
        }
    }
    // ----- FIN TEMPORAL -----

    var splashFinished by remember { mutableStateOf(false) }
    var currentRole by remember { mutableStateOf<Role?>(null) }

    when {
        !splashFinished -> SplashScreen(onSplashFinished = { splashFinished = true })
        currentRole == null -> LoginScreen(onRoleSelected = { currentRole = it })
        currentRole == Role.ADMIN -> AdminScreen(onBack = { currentRole = null })
        currentRole == Role.DRIVER -> DriverScreen(onBack = { currentRole = null })
    }
}