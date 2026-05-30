package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carcare.model.Role

@Composable
fun LoginScreen(onLogin: (Role, String?) -> Unit) {
    var idCardNumber by remember { mutableStateOf("") }
    var isDriverMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "CarCare", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        if (!isDriverMode) {
            Button(
                onClick = { onLogin(Role.ADMIN, null) },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Entrar como Admin")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { isDriverMode = true },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Soy Conductor")
            }
        } else {
            Text(text = "Ingrese su Cédula", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = idCardNumber,
                onValueChange = { idCardNumber = it },
                label = { Text("Número de Cédula") },
                modifier = Modifier.fillMaxWidth(0.7f),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { if (idCardNumber.isNotBlank()) onLogin(Role.DRIVER, idCardNumber) },
                modifier = Modifier.fillMaxWidth(0.7f),
                enabled = idCardNumber.isNotBlank()
            ) {
                Text("Entrar")
            }
            TextButton(onClick = { isDriverMode = false }) {
                Text("Volver")
            }
        }
    }
}
