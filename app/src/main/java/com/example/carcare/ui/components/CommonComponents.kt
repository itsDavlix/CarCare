package com.example.carcare.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.carcare.model.VehicleStatus

@Composable
fun StatusBadge(status: VehicleStatus) {
    val color = when (status) {
        VehicleStatus.AVAILABLE -> Color(0xFF4CAF50)
        VehicleStatus.ASSIGNED -> Color(0xFF00BCD4)
        VehicleStatus.IN_USE -> Color(0xFF2196F3)
        VehicleStatus.PENDING_REVIEW -> Color(0xFFFFC107)
        VehicleStatus.MAINTENANCE -> Color(0xFFFF9800)
        VehicleStatus.OUT_OF_SERVICE -> Color(0xFFF44336)
        VehicleStatus.INACTIVE -> Color(0xFF9E9E9E)
    }
    Surface(
        color = color.copy(alpha = 0.2f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
