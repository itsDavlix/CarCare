package com.example.carcare.util

import java.util.Calendar
import java.util.Date

/**
 * Resultado de una validación.
 * - Si [isValid] es true, el dato es válido y [errorMessage] es null.
 * - Si [isValid] es false, [errorMessage] contiene el mensaje a mostrar.
 */
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

    // ===================================================================
    //  Placas de Nicaragua (particulares)
    // ===================================================================

    private val NICARAGUA_PLATE_CODES = listOf(
        "M", "BO", "CA", "CI", "CO", "ES", "GR", "JI", "LE",
        "MD", "MS", "MT", "NS", "RI", "SJ", "RACCN", "RACCS"
    )

    private val plateCodeAlternation = NICARAGUA_PLATE_CODES
        .sortedByDescending { it.length }
        .joinToString("|")

    private val nicaraguaPlateRegex = Regex(
        "^($plateCodeAlternation)[\\s-]?\\d{3}[\\s-]?\\d{2,3}$",
        RegexOption.IGNORE_CASE
    )

    private val plateSplitRegex = Regex("^([A-Z]+)(\\d+)$")

    fun normalizePlate(plate: String): String {
        return plate.uppercase().replace(Regex("[\\s-]"), "")
    }

    fun formatPlate(normalizedPlate: String): String {
        val match = plateSplitRegex.matchEntire(normalizedPlate.uppercase()) ?: return normalizedPlate
        val letters = match.groupValues[1]
        val digits = match.groupValues[2]
        return if (digits.length >= 5) {
            "$letters ${digits.substring(0, 3)} ${digits.substring(3)}"
        } else {
            "$letters $digits"
        }
    }

    fun validatePlate(
        plate: String,
        existingPlates: List<Pair<String, String>> = emptyList(),
        currentId: String? = null
    ): ValidationResult {
        val trimmed = plate.trim()
        if (trimmed.isBlank()) return ValidationResult.invalid("La placa es obligatoria")
        if (!nicaraguaPlateRegex.matches(trimmed)) {
            return ValidationResult.invalid("Formato inválido. Ej: M 123 456 o MT 12345")
        }
        val normalized = normalizePlate(trimmed)
        val duplicate = existingPlates.any { (id, p) -> p == normalized && id != currentId }
        if (duplicate) return ValidationResult.invalid("Esta placa ya está registrada")
        return ValidationResult.Valid
    }

    // ===================================================================
    //  Genéricos
    // ===================================================================

    fun validateRequired(value: String, fieldName: String): ValidationResult {
        return if (value.trim().isBlank()) {
            ValidationResult.invalid("$fieldName es obligatorio")
        } else ValidationResult.Valid
    }

    /**
     * Nombres y apellidos: solo letras (incluye acentos y ñ), espacios, guiones y apóstrofes.
     * Mínimo 2 caracteres útiles. Rechaza números y símbolos raros.
     */
    private val nameRegex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü '-]+$")

    fun validateName(value: String, fieldName: String): ValidationResult {
        val trimmed = value.trim()
        if (trimmed.isBlank()) return ValidationResult.invalid("$fieldName es obligatorio")
        if (trimmed.length < 2) return ValidationResult.invalid("$fieldName debe tener al menos 2 caracteres")
        if (!nameRegex.matches(trimmed)) {
            return ValidationResult.invalid("$fieldName solo acepta letras")
        }
        return ValidationResult.Valid
    }

    // ===================================================================
    //  Vehículo
    // ===================================================================

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

    fun validateOptionalMileage(mileageStr: String): ValidationResult {
        if (mileageStr.isBlank()) return ValidationResult.Valid
        val km = mileageStr.toLongOrNull() ?: return ValidationResult.invalid("Kilometraje inválido")
        if (km < 0) return ValidationResult.invalid("El kilometraje no puede ser negativo")
        return ValidationResult.Valid
    }

    /**
     * Valida que el tipo de combustible no esté vacío. Como ahora viene de un dropdown
     * con enum (FuelType), basta con verificar que se haya seleccionado uno.
     */
    fun validateFuelType(fuelType: String): ValidationResult {
        return if (fuelType.isBlank()) {
            ValidationResult.invalid("Debe seleccionar un tipo de combustible")
        } else ValidationResult.Valid
    }

    // ===================================================================
    //  Conductor
    // ===================================================================

    fun validateAge(ageStr: String): ValidationResult {
        if (ageStr.isBlank()) return ValidationResult.invalid("La edad es obligatoria")
        val age = ageStr.toIntOrNull() ?: return ValidationResult.invalid("Edad inválida")
        if (age < 18) return ValidationResult.invalid("El conductor debe tener al menos 18 años")
        if (age > 75) return ValidationResult.invalid("Edad máxima permitida: 75 años")
        return ValidationResult.Valid
    }

    fun normalizeIdCard(idCard: String): String {
        return idCard.trim().replace("-", "").uppercase()
    }

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
        val duplicate = existingIds.any { (id, c) ->
            normalizeIdCard(c) == normalized && id != currentId
        }
        if (duplicate) return ValidationResult.invalid("Esta cédula ya está registrada")
        return ValidationResult.Valid
    }

    fun normalizePhone(phone: String): String {
        return phone.filter { it.isDigit() }
    }

    fun validatePhone(
        phone: String,
        existingPhones: List<Pair<String, String>> = emptyList(),
        currentId: String? = null
    ): ValidationResult {
        val trimmed = phone.trim()
        if (trimmed.isBlank()) return ValidationResult.invalid("El teléfono es obligatorio")
        val digits = normalizePhone(trimmed)
        if (digits.length !in 8..15) {
            return ValidationResult.invalid("Teléfono debe tener entre 8 y 15 dígitos")
        }
        val duplicate = existingPhones.any { (id, p) ->
            normalizePhone(p) == digits && id != currentId
        }
        if (duplicate) return ValidationResult.invalid("Este teléfono ya está registrado")
        return ValidationResult.Valid
    }

    fun validateLicenseNumber(
        licenseNumber: String,
        existingLicenses: List<Pair<String, String>> = emptyList(),
        currentId: String? = null
    ): ValidationResult {
        val trimmed = licenseNumber.trim()
        if (trimmed.isBlank()) return ValidationResult.invalid("El número de licencia es obligatorio")
        if (trimmed.length < 5) {
            return ValidationResult.invalid("Número de licencia muy corto")
        }
        val normalized = trimmed.uppercase()
        val duplicate = existingLicenses.any { (id, l) ->
            l.trim().uppercase() == normalized && id != currentId
        }
        if (duplicate) return ValidationResult.invalid("Esta licencia ya está registrada a otro conductor")
        return ValidationResult.Valid
    }

    /**
     * Vencimiento de licencia: no puede estar vencida al registrar,
     * tampoco puede ser absurdamente lejana (máx 10 años a futuro).
     */
    fun validateLicenseExpiry(date: Date?): ValidationResult {
        if (date == null) return ValidationResult.invalid("La fecha de vencimiento es obligatoria")
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
        val maxAllowed = Calendar.getInstance().apply { add(Calendar.YEAR, 10) }.time
        if (date.before(today)) {
            return ValidationResult.invalid("La licencia no puede estar vencida al registrarse")
        }
        if (date.after(maxAllowed)) {
            return ValidationResult.invalid("Fecha demasiado lejana (máx. 10 años)")
        }
        return ValidationResult.Valid
    }

    // ===================================================================
    //  Mantenimiento
    // ===================================================================

    fun validateMaintenanceDates(startDate: Date?, completionDate: Date?): ValidationResult {
        if (startDate == null) return ValidationResult.invalid("Fecha de inicio obligatoria")
        if (completionDate != null && completionDate.before(startDate)) {
            return ValidationResult.invalid("Finalización no puede ser anterior al inicio")
        }
        return ValidationResult.Valid
    }

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

    /**
     * Descripción del mantenimiento: required + longitud mínima útil.
     */
    fun validateMaintenanceDescription(description: String): ValidationResult {
        val trimmed = description.trim()
        if (trimmed.isBlank()) return ValidationResult.invalid("La descripción es obligatoria")
        if (trimmed.length < 5) {
            return ValidationResult.invalid("Descripción muy corta (mín. 5 caracteres)")
        }
        return ValidationResult.Valid
    }

    /**
     * Próximo kilometraje del mantenimiento: opcional, pero si se ingresa
     * debe ser estrictamente mayor al actual del vehículo.
     */
    fun validateNextMileage(
        nextStr: String,
        currentVehicleMileage: Long
    ): ValidationResult {
        if (nextStr.isBlank()) return ValidationResult.Valid
        val km = nextStr.toLongOrNull() ?: return ValidationResult.invalid("Kilometraje inválido")
        if (km < 0) return ValidationResult.invalid("No puede ser negativo")
        if (km <= currentVehicleMileage) {
            return ValidationResult.invalid(
                "Debe ser > kilometraje actual ($currentVehicleMileage km)"
            )
        }
        return ValidationResult.Valid
    }

    /**
     * Valida que una fecha (si se ingresó) sea hoy o futura.
     * Pensada para campos opcionales como "Próxima fecha programada".
     */
    fun validateFutureDate(date: Date?, fieldName: String): ValidationResult {
        if (date == null) return ValidationResult.Valid
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
        if (date.before(today)) {
            return ValidationResult.invalid("$fieldName debe ser hoy o posterior")
        }
        return ValidationResult.Valid
    }

    // ===================================================================
    //  Asignación
    // ===================================================================

    /**
     * Fecha de salida de asignación: puede ser hoy o pre-asignar hasta 30 días.
     * No se permite registrar salidas pasadas (eso es un dato histórico, no una asignación).
     */
    fun validateDepartureDate(date: Date?): ValidationResult {
        if (date == null) return ValidationResult.invalid("Fecha de salida obligatoria")
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
        val maxFuture = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.time
        if (date.before(today)) {
            return ValidationResult.invalid("No se pueden registrar salidas en el pasado")
        }
        if (date.after(maxFuture)) {
            return ValidationResult.invalid("Solo se puede pre-asignar hasta 30 días a futuro")
        }
        return ValidationResult.Valid
    }

    fun validateAssignmentDates(departureDate: Date?, plannedReturnDate: Date?): ValidationResult {
        if (departureDate == null) return ValidationResult.invalid("Fecha de salida obligatoria")
        if (plannedReturnDate == null) return ValidationResult.invalid("Fecha de retorno obligatoria")
        if (plannedReturnDate.before(departureDate)) {
            return ValidationResult.invalid("Retorno no puede ser anterior a la salida")
        }
        return ValidationResult.Valid
    }

    /**
     * Kilometraje inicial de asignación: ≥ km registrado en el vehículo.
     * El conductor no puede "salir" con menos kilómetros de los que tiene el auto.
     */
    fun validateAssignmentInitialMileage(
        mileageStr: String,
        vehicleMileage: Long
    ): ValidationResult {
        val basic = validateMileage(mileageStr)
        if (!basic.isValid) return basic
        val km = mileageStr.toLongOrNull() ?: return basic
        if (km < vehicleMileage) {
            return ValidationResult.invalid(
                "Debe ser ≥ kilometraje del vehículo ($vehicleMileage km)"
            )
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