package com.example.carcare.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.carcare.model.AssignmentStatus
import com.example.carcare.model.DriverStatus
import com.example.carcare.model.MaintenanceStatus
import com.example.carcare.model.VehicleStatus
import com.example.carcare.ui.theme.statusColor

/* ───────────────────────── Badges de estado ─────────────────────────
   Un punto del color del estado + etiqueta, en una píldora tintada con el
   MISMO tono (texto en el color del estado sobre su propio tinte → buen
   contraste, evita el gris lavado). Una sola implementación (BadgeChip) y un
   overload de StatusBadge por enum del dominio (label + statusColor desde
   ui/theme/StatusColors.kt).                                              */

@Composable
private fun BadgeChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.14f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
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

/* ───────────────────────── Primitivas reutilizables ───────────────────────── */

/**
 * Contenedor de icono tintado (la "ficha" que abre cada fila de lista). El tinte
 * comunica el color del dominio sin recurrir a franjas laterales.
 */
@Composable
fun LeadingTile(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Int = 52
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(MaterialTheme.shapes.small)
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size * 0.46f).dp))
    }
}

/**
 * Estado vacío con personalidad: icono en círculo tintado + título + subtítulo.
 * Sustituye los "No hay…" sueltos centrados.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.size(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }
    }
}

/**
 * Realimentación táctil sobria: la tarjeta se hunde ~2% al presionar.
 * Rápida (110/150ms) y con ease estándar — acción frecuente, sin rebote.
 */
@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = tween(if (pressed) 110 else 150),
        label = "pressScale"
    )
    return this.scale(scale)
}

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
