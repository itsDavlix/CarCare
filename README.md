<p align="center">
  <img src="carcare-logo.png" alt="carcare-logo" width="220"/>
</p>

# 🚗 CarCare — Sistema de Gestión Integral de Flotas Vehiculares

Aplicación móvil Android nativa que centraliza la administración de **vehículos, conductores, asignaciones y mantenimientos**, con operación offline, autenticación segura y actualización en tiempo real.

Desarrollada con **Kotlin**, **Jetpack Compose** y arquitectura **MVVM + Repository**. Se conecta a una API REST propia construida con **Spring Boot**, almacena los datos en **PostgreSQL (Neon)** y utiliza **Room** como caché local.

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin 2.2.10"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Arquitectura-MVVM%20%2B%20Repository-FF6F00" alt="MVVM + Repository"/>
  <img src="https://img.shields.io/badge/API-Spring%20Boot%20Java%2021-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot Java 21"/>
  <img src="https://img.shields.io/badge/Base%20de%20Datos-PostgreSQL%20%7C%20Neon-336791?logo=postgresql&logoColor=white" alt="PostgreSQL Neon"/>
  <img src="https://img.shields.io/badge/Tiempo%20Real-SSE-00A896" alt="Server-Sent Events"/>
  <img src="https://img.shields.io/badge/Estado-Completado-success" alt="Estado Completado"/>
  <img src="https://img.shields.io/badge/UAM-Proyecto%20Acad%C3%A9mico-00BCD4" alt="UAM Proyecto Académico"/>
</p>

---

## 📝 Sobre el proyecto

**CarCare** es una plataforma móvil para la gestión integral de flotas vehiculares. Su propósito es sustituir registros en papel, hojas de cálculo y procesos desconectados por una solución única que permita conocer el estado de la operación y mantener la trazabilidad de cada vehículo.

La aplicación conecta a **administradores** y **conductores** mediante una API REST propia. Los cambios relevantes se comunican en tiempo real usando **Server-Sent Events (SSE)**, mientras que **Room** conserva una copia local de los datos para que la información principal pueda consultarse cuando la conexión no está disponible.

### Problemas que resuelve

- Falta de un control centralizado sobre vehículos, conductores y mantenimientos.
- Poca visibilidad sobre la ubicación operativa y el estado de cada unidad.
- Asignaciones manuales realizadas mediante llamadas, mensajes o documentos físicos.
- Mantenimientos atrasados por falta de alertas y seguimiento.
- Ausencia de evidencia sobre kilometraje, combustible y condición del vehículo.
- Dificultad para consultar historiales y generar reportes de la flota.

### Propuesta de valor

- **Gestión centralizada:** vehículos, conductores, asignaciones y mantenimientos desde una sola aplicación.
- **Actualización en tiempo real:** los cambios se reflejan sin necesidad de recargar manualmente las pantallas.
- **Funcionamiento offline-first:** la aplicación consulta la caché local cuando la API no está disponible y actualiza los datos al recuperar la conexión.
- **Seguridad basada en JWT:** sesión persistente, control por roles y cierre automático por inactividad.
- **Trazabilidad completa:** registro del estado inicial y final de cada vehículo, incluyendo fotografías.
- **Operaciones consistentes:** el backend coordina los cambios de estado mediante transacciones atómicas.

---

## 👥 Perfiles del sistema

### 🛡️ Administrador

El administrador tiene una visión global de la flota y puede:

- Consultar un dashboard con estadísticas, alertas y notificaciones en tiempo real.
- Registrar, editar, consultar y eliminar vehículos.
- Gestionar los **7 estados** de un vehículo:
  - Disponible.
  - Asignado.
  - En uso.
  - Pendiente de revisión.
  - En mantenimiento.
  - Fuera de servicio.
  - Inactivo.
- Registrar y administrar conductores.
- Suspender conductores; el backend libera automáticamente el vehículo relacionado cuando corresponde.
- Crear, editar, completar y eliminar asignaciones.
- Consultar asignaciones pendientes, activas, completadas y rechazadas.
- Revisar las fotografías iniciales y finales del nivel de combustible.
- Registrar y dar seguimiento a mantenimientos.
- Exportar información de vehículos, conductores y asignaciones en formato **CSV**.
- Restablecer credenciales y administrar la seguridad de los usuarios.

