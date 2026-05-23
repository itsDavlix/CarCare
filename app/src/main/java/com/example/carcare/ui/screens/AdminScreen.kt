package com.example.carcare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carcare.model.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Vehículos", "Mantenimiento", "Asignaciones")

    val vehicles by remember { mutableStateOf(listOf(
        Vehicle("1", "ABC-123", "Toyota Hilux", 2022, "Disponible"),
        Vehicle("2", "XYZ-789", "Ford Ranger", 2021, "En Mantenimiento")
    )) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Admin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = {
                            when (index) {
                                0 -> Icon(Icons.Default.DirectionsCar, contentDescription = null)
                                1 -> Icon(Icons.Default.Build, contentDescription = null)
                                else -> Icon(Icons.Default.Person, contentDescription = null)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { /* Acción para agregar vehículo */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Vehículo")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(tabs[selectedTab], style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            when (selectedTab) {
                0 -> LazyColumn {
                    items(vehicles) { vehicle ->
                        VehicleItem(vehicle)
                    }
                }
                1 -> Text("Lista de mantenimientos pendientes y realizados...")
                2 -> Text("Gestión de asignaciones de vehículos a conductores...")
            }
        }
    }
}

@Composable
fun VehicleItem(vehicle: Vehicle) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${vehicle.model} (${vehicle.year})", style = MaterialTheme.typography.titleMedium)
            Text(text = "Placa: ${vehicle.plate}")
            Text(text = "Estado: ${vehicle.status}")
        }
    }
}
