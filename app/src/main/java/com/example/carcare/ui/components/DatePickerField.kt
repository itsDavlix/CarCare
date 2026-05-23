package com.example.carcare.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val displayText = selectedDate?.let { formatter.format(it) } ?: ""

    OutlinedTextField(
        value = displayText,
        onValueChange = { /* readOnly */ },
        label = { Text(label) },
        readOnly = true,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha")
            }
        },
        modifier = modifier.fillMaxWidth()
    )

    if (showDialog) {
        DatePickerDialogContent(
            initialDate = selectedDate,
            minDate = minDate,
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
    onDismiss: () -> Unit,
    onConfirm: (Date) -> Unit
) {
    val initialMillis = initialDate?.time ?: System.currentTimeMillis()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return minDate?.let { utcTimeMillis >= it.time } ?: true
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // El DatePicker devuelve UTC; ajustamos al timezone local
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