### 🚘 Conductor

El conductor dispone de un flujo simplificado para:

- Recibir nuevas asignaciones en tiempo real mediante SSE.
- Consultar los datos del vehículo asignado.
- Aceptar una asignación registrando:
  - Kilometraje inicial.
  - Nivel de combustible.
  - Condición general del vehículo.
  - Observaciones.
  - Fotografía opcional como evidencia.
- Rechazar una asignación indicando un motivo escrito.
- Consultar una carrera activa y su fecha prevista de retorno.
- Recibir una alerta visual cuando la devolución se encuentra vencida.
- Entregar el vehículo registrando kilometraje, combustible, condición, observaciones y fotografía final.
- Reportar el vehículo a mantenimiento durante el proceso de devolución.
- Consultar notificaciones y el historial relacionado con sus asignaciones.

---

## ✨ Funcionalidades principales

### 📊 Dashboard

- Resumen de vehículos activos y disponibles.
- Asignaciones del día y carreras en curso.
- Alertas de mantenimientos pendientes.
- Avisos de seguros, circulación y licencias próximos a vencer.
- Distribución de vehículos por estado.
- Actividad reciente y estadísticas de uso.
- Notificaciones recibidas en tiempo real.

### 🚙 Gestión de vehículos

- CRUD completo de vehículos.
- Registro de placa, marca, modelo, año, color y tipo de combustible.
- Números de chasis y motor únicos.
- Control de kilometraje y estado operativo.
- Datos de póliza, circulación y fechas de vencimiento.
- Fotografías del vehículo y de sus documentos.
- Búsqueda por marca, modelo o placa.
- Actualización automática del estado al completar asignaciones o mantenimientos.

### 👤 Gestión de conductores

- Alta, edición, consulta y baja de conductores.
- Registro de cédula, teléfono, edad, licencia y fecha de vencimiento.
- Fotografías de perfil y licencia.
- Estados activo, inactivo y suspendido.
- Detección de licencias vencidas o próximas a vencer.
- Generación y restablecimiento de credenciales.
- Liberación automática de asignaciones relacionadas al suspender un conductor.

### 📋 Gestión de asignaciones

- Creación de asignaciones entre un vehículo disponible y un conductor activo.
- Fecha de salida, fecha prevista de retorno y kilometraje estimado.
- Flujo completo: **crear → aceptar/rechazar → en uso → completar**.
- Comparación entre kilometraje, combustible y condición inicial y final.
- Evidencia fotográfica de salida y retorno.
- Detección de devoluciones atrasadas.
- Historial filtrable con los datos relevantes de cada operación.

### 🔧 Gestión de mantenimientos

- Mantenimientos preventivos y correctivos.
- Tipos especializados: aceite, frenos, motor, llantas, batería, alineación y reparación general.
- Flujo de estados: **PENDING → IN_PROGRESS → COMPLETED**.
- Registro de fecha, kilometraje, descripción y responsable.
- Programación del siguiente mantenimiento por fecha o kilometraje.
- Historial general y por vehículo.
- Cambio automático del vehículo a disponible cuando el mantenimiento finaliza y las reglas de negocio lo permiten.

### 📤 Exportación CSV

- Inventario de vehículos.
- Registro de conductores.
- Historial de asignaciones.
- Historial de mantenimientos.
- Información filtrable para auditoría y análisis externo.

### 🔔 Notificaciones en tiempo real

- Canal SSE para administradores y conductores.
- Actualización automática de pantallas sin recarga manual.
- Avisos por asignaciones creadas, aceptadas, rechazadas o completadas.
- Notificaciones por mantenimiento y seguridad.
- Reconexión automática cuando se interrumpe el canal de eventos.
- Marcado individual o general de notificaciones como leídas.

---

## 🔄 Flujo funcional de principio a fin

1. **El administrador crea la asignación.** Selecciona un vehículo disponible, un conductor activo y define la fecha prevista de retorno y los kilómetros estimados.
2. **El conductor recibe la asignación.** SSE notifica el cambio y actualiza la pantalla sin recargarla.
3. **El conductor acepta o rechaza.** Al aceptar, registra kilometraje inicial, combustible, condición y una fotografía opcional. Al rechazar, escribe el motivo.
4. **La carrera queda activa.** El conductor consulta la fecha de retorno y recibe una alerta si la entrega está atrasada.
5. **El conductor entrega el vehículo.** Registra kilometraje final, combustible, condición, observaciones y fotografía; también puede solicitar mantenimiento.
6. **El backend procesa la operación.** Una transacción actualiza la asignación, el vehículo, el kilometraje y los estados relacionados.
7. **El administrador consulta el resultado.** El dashboard, las listas y las notificaciones se actualizan en tiempo real y la información queda disponible para exportación.

---

## 🏗️ Arquitectura técnica

CarCare utiliza el patrón **MVVM + Repository**, separando la interfaz, el estado de presentación y el acceso a datos.

```text
┌──────────────────────────────────────────────┐
│                  UI Layer                    │
│        Jetpack Compose · Material 3          │
│            Navigation Compose                │
└──────────────────────┬───────────────────────┘
                       │
┌──────────────────────▼───────────────────────┐
│                  ViewModels                  │
│       Estado reactivo · Validaciones         │
│          Lógica de presentación              │
└──────────────────────┬───────────────────────┘
                       │
┌──────────────────────▼───────────────────────┐
│                  Repositories                │
│      Fuente única de verdad: Room + API      │
└───────────────┬──────────────────┬───────────┘
                │                  │
┌───────────────▼────────────┐  ┌──▼────────────────────┐
│ Retrofit · OkHttp · Moshi  │  │ Room · DataStore      │
│ API REST · JWT · SSE       │  │ Caché · Sesión        │
└───────────────┬────────────┘  └───────────────────────┘
                │
┌───────────────▼───────────────────────────────────────┐
│ Spring Boot Java 21 · PostgreSQL Neon · Render       │
└───────────────────────────────────────────────────────┘
```

### Componentes principales

1. **Capa de presentación**
   - Pantallas declarativas con Jetpack Compose.
   - Componentes reutilizables basados en Material 3.
   - ViewModels con estado reactivo.
   - Navegación diferenciada por rol.

2. **Capa de datos**
   - Retrofit y OkHttp para consumir la API REST.
   - Moshi para serialización y deserialización.
   - Room como caché local tipada.
   - DataStore para preferencias y sesión.
   - Repositorios como fuente única de verdad.

3. **Backend**
   - API REST desarrollada con Spring Boot y Java 21.
   - Autenticación y autorización mediante JWT.
   - Operaciones transaccionales para mantener consistencia.
   - Server-Sent Events para actualizaciones en tiempo real.
   - PostgreSQL alojado en Neon.
   - Despliegue en Render.

---

## 🛠️ Stack tecnológico

| Área | Tecnología |
|---|---|
| **Aplicación móvil** | Android nativo |
| **Lenguaje Android** | Kotlin 2.2.10 |
| **Interfaz** | Jetpack Compose + Material 3 |
| **Arquitectura** | MVVM + Repository |
| **Inyección de dependencias** | Hilt 2.59.2 |
| **Navegación** | Navigation Compose |
| **Cliente HTTP** | Retrofit 2.11.0 + OkHttp 4.12.0 |
| **Serialización** | Moshi 1.15.1 |
| **Tiempo real** | Server-Sent Events con OkHttp SSE |
| **Caché local** | Room 2.8.4 (SQLite) |
| **Sesión y preferencias** | DataStore Preferences 1.1.1 |
| **Imágenes** | Coil Compose 2.7.0 + Base64 |
| **Concurrencia** | Kotlin Coroutines 1.9.0 |
| **Backend** | Spring Boot + Java 21 |
| **Base de datos remota** | PostgreSQL en Neon |
| **Despliegue backend** | Render |
| **CI** | GitHub Actions |
| **Build Android** | Gradle 9.4.1 con Kotlin DSL |
| **Android mínimo** | API 24 — Android 7.0 |
| **Android objetivo** | API 36 |

---

## 🔒 Seguridad, calidad y robustez

### Autenticación y sesión

- Inicio de sesión con cédula y contraseña.
- Autorización según los roles `ADMIN` y `DRIVER`.
- Tokens JWT con una vigencia configurada de 12 horas.
- Sesión persistente mediante DataStore.
- Auto-login al volver a abrir la aplicación mientras la sesión es válida.
- Cierre automático tras 30 minutos de inactividad.
- Cambio obligatorio de contraseña en el primer ingreso cuando aplica.

### Estrategia offline-first

- Room conserva vehículos, conductores, asignaciones y mantenimientos.
- La información almacenada puede consultarse sin conexión.
- Los repositorios actualizan la caché al obtener datos de la API.
- La aplicación recupera la información remota al restablecerse la conectividad.
- Las operaciones que modifican datos requieren confirmación del backend.

### Pruebas y monitoreo

- Pruebas unitarias para ViewModels, validaciones y lógica de presentación.
- GitHub Actions ejecuta la suite de pruebas en los pull requests.
- Un pipeline exitoso es requisito para integrar cambios en `develop`.
- El canal SSE implementa reconexión automática.
- UptimeRobot supervisa periódicamente la disponibilidad de la API.

---

## 📂 Estructura del proyecto

```text
app/src/main/java/com/example/carcare/
├── CarCareApp.kt
├── MainActivity.kt
├── data/
│   ├── AuthSession.kt
│   ├── SessionEvents.kt
│   ├── SessionStore.kt
│   ├── local/
│   │   ├── CarCareDatabase.kt
│   │   ├── DatabaseModule.kt
│   │   ├── *Dao.kt
│   │   └── *Entity.kt
│   ├── network/
│   │   ├── ApiClient.kt
│   │   ├── SseClient.kt
│   │   ├── api/
│   │   └── dto/
│   └── repository/
├── model/
│   ├── Vehicle.kt
│   ├── Driver.kt
│   ├── Maintenance.kt
│   ├── Assignment.kt
│   ├── Notificacion.kt
│   └── Enums.kt
├── ui/
│   ├── components/
│   ├── screens/
│   │   ├── AuthScreen.kt
│   │   ├── ForceChangePasswordScreen.kt
│   │   ├── AdminScreen.kt
│   │   ├── DriverScreen.kt
│   │   └── admin/
│   ├── theme/
│   └── viewmodel/
└── util/
    ├── CsvExporter.kt
    └── Validators.kt
```

---

## 🚀 Instalación y ejecución

### Descargar el APK

La versión presentada del APK puede descargarse desde Google Drive:

