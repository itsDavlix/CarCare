package com.example.carcare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Campo de fecha que abre el DatePicker Material 3 al tocar cualquier parte del campo.
 *
 * Implementación: usamos `enabled = false` en el OutlinedTextField para que no
 * intercepte el touch (sino el cursor de texto se activaría). Después sobrescribimos
 * los colores `disabled*` con los colores normales para que visualmente se vea habilitado.
 * El Box exterior recibe el click y abre el diálogo.
 *
 * @param label etiqueta del campo
 * @param selectedDate fecha actualmente seleccionada (null = sin fecha)
 * @param onDateSelected callback cuando el usuario confirma una fecha
 * @param isError marca el campo como inválido visualmente
 * @param supportingText texto de ayuda o error debajo del campo
 * @param minDate fecha mínima seleccionable (opcional)
 * @param maxDate fecha máxima seleccionable (opcional)
 * @param enabled si el campo está habilitado para interacción
 * @param modifier modificador adicional
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit,
    isError: Boolean = false,
    supportingText: String? = null,
    minDate: Date? = null,
    maxDate: Date? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val displayText = selectedDate?.let { formatter.format(it) } ?: ""

    // Colores que mantienen el aspecto de campo HABILITADO aunque el TextField esté disabled.
    // Esto es necesario porque al estar disabled, el Box exterior puede recibir el click
    // sin que el TextField intente abrir el teclado virtual.
    val colors = OutlinedTextFieldDefaults.colors(
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
        disabledLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSupportingTextColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { showDialog = true }
            )
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = { /* no-op */ },
            label = { Text(label) },
            placeholder = { Text("dd/mm/aaaa") },
            // enabled=false hace que el TextField no capture el touch, así el Box puede
            enabled = false,
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            colors = colors,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDialog) {
        DatePickerDialogContent(
            initialDate = selectedDate,
            minDate = minDate,
            maxDate = maxDate,
            onDismiss = { showDialog = false },
            onConfirm = { date ->
                onDateSelected(date)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogContent(
    initialDate: Date?,
    minDate: Date?,
    maxDate: Date?,
    onDismiss: () -> Unit,
    onConfirm: (Date) -> Unit
) {
    val initialMillis = initialDate?.time ?: System.currentTimeMillis()
    // El DatePicker representa cada día como medianoche UTC, mientras que minDate/maxDate
    // son Dates locales (con hora). Comparar contra it.time directamente des-habilitaba el
    // propio día límite (p. ej. "hoy" con minDate = Date()): hay que normalizar ambos lados
    // a medianoche UTC del día calendario local.
    val minUtcMidnight = remember(minDate) { minDate?.toUtcMidnightOfLocalDay() }
    val maxUtcMidnight = remember(maxDate) { maxDate?.toUtcMidnightOfLocalDay() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val afterMin = minUtcMidnight?.let { utcTimeMillis >= it } ?: true
                val beforeMax = maxUtcMidnight?.let { utcTimeMillis <= it } ?: true
                return afterMin && beforeMax
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val tz = TimeZone.getDefault()
                        val offset = tz.getOffset(millis)
                        onConfirm(Date(millis - offset))
                    } ?: onDismiss()
                }
            ) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/**
 * Medianoche UTC del día calendario LOCAL de esta fecha: la unidad con la que el
 * DatePicker de Material 3 representa cada día seleccionable.
 */
private fun Date.toUtcMidnightOfLocalDay(): Long {
    val local = Calendar.getInstance().apply { time = this@toUtcMidnightOfLocalDay }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}