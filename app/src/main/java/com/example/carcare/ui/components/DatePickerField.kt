package com.example.carcare.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Campo de texto de solo lectura que abre un DatePicker Material 3 al hacer clic en el ícono.
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
    modifier: Modifier = Modifier,
    label: String,
    selectedDate: Date?,
    onDateSelected: (Date) -> Unit,
    isError: Boolean = false,
    supportingText: String? = null,
    minDate: Date? = null,
    maxDate: Date? = null,
    enabled: Boolean = true,

) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val displayText = selectedDate?.let { formatter.format(it) } ?: ""

    OutlinedTextField(
        value = displayText,
        onValueChange = { /* readOnly */ },
        label = { Text(label) },
        readOnly = true,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        trailingIcon = {
            IconButton(
                onClick = { showDialog = true },
                enabled = enabled
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha")
            }
        },
        modifier = modifier.fillMaxWidth()
    )

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
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val afterMin = minDate?.let { utcTimeMillis >= it.time } ?: true
                val beforeMax = maxDate?.let { utcTimeMillis <= it.time } ?: true
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
                        // El DatePicker M3 devuelve UTC; ajustamos al timezone local
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