[**Descargar CarCare para Android**](https://drive.google.com/file/d/14kEFNz6Rf3ucM1WL66zriWDEsdRWC2BV/view?usp=sharing)

Para instalarlo manualmente:

1. Descargar el archivo `.apk` en un dispositivo Android.
2. Autorizar la instalación de aplicaciones desconocidas para el navegador o gestor de archivos utilizado.
3. Abrir el APK descargado.
4. Confirmar la instalación de CarCare.

### Ejecutar desde el código fuente

#### Requisitos

- Android Studio compatible con las versiones de Gradle y Android Gradle Plugin del proyecto.
- JDK 21.
- Android SDK 36.
- Emulador o dispositivo físico con Android 7.0 o superior.
- Conexión a internet para sincronizar dependencias y utilizar las operaciones remotas.

#### Pasos

```bash
git clone https://github.com/itsDavlix/CarCare.git
cd CarCare
```

1. Abrir el proyecto en Android Studio.
2. Esperar la sincronización de Gradle.
3. Seleccionar un emulador o dispositivo.
4. Ejecutar con **Run** o `Shift + F10`.

Compilación desde la terminal:

```bash
# Linux o macOS
./gradlew assembleDebug

# Windows
.\gradlew.bat assembleDebug
```

El APK de depuración se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔑 Credenciales de demostración

Utiliza las siguientes credenciales para probar los perfiles principales de la aplicación:

| Perfil | Usuario / Cédula | Contraseña |
|---|---|---|
| **Administrador** | `0010802061014S` | `Admin123` |
| **Conductor** | `0012508051002L` | `1234567890` |

> [!IMPORTANT]
> Estas credenciales son únicamente para demostración y pruebas académicas. No deben utilizarse en un entorno de producción.

---

## 🌐 Backend y repositorios

| Componente | Repositorio / servicio |
|---|---|
| **Aplicación Android** | [github.com/itsDavlix/CarCare](https://github.com/itsDavlix/CarCare) |
| **API CarCare** | [github.com/MG-Chamorro/Api---CarCare](https://github.com/MG-Chamorro/Api---CarCare) |
| **API desplegada** | `https://api-carcare.onrender.com/` |
| **Base de datos** | PostgreSQL en Neon |

La URL base de la aplicación se encuentra en:

```text
app/src/main/java/com/example/carcare/data/network/ApiClient.kt
```

```kotlin
const val BASE_URL = "https://api-carcare.onrender.com/"
```

El cliente ejecuta una solicitud de calentamiento al endpoint `/api/health` para reducir el impacto del arranque en frío del servicio desplegado en Render.

---

## 🧪 Pruebas

```bash
# Pruebas unitarias
./gradlew test

# Pruebas instrumentadas con emulador o dispositivo activo
./gradlew connectedAndroidTest
```

---

## 🌿 Flujo de trabajo Git

```text
main         Entregas estables
└── develop  Integración continua
    ├── feature/<nombre>
    ├── fix/<nombre>
    ├── refactor/<nombre>
    ├── perf/<nombre>
    ├── ui/<nombre>
    ├── docs/<nombre>
    └── chore/<nombre>
```

El proyecto utiliza **Conventional Commits**:

```text
feat: nueva funcionalidad
fix: corrección de un error
refactor: reorganización sin cambiar el comportamiento
perf: mejora de rendimiento
ui: cambio visual o de experiencia
build: cambios de compilación o dependencias
docs: actualización de documentación
test: creación o modificación de pruebas
chore: tareas de mantenimiento
```

Ejemplos:

```text
feat: add CSV export for vehicles
fix: rollback on assignment completion error
refactor: extract BaseListViewModel
chore: update Hilt to 2.59.2
```

---

## 🎯 Usuarios y sectores beneficiados

CarCare puede adaptarse a organizaciones que operan vehículos propios o asignados:

- Empresas de logística, delivery, construcción y agricultura.
- Cooperativas de taxis, microbuses, transporte escolar y turismo.
- Hospitales, alcaldías, ministerios e instituciones públicas.
- Universidades e institutos con vehículos para actividades administrativas o de campo.

---

## 🗺️ Proyección y escalabilidad

### Mejoras funcionales

- Rutas y geolocalización en tiempo real.
- Análisis de consumo de combustible por vehículo y período.
- Reportes avanzados con filtros y gráficas.
- Notificaciones push mediante Firebase Cloud Messaging.
- Inspecciones visuales más completas con la cámara del dispositivo.

### Evolución técnica

- Kotlin Multiplatform para extender la solución a iOS.
- Pruebas de integración y pruebas end-to-end.
- Despliegue continuo automatizado.
- Métricas y observabilidad avanzada del backend.
- OAuth 2.0 y autenticación biométrica.

### Visión de producto

- Plataforma SaaS multiempresa y multi-tenant.
- Dashboard web administrativo.
- Predicción de mantenimientos mediante inteligencia artificial.
- Integración con ERP y otros sistemas empresariales.

---

## 🧑‍💻 Equipo de desarrollo

| Integrante | Rol |
|---|---|
| **David Alejandro Espinoza Largaespada** | Coordinador |
| **Manuel Joaquín Chamorro Gómez** | Desarrollador Backend |
| **Valeria Carolina Grijalva Arévalo** | Diseño y Visuales |

**Docente:** M.Sc. José Durán García  
**Asignatura:** Programación Orientada a Objetos 2  
**Año de desarrollo:** 2026  
**Institución:** Universidad Americana (UAM)

---

## 📝 Uso académico

CarCare fue desarrollado como proyecto académico y demostración de una arquitectura móvil moderna, con separación por capas, sincronización en tiempo real, persistencia local, backend propio y trabajo colaborativo mediante GitHub.
