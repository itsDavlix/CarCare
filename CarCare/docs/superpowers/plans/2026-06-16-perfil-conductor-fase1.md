# Perfil del conductor (Fase 1) — Plan de implementación

> **Para workers agénticos:** ejecutar tarea por tarea. Pasos con checkbox `- [ ]`.

**Goal:** Dar al conductor dos pantallas (Inicio / Perfil) con barra inferior, y permitirle cambiar su propia contraseña desde el perfil, usando el endpoint self-service que ya existe en el backend.

**Architecture:** `DriverScreen` pasa a ser un *shell* (`Scaffold` + `NavigationBar` + `AnimatedContent`) con dos secciones internas, igual que `AdminScreen`. El cambio de contraseña va por `AuthRepository`/`AuthViewModel` (capa de auth ya existente) contra `POST /api/auth/cambiar-password`.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Retrofit/Moshi. Sin backend nuevo (el endpoint ya existe).

**Verificación:** el proyecto no tiene infra de tests de UI Compose; se verifica con `:app:compileDebugKotlin` + `:app:assembleDebug`, más un unit test de `Validators.validatePassword`. Rama: `claude/perfil-conductor` desde `origin/develop`.

---

### Task 0: Rama
- [ ] `git fetch origin && git checkout -b claude/perfil-conductor origin/develop` (en el worktree).

### Task 1: DTO + endpoint Retrofit
**Files:** Create `app/src/main/java/com/example/carcare/data/network/dto/CambioPasswordDto.kt`; Modify `app/src/main/java/com/example/carcare/data/network/api/AuthApiService.kt`.

- [ ] Crear el DTO (coincide con `CambioPasswordDTO` del backend):
```kotlin
package com.example.carcare.data.network.dto

/** Cuerpo de POST /api/auth/cambiar-password (auto-servicio del conductor). */
data class CambioPasswordDto(
    val cedula: String,
    val passwordActual: String,
    val passwordNueva: String
)
```
- [ ] Agregar a `AuthApiService`:
```kotlin
import com.example.carcare.data.network.dto.CambioPasswordDto
import retrofit2.Response
// ...
@POST("api/auth/cambiar-password")
suspend fun cambiarPassword(@Body dto: CambioPasswordDto): Response<Unit>
```

### Task 2: AuthRepository.changePassword
**Files:** Modify `app/src/main/java/com/example/carcare/data/repository/AuthRepository.kt`.

- [ ] Agregar:
```kotlin
import com.example.carcare.data.network.dto.CambioPasswordDto
// ...
suspend fun changePassword(cedula: String, actual: String, nueva: String) {
    api.cambiarPassword(CambioPasswordDto(cedula = cedula, passwordActual = actual, passwordNueva = nueva))
}
```

### Task 3: AuthViewModel.changePassword (con callbacks, sin tocar el estado del login)
**Files:** Modify `app/src/main/java/com/example/carcare/ui/viewmodel/AuthViewModel.kt`.

- [ ] Agregar:
```kotlin
fun changePassword(
    cedula: String,
    actual: String,
    nueva: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    viewModelScope.launch {
        try {
            repository.changePassword(cedula, actual, nueva)
            onSuccess()
        } catch (e: Exception) {
            onError(e.toUserMessage())
        }
    }
}
```

### Task 4: Validators — unit test de validatePassword
**Files:** Create `app/src/test/java/com/example/carcare/ValidatorsPasswordTest.kt`.
(`Validators.validatePassword` ya existe; este test fija su contrato.)

- [ ] Test:
```kotlin
package com.example.carcare
import com.example.carcare.util.Validators
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsPasswordTest {
    @Test fun corta_esInvalida() { assertFalse(Validators.validatePassword("123", "123").isValid) }
    @Test fun noCoinciden_esInvalida() { assertFalse(Validators.validatePassword("secreta1", "secreta2").isValid) }
    @Test fun validaYCoincide_esValida() { assertTrue(Validators.validatePassword("secreta1", "secreta1").isValid) }
}
```
- [ ] Run: `gradlew :app:testDebugUnitTest --tests "*ValidatorsPasswordTest*"` → PASS.

### Task 5: ChangeOwnPasswordDialog
**Files:** Modify `app/src/main/java/com/example/carcare/ui/screens/DriverScreen.kt` (agregar composable).

- [ ] Diálogo self-service (actual + nueva + confirmar, mostrar/ocultar, `Validators.validatePassword`):
```kotlin
@Composable
private fun ChangeOwnPasswordDialog(onDismiss: () -> Unit, onConfirm: (actual: String, nueva: String) -> Unit) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    val validation = Validators.validatePassword(nueva, confirm)
    val transform = if (visible) VisualTransformation.None else PasswordVisualTransformation()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar contraseña") },
        text = {
            Column {
                OutlinedTextField(actual, { actual = it }, label = { Text("Contraseña actual") }, singleLine = true,
                    visualTransformation = transform, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = attempted && actual.isBlank(), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(nueva, { nueva = it }, label = { Text("Nueva contraseña") }, singleLine = true,
                    visualTransformation = transform, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = attempted && !validation.isValid, modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { IconButton(onClick = { visible = !visible }) {
                        Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(confirm, { confirm = it }, label = { Text("Confirmar contraseña") }, singleLine = true,
                    visualTransformation = transform, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = attempted && !validation.isValid, modifier = Modifier.fillMaxWidth(),
                    supportingText = if (attempted && !validation.isValid) { { Text(validation.errorMessage ?: "") } } else null)
            }
        },
        confirmButton = { TextButton(onClick = {
            attempted = true
            if (actual.isNotBlank() && validation.isValid) onConfirm(actual, nueva)
        }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
```
Imports nuevos en DriverScreen: `KeyboardType`, `PasswordVisualTransformation`, `VisualTransformation`, iconos `Visibility/VisibilityOff`, `mutableStateOf`/`remember` (ya presentes).

### Task 6: DriverProfileSection
**Files:** Modify `DriverScreen.kt`.

- [ ] Sección de perfil (reusa `KeyValueRow`/`DialogSectionLabel` de `admin` — o réplica local si no se quiere dependencia cruzada de paquete; usar `StatusBadge` de components):
```kotlin
@Composable
private fun DriverProfileSection(
    driver: Driver,
    assignedVehicle: Vehicle?,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(driver.status.statusColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, tint = driver.status.statusColor, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column { Text(driver.fullName, style = MaterialTheme.typography.headlineSmall); StatusBadge(status = driver.status) }
        }
        Spacer(Modifier.height(16.dp))
        ProfileRow("Cédula", driver.idCardNumber)
        ProfileRow("Teléfono", driver.phone)
        ProfileRow("Edad", "${driver.age} años")
        ProfileRow("Licencia vence", sdf.format(driver.licenseExpiryDate))
        ProfileRow("Vehículo asignado", assignedVehicle?.let { "${it.brand} ${it.model} (${Validators.formatPlate(it.plate)})" } ?: "Ninguno")
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onChangePassword, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Cambiar contraseña")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Cerrar sesión")
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(140.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}
```

### Task 7: DriverScreen → shell con barra inferior + extraer DriverHomeSection
**Files:** Modify `DriverScreen.kt`, `MainActivity.kt`.

- [ ] Agregar `authViewModel: AuthViewModel` al parámetro de `DriverScreen` y pasarlo desde `MainActivity` (ya existe `authViewModel` activity-scoped):
```kotlin
// MainActivity, dentro del composable Routes.DRIVER:
DriverScreen(driverIdCard = driverIdCard, onBack = logout,
    vehicleViewModel = vehicleViewModel, driverViewModel = driverViewModel,
    assignmentViewModel = assignmentViewModel, maintenanceViewModel = maintenanceViewModel,
    authViewModel = authViewModel)
```
- [ ] Mover el contenido actual del `else ->` (saludo + vehículo asignado + botón reportar) y los estados de carga/error/empty a un `DriverHomeSection(...)` privado.
- [ ] `DriverScreen` queda como shell:
```kotlin
var selectedTab by remember { mutableIntStateOf(0) }
var showChangePw by remember { mutableStateOf(false) }
var pwFeedback by remember { mutableStateOf<String?>(null) } // snackbar
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()
Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = { CarCareTopBar(onAvatarClick = { selectedTab = 1 }, avatarLetter = driver?.firstName?.firstOrNull()?.uppercase() ?: "C", subtitle = "Mi panel · Conductor") },
    bottomBar = { NavigationBar {
        NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Inicio") })
        NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Perfil") })
    } }
) { padding ->
    Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
        AnimatedContent(selectedTab, transitionSpec = { fadeIn(tween(170)) togetherWith fadeOut(tween(90)) }, label = "driverTab") { tab ->
            when (tab) {
                0 -> DriverHomeSection(/* mismos parámetros que hoy */)
                else -> if (driver != null) DriverProfileSection(driver, assignedVehicle, onChangePassword = { showChangePw = true }, onLogout = onBack)
                        else DriverHomeSection(/* fallback carga */)
            }
        }
    }
}
if (showChangePw && driver != null) {
    ChangeOwnPasswordDialog(
        onDismiss = { showChangePw = false },
        onConfirm = { actual, nueva ->
            authViewModel.changePassword(
                cedula = driver.idCardNumber, actual = actual, nueva = nueva,
                onSuccess = { showChangePw = false; scope.launch { snackbarHostState.showSnackbar("Contraseña actualizada") } },
                onError = { msg -> showChangePw = false; scope.launch { snackbarHostState.showSnackbar(msg) } }
            )
        }
    )
}
```
Imports nuevos: `AnimatedContent`, `fadeIn/fadeOut/togetherWith`, `tween`, `mutableIntStateOf`, `rememberCoroutineScope`, `kotlinx.coroutines.launch`, iconos `Home`/`Person`.

### Task 8: Verificar
- [ ] `gradlew :app:compileDebugKotlin` → EXIT 0.
- [ ] `gradlew :app:testDebugUnitTest --tests "*ValidatorsPasswordTest*"` → PASS.
- [ ] `gradlew :app:assembleDebug` → EXIT 0.

### Task 9: Commit + PR
- [ ] Commit en `claude/perfil-conductor`, push, PR a `develop`.

---

## Self-review
- **Cobertura del spec (Fase 1):** navegación 2 pestañas (Task 7) ✓, perfil (Task 6) ✓, cambio de contraseña self-service (Tasks 1-5, 7) ✓, logout movido al perfil (Task 6/7) ✓. El aviso al admin queda para Fase 2 (documentado).
- **Placeholders:** ninguno; cada paso trae código real.
- **Consistencia de tipos:** `CambioPasswordDto(cedula, passwordActual, passwordNueva)` == DTO del backend; `changePassword(cedula, actual, nueva)` consistente repo→VM→UI.
