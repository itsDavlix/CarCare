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
    var currentRole by remember { mutableStateOf<Role?>(null) }

    when (currentRole) {
        null -> LoginScreen(onRoleSelected = { currentRole = it })
        Role.ADMIN -> AdminScreen(onBack = { currentRole = null })
        Role.DRIVER -> DriverScreen(onBack = { currentRole = null })
    }
}
