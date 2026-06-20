package com.example.carcare.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.carcare.model.Maintenance
import com.example.carcare.model.Vehicle
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Exporta datos a CSV y abre el selector para compartir/guardar el archivo.
 * El CSV se escribe en cacheDir y se comparte vía FileProvider (ver AndroidManifest).
 */
object CsvExporter {

    /** Exporta el historial de mantenimientos (cruzando con vehículos para placa/marca). */
    fun exportMaintenances(context: Context, maintenances: List<Maintenance>, vehicles: List<Vehicle>) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val headers = listOf(
            "Vehiculo", "Placa", "Tipo", "Fecha", "Km", "Estado", "Responsable", "Descripcion"
        )
        val rows = maintenances.map { m ->
            val v = vehicles.firstOrNull { it.id == m.vehicleId }
            listOf(
                v?.let { "${it.brand} ${it.model}" } ?: "Vehiculo eliminado",
                v?.let { Validators.formatPlate(it.plate) } ?: "",
                m.type.label,
                sdf.format(m.date),
                m.currentMileage.toString(),
                m.status.label,
                m.responsible,
                m.description
            )
        }
        share(context, "mantenimientos_${System.currentTimeMillis()}.csv", headers, rows)
    }

    private fun share(context: Context, fileName: String, headers: List<String>, rows: List<List<String>>) {
        val csv = buildString {
            append('﻿') // BOM: ayuda a que Excel reconozca el UTF-8 (acentos)
            appendLine(headers.joinToString(",") { escape(it) })
            rows.forEach { row -> appendLine(row.joinToString(",") { escape(it) }) }
        }
        val file = File(context.cacheDir, fileName).apply { writeText(csv) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar CSV"))
    }

    /** Encierra en comillas y duplica las comillas internas si el valor tiene coma/comilla/salto. */
    private fun escape(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
}
