package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carcare.data.Maintenance
import com.example.carcare.ui.viewmodel.MaintenanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceScreen(
    viewModel: MaintenanceViewModel,
    vehicleId: Int,
    onNavigateBack: () -> Unit
) {
    var type by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var workshop by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var nextMileage by remember { mutableStateOf("") }
    var nextDate by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Mantenimiento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Tipo (ej: Cambio de Aceite)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Fecha (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = mileage, onValueChange = { mileage = it }, label = { Text("Kilometraje Actual") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Costo") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(value = workshop, onValueChange = { workshop = it }, label = { Text("Taller") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Próximo Mantenimiento (Opcional)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(value = nextMileage, onValueChange = { nextMileage = it }, label = { Text("Kilometraje Próximo") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = nextDate, onValueChange = { nextDate = it }, label = { Text("Fecha Próxima") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val maintenance = Maintenance(
                        vehicleId = vehicleId,
                        type = type,
                        date = date,
                        mileage = mileage.toIntOrNull() ?: 0,
                        cost = cost.toDoubleOrNull() ?: 0.0,
                        workshop = workshop,
                        description = description,
                        nextMileage = nextMileage.toIntOrNull(),
                        nextDate = nextDate.ifBlank { null }
                    )
                    viewModel.insertMaintenance(maintenance)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = type.isNotBlank() && date.isNotBlank(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Guardar Mantenimiento")
            }
        }
    }
}
