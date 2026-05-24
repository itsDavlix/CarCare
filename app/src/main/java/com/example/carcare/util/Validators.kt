package com.example.carcare.util

import java.util.Calendar
import java.util.Date

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        val Valid = ValidationResult(true)
        fun invalid(message: String) = ValidationResult(false, message)
    }
}

object Validators {

    // ---------- Placas de Nicaragua (particulares) ----------

    /**
     * Códigos válidos para placas en Nicaragua según
     * la Dirección de Seguridad de Tránsito Nacional (DSTN).
     */
    private val NICARAGUA_PLATE_CODES = listOf(
        "M",     // Managua
        "BO",    // Boaco
        "CA",    // Carazo
        "CI",    // Chinandega
        "CO",    // Chontales
        "ES",    // Estelí
        "GR",    // Granada
        "JI",    // Jinotega
        "LE",    // León
        "MD",    // Madriz
        "MS",    // Masaya
        "MT",    // Matagalpa
        "NS",    // Nueva Segovia
        "RI",    // Rivas
        "SJ",    // Río San Juan
        "RACCN", // Costa Caribe Norte
        "RACCS"  // Costa Caribe Sur
    )

    // Los códigos más largos van primero para que la regex no haga match parcial.
    private val plateCodeAlternation = NICARAGUA_PLATE_CODES
        .sortedByDescending { it.length }
        .joinToString("|")

    /**
     * Acepta: "M 123 456", "M-123-456", "M123456", "MT 12345", "RACCN 12345".
     * Total de dígitos: 5 o 6, con separadores opcionales (espacio o guion) entre grupos.
     */
    private val nicaraguaPlateRegex = Regex(
        "^($plateCodeAlternation)[\\s-]?\\d{3}[\\s-]?\\d{2,3}$",
        RegexOption.IGNORE_CASE
    )

    // Regex para separar el código de letras del bloque de dígitos al formatear.
    private val plateSplitRegex = Regex("^([A-Z]+)(\\d+)$")

    /**
     * Normaliza una placa quitando espacios y guiones, en mayúsculas.
     * Ej: "m 123-456" → "M123456". Esta es la forma en que se ALMACENA.
     */
    fun normalizePlate(plate: String): String {
        return plate.uppercase().replace(Regex("[\\s-]"), "")
    }

    /**
     * Formatea una placa NORMALIZADA para mostrar al usuario.
     * Ej: "M123456" → "M 123 456", "MT12345" → "MT 123 45", "RACCN12345" → "RACCN 123 45".
     * Si la placa no matchea el patrón esperado, se devuelve tal cual.
     */
    fun formatPlate(normalizedPlate: String): String {
        val match = plateSplitRegex.matchEntire(normalizedPlate.uppercase()) ?: return normalizedPlate
        val letters = match.groupValues[1]
        val digits = match.groupValues[2]
        // Partimos los dígitos en grupos: primeros 3 + resto (2 o 3)
        return if (digits.length >= 5) {
            "$letters ${digits.substring(0, 3)} ${digits.substring(3)}"
        } else {
            "$letters $digits"
        }
    }

    /**
     * Valida que una placa cumpla con el formato nicaragüense.
     * @param plate texto ingresado por el usuario (sin normalizar)
     * @param existingPlates lista de pares (id, placaNormalizada) ya registradas
     * @param currentId id del vehículo en edición (para excluir su propia placa)
     */
    fun validatePlate(
        plate: String,
        existingPlates: List<Pair<String, String>> = emptyList(),
        currentId: String? = null
    ): ValidationResult {
        val trimmed = plate.trim()
        if (trimmed.isBlank()) {
            return ValidationResult.invalid("La placa es obligatoria")
        }
        if (!nicaraguaPlateRegex.matches(trimmed)) {
            return ValidationResult.invalid("Formato inválido. Ej: M 123 456 o MT 12345")
        }
        val normalized = normalizePlate(trimmed)
        val duplicate = existingPlates.any { (id, p) -> p == normalized && id != currentId }
        if (duplicate) {
            return ValidationResult.invalid("Esta placa ya está registrada")
        }
        return ValidationResult.Valid
    }

    // ---------- Vehículo ----------

    fun validateRequired(value: String, fieldName: String): ValidationResult {
        return if (value.trim().isBlank()) {
            ValidationResult.invalid("$fieldName es obligatorio")
        } else ValidationResult.Valid
    }

    fun validateYear(yearStr: String): ValidationResult {
        if (yearStr.isBlank()) return ValidationResult.invalid("El año es obligatorio")
        val year = yearStr.toIntOrNull() ?: return ValidationResult.invalid("Año inválido")
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        if (year < 1980 || year > currentYear + 1) {
            return ValidationResult.invalid("Año debe estar entre 1980 y ${currentYear + 1}")
        }
        return ValidationResult.Valid
    }

    fun validateMileage(mileageStr: String): ValidationResult {
        if (mileageStr.isBlank()) return ValidationResult.invalid("El kilometraje es obligatorio")
        val km = mileageStr.toLongOrNull() ?: return ValidationResult.invalid("Kilometraje inválido")
        if (km < 0) return ValidationResult.invalid("El kilometraje no puede ser negativo")
        if (km > 2_000_000) return ValidationResult.invalid("Kilometraje fuera de rango")
        return ValidationResult.Valid
    }

