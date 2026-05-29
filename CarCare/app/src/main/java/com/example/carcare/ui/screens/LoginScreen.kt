package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carcare.model.Role

@Composable
fun LoginScreen(onRoleSelected: (Role) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "CarCare", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onRoleSelected(Role.ADMIN) },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Entrar como Admin")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onRoleSelected(Role.DRIVER) },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Entrar como Conductor")
        }
    }
}
