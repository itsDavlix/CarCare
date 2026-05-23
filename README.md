# 🚗 CarCare

**App Android nativa para la gestión inteligente de flotas vehiculares.**

Proyecto académico desarrollado para la asignatura de **Programación Orientada a Objetos 2 (POO2)**.

---

## 📋 Descripción

CarCare es una aplicación móvil que permite a administradores de flotas y conductores gestionar el ciclo de vida completo de vehículos:

- Registro y control de vehículos
- Administración de conductores y sus licencias
- Programación y seguimiento de mantenimientos
- Asignación de vehículos a conductores
- Alertas críticas (mantenimientos vencidos, licencias por expirar)
- Dashboard con estadísticas en tiempo real

---

## 🛠️ Stack Tecnológico

| Categoría | Tecnología |
|---|---|
| **Lenguaje** | Kotlin |
| **UI** | Jetpack Compose |
| **Diseño** | Material 3 |
| **Arquitectura** | MVVM |
| **Estado** | `mutableStateListOf` (Compose State) |
| **ViewModels** | `androidx.lifecycle.ViewModel` |
| **Build** | Gradle (Kotlin DSL) |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 |

> **Nota:** Por ahora los datos viven en memoria. La persistencia con Room está en el roadmap.

---

## 📂 Estructura del proyecto
