package com.example.carcare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.carcare.model.Notificacion
import com.example.carcare.model.NotificationType
import com.example.carcare.ui.theme.Amber
import com.example.carcare.ui.theme.StatusAvailable
import com.example.carcare.ui.theme.StatusInUse
import com.example.carcare.ui.theme.StatusInactive
import com.example.carcare.ui.theme.StatusMaintenance
import com.example.carcare.ui.theme.StatusOutOfService
import com.example.carcare.ui.theme.instrumentSmall
import java.text.SimpleDateFormat
import java.util.Locale

/** Fila de notificación: ficha de icono por tipo + mensaje + hora; punto ámbar si no-leída. */
@Composable
fun NotificationItem(
    notification: Notificacion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeadingTile(icon = notification.type.icon(), tint = notification.type.accent(), size = 40)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(notification.message, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Text(
                sdf.format(notification.timestamp),
                style = instrumentSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!notification.read) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(8.dp).clip(CircleShape).background(Amber))
        }
    }
}

private fun NotificationType.icon(): ImageVector = when (this) {
    NotificationType.VEHICULO -> Icons.Default.DirectionsCar
    NotificationType.CONDUCTOR -> Icons.Default.Person
    NotificationType.MANTENIMIENTO -> Icons.Default.Build
    NotificationType.ASIGNACION -> Icons.AutoMirrored.Filled.Assignment
    NotificationType.SEGURIDAD -> Icons.Default.Lock
    NotificationType.OTRO -> Icons.Default.Notifications
}

@Composable
private fun NotificationType.accent(): Color = when (this) {
    NotificationType.VEHICULO -> StatusInUse
    NotificationType.CONDUCTOR -> StatusAvailable
    NotificationType.MANTENIMIENTO -> StatusMaintenance
    NotificationType.ASIGNACION -> MaterialTheme.colorScheme.primary
    NotificationType.SEGURIDAD -> StatusOutOfService
    NotificationType.OTRO -> StatusInactive
}
