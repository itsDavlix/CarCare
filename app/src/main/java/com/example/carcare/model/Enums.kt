package com.example.carcare.model

enum class Role {
    ADMIN, DRIVER
}

enum class VehicleStatus(val label: String) {
    AVAILABLE("Disponible"),
    ASSIGNED("Asignado"),
    IN_USE("En uso"),
    PENDING_REVIEW("Pendiente de revisión"),
    MAINTENANCE("En mantenimiento"),
    OUT_OF_SERVICE("Fuera de servicio"),
    INACTIVE("Inactivo")
}

/**
 * Tipos de combustible disponibles.
 * Se serializa a String al guardar en Vehicle.fuelType para mantener
 * compatibilidad con datos existentes y futura persistencia.
 */
enum class FuelType(val label: String) {
    GASOLINE("Gasolina"),
    DIESEL("Diésel"),
    HEV("HEV");

    companion object {
        /** Busca un FuelType por su label. Retorna null si no se encuentra. */
        fun fromLabel(label: String): FuelType? =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) }
    }
}

enum class DriverStatus(val label: String) {
    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    SUSPENDED("Suspendido")
}

enum class MaintenanceStatus(val label: String) {
    PENDING("Pendiente"),
    IN_PROGRESS("En proceso"),
    COMPLETED("Completado")
}

enum class MaintenanceType(val label: String) {
    PREVENTIVE("Preventivo"),
    CORRECTIVE("Correctivo"),
    OIL_CHANGE("Cambio de aceite"),
    BRAKES("Revisión de frenos"),
    ENGINE("Revisión de motor"),
    TIRES("Cambio de llantas"),
    BATTERY("Cambio de batería"),
    ALIGNMENT("Alineación y balanceo"),
    GENERAL_REPAIR("Reparación general")
}

/**
 * Estado de una asignación. Ahora con [label] para mantener la consistencia con
 * el resto de los enums (badges, dashboard) en lugar de un if (==ACTIVE) suelto.
 */
enum class AssignmentStatus(val label: String) {
    ACTIVE("Activa"),
    COMPLETED("Finalizada")
}