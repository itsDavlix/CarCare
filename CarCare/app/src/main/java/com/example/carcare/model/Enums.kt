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
    HEV("HEV"),
    ELECTRIC("Eléctrico");

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
    PENDING_ACCEPTANCE("Por aceptar"),
    ACTIVE("Activa"),
    COMPLETED("Finalizada"),
    REJECTED("Rechazada")
}

/**
 * Nivel de combustible al hacer check-out (aceptar) y check-in (entregar) la carrera.
 * Los nombres coinciden con el enum NivelCombustible del backend para serializar directo.
 */
enum class FuelLevel(val label: String) {
    VACIO("Vacío"),
    UN_CUARTO("¼ de tanque"),
    MEDIO("½ tanque"),
    TRES_CUARTOS("¾ de tanque"),
    LLENO("Lleno");

    companion object {
        fun fromName(name: String?): FuelLevel? =
            name?.let { runCatching { valueOf(it) }.getOrNull() }
    }
}