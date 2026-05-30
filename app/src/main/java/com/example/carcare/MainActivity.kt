package com.example.carcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
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
    // Controla si el splash ya terminó
    var splashFinished by remember { mutableStateOf(false) }
    var currentRole by remember { mutableStateOf<Role?>(null) }
    var loggedInDriverIdCard by remember { mutableStateOf<String?>(null) }

    when {
        !splashFinished -> SplashScreen(onSplashFinished = { splashFinished = true })
        currentRole == null -> LoginScreen(onLogin = { role, idCard -> 
            currentRole = role
            loggedInDriverIdCard = idCard
        })
        currentRole == Role.ADMIN -> AdminScreen(onBack = { 
            currentRole = null 
            loggedInDriverIdCard = null
        })
        currentRole == Role.DRIVER -> DriverScreen(
            driverIdCard = loggedInDriverIdCard ?: "",
            onBack = { 
                currentRole = null 
                loggedInDriverIdCard = null
            }
        )
    }
}
