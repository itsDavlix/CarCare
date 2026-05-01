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
import com.example.carcare.data.Vehicle
import com.example.carcare.ui.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVehicleScreen(
    viewModel: VehicleViewModel,
    vehicleId: Int? = null,
    onNavigateBack: () -> Unit
) {
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var engine by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(vehicleId) {
        if (vehicleId != null) {
            val vehicle = viewModel.getVehicleById(vehicleId)
            if (vehicle != null) {
                brand = vehicle.brand
                model = vehicle.model
                year = vehicle.year.toString()
                engine = vehicle.engine
                plate = vehicle.plate
                mileage = vehicle.mileage.toString()
                fuelType = vehicle.fuelType
                color = vehicle.color
                description = vehicle.description
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vehicleId == null) "Agregar Vehículo" else "Editar Vehículo") },
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
            OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modelo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Año") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(value = engine, onValueChange = { engine = it }, label = { Text("Motor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("Placa") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = mileage,
                onValueChange = { mileage = it },
                label = { Text("Kilometraje") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(value = fuelType, onValueChange = { fuelType = it }, label = { Text("Tipo de Combustible") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val vehicle = Vehicle(
                        id = vehicleId ?: 0,
                        brand = brand,
                        model = model,
                        year = year.toIntOrNull() ?: 0,
                        engine = engine,
                        plate = plate,
                        mileage = mileage.toIntOrNull() ?: 0,
                        fuelType = fuelType,
                        color = color,
                        description = description
                    )
                    if (vehicleId == null) {
                        viewModel.insertVehicle(vehicle)
                    } else {
                        viewModel.updateVehicle(vehicle)
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = brand.isNotBlank() && model.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Guardar Vehículo")
            }
        }
    }
}