    /**
     * Valida kilometraje opcional (acepta vacío).
     * Útil para campos como "próximo kilometraje" del mantenimiento.
     */
    fun validateOptionalMileage(mileageStr: String): ValidationResult {
        if (mileageStr.isBlank()) return ValidationResult.Valid
        val km = mileageStr.toLongOrNull() ?: return ValidationResult.invalid("Kilometraje inválido")
        if (km < 0) return ValidationResult.invalid("El kilometraje no puede ser negativo")
        return ValidationResult.Valid
    }

    // ---------- Conductor ----------

    fun validateAge(ageStr: String): ValidationResult {
        if (ageStr.isBlank()) return ValidationResult.invalid("La edad es obligatoria")
        val age = ageStr.toIntOrNull() ?: return ValidationResult.invalid("Edad inválida")
        if (age < 18) return ValidationResult.invalid("El conductor debe tener al menos 18 años")
        if (age > 75) return ValidationResult.invalid("Edad máxima permitida: 75 años")
        return ValidationResult.Valid
    }

    /**
     * Cédula nicaragüense: 13 caracteres alfanuméricos (formato XXX-DDMMYY-XXXXL).
     * Normaliza internamente, así que [existingIds] puede pasar valores crudos o normalizados.
     */
    fun validateIdCard(
        idCard: String,
        existingIds: List<Pair<String, String>> = emptyList(),
        currentId: String? = null
    ): ValidationResult {
        val trimmed = idCard.trim()
        if (trimmed.isBlank()) return ValidationResult.invalid("La cédula es obligatoria")
        val normalized = normalizeIdCard(trimmed)
        if (normalized.length !in 13..16) {
            return ValidationResult.invalid("Cédula debe tener 13 caracteres")
        }
        if (!normalized.all { it.isLetterOrDigit() }) {
            return ValidationResult.invalid("Cédula solo acepta letras y números")
        }
        // Normalizamos también los existentes para comparar parejo
        val duplicate = existingIds.any { (id, c) ->
            normalizeIdCard(c) == normalized && id != currentId
        }
        if (duplicate) {
            return ValidationResult.invalid("Esta cédula ya está registrada")
        }
        return ValidationResult.Valid
    }

    fun normalizeIdCard(idCard: String): String {
        return idCard.trim().replace("-", "").uppercase()
    }

    fun validatePhone(phone: String): ValidationResult {
        val trimmed = phone.trim()
        if (trimmed.isBlank()) return ValidationResult.invalid("El teléfono es obligatorio")
        val digits = trimmed.filter { it.isDigit() }
        if (digits.length !in 8..15) {
            return ValidationResult.invalid("Teléfono debe tener entre 8 y 15 dígitos")
        }
        return ValidationResult.Valid
    }

    fun validateLicenseExpiry(date: Date?): ValidationResult {
        if (date == null) return ValidationResult.invalid("La fecha de vencimiento es obligatoria")
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
        if (date.before(today)) {
            return ValidationResult.invalid("La licencia no puede estar vencida al registrarse")
        }
        return ValidationResult.Valid
    }

    // ---------- Mantenimiento ----------

    fun validateMaintenanceDates(startDate: Date?, completionDate: Date?): ValidationResult {
        if (startDate == null) return ValidationResult.invalid("Fecha de inicio obligatoria")
        if (completionDate != null && completionDate.before(startDate)) {
            return ValidationResult.invalid("Finalización no puede ser anterior al inicio")
        }
        return ValidationResult.Valid
    }

    /**
     * Valida que el kilometraje del mantenimiento sea coherente con el del vehículo.
     * No puede ser menor (el vehículo no retrocede km).
     */
    fun validateMaintenanceMileage(
        mileageStr: String,
        vehicleCurrentMileage: Long
    ): ValidationResult {
        val basic = validateMileage(mileageStr)
        if (!basic.isValid) return basic
        val km = mileageStr.toLongOrNull() ?: return basic
        if (km < vehicleCurrentMileage) {
            return ValidationResult.invalid(
                "Debe ser ≥ kilometraje actual del vehículo ($vehicleCurrentMileage km)"
            )
        }
        return ValidationResult.Valid
    }

    // ---------- Asignación ----------

    fun validateAssignmentDates(departureDate: Date?, plannedReturnDate: Date?): ValidationResult {
        if (departureDate == null) return ValidationResult.invalid("Fecha de salida obligatoria")
        if (plannedReturnDate == null) return ValidationResult.invalid("Fecha de retorno obligatoria")
        if (plannedReturnDate.before(departureDate)) {
            return ValidationResult.invalid("Retorno no puede ser anterior a la salida")
        }
        return ValidationResult.Valid
    }

    fun validateFinalMileage(finalStr: String, initialMileage: Long): ValidationResult {
        if (finalStr.isBlank()) return ValidationResult.invalid("Kilometraje final obligatorio")
        val km = finalStr.toLongOrNull() ?: return ValidationResult.invalid("Kilometraje inválido")
        if (km < initialMileage) {
            return ValidationResult.invalid("Final ($km) no puede ser menor al inicial ($initialMileage)")
        }
        return ValidationResult.Valid
    }
}