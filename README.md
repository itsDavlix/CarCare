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

```
app/src/main/java/com/example/carcare/
├── MainActivity.kt                  # Punto de entrada de la app
├── model/
│   └── Models.kt                    # Entidades: Vehicle, Driver, Maintenance, Assignment, FuelType
├── ui/
│   ├── components/
│   │   ├── CommonComponents.kt      # Componentes reutilizables (badges, etc.)
│   │   └── DatePickerField.kt       # DatePicker Material 3 con validación
│   ├── screens/
│   │   ├── SplashScreen.kt          # Pantalla inicial con animaciones
│   │   ├── LoginScreen.kt           # Selector de rol
│   │   ├── AdminScreen.kt           # Panel de administrador
│   │   └── DriverScreen.kt          # Panel de conductor
│   ├── theme/                       # Tema y tipografía Material 3
│   ├── utils/
│   │   └── Validators.kt            # Validaciones de formularios (placas NIC, fechas, etc.)
│   └── viewmodel/
│       ├── VehicleViewModel.kt
│       ├── DriverViewModel.kt
│       ├── MaintenanceViewModel.kt
│       └── AssignmentViewModel.kt
```

---

## 🚀 Cómo correr el proyecto

### Requisitos previos
- Android Studio (Hedgehog o más reciente)
- JDK 21
- Emulador Android API 24+ o dispositivo físico con depuración USB activada

### Pasos
1. Clonar el repositorio:
```bash
   git clone https://github.com/itsDavlix/CarCare.git
   cd CarCare
```
2. Abrir el proyecto en Android Studio.
3. Esperar a que Gradle sincronice automáticamente.
4. Seleccionar un dispositivo/emulador.
5. Presionar **Run** ▶️ o `Shift + F10`.

---

## 👥 Roles de la aplicación

### 🔧 Administrador
- Dashboard con estadísticas de flota
- CRUD de vehículos
- CRUD de conductores
- Registro de mantenimientos preventivos y correctivos
- Asignación de vehículos a conductores
- Recepción de vehículos (devolución con observaciones)
- Vista de alertas críticas

### 🚙 Conductor
- Visualización del vehículo asignado
- Reporte de problemas / mantenimientos *(en desarrollo)*

---

## 🌿 Flujo de trabajo Git

```
main         (entregas estables)
└── develop  (integración de features)
├── feature/<nombre>
├── fix/<nombre>
├── refactor/<nombre>
├── ui/<nombre>
├── docs/<nombre>
└── chore/<nombre>
```

### Convención de commits
Usamos **Conventional Commits**:

```
feat(scope): nueva funcionalidad
fix(scope): corrección de bug
refactor(scope): reorganización sin cambio de comportamiento
ui(scope): cambios visuales
docs(scope): documentación
chore(scope): configuración, build, dependencias
```

---

## 📝 Licencia

Proyecto académico desarrollado en el marco de la asignatura POO2 - Universidad Americana (UAM).  
Uso educativo únicamente.
