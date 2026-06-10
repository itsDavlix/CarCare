package com.example.carcare.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.model.DriverStatus
import com.example.carcare.model.MaintenanceStatus
import com.example.carcare.model.VehicleStatus
import com.example.carcare.ui.theme.statusColor

/* ───────────────────────── Badges de estado ─────────────────────────
   Una sola implementación visual (BadgeChip) + un overload de StatusBadge
   por enum del dominio. Etiqueta = enum.label, color = enum.statusColor
   (paleta de marca en ui/theme/StatusColors.kt). Antes había dos badges
   con hex de Material hardcodeados y dos entidades sin badge.            */

@Composable
private fun BadgeChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun StatusBadge(status: VehicleStatus) = BadgeChip(status.label, status.statusColor)

@Composable
fun StatusBadge(status: DriverStatus) = BadgeChip(status.label, status.statusColor)

@Composable
fun StatusBadge(status: MaintenanceStatus) = BadgeChip(status.label, status.statusColor)

@Composable
fun StatusBadge(status: AssignmentStatus) = BadgeChip(status.label, status.statusColor)

/** Alias de transición: mismos pixeles que StatusBadge(status). */
@Deprecated(
    message = "Unificado: usar StatusBadge(status), hay un overload por tipo de estado.",
    replaceWith = ReplaceWith("StatusBadge(status)")
)
@Composable
fun DriverStatusBadge(status: DriverStatus) = StatusBadge(status)

/* ───────────────────────── Diálogos comunes ───────────────────────── */

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}