# Notificaciones y perfil del conductor — Diseño

- **Fecha:** 2026-06-13
- **Estado:** propuesta para revisión
- **Repos:** app Android (`itsDavlix/CarCare`) + backend (`MG-Chamorro/Api---CarCare`)
- **Decisiones tomadas:** notificaciones con **backend completo** · navegación del conductor con **barra inferior de 2 pestañas** · entrega **por fases** · notificaciones **in-app** (no push del SO).

## Objetivo

1. Panel de notificaciones en el dashboard del admin (entre "Resumen de flota" y "Estadísticas").
2. Sistema de notificaciones: el admin recibe avisos de actividad (acciones de admin y de conductores); el conductor recibe avisos básicos (su asignación, mantenimiento de su vehículo).
3. Pantalla de perfil del conductor (segunda pantalla) con cambio de contraseña self-service.
4. El cambio de contraseña del conductor genera una notificación al admin.

---

## Fase 1 — Perfil del conductor (app + endpoint existente)

### Navegación
`DriverScreen` pasa a ser un *shell*: `Scaffold` con `NavigationBar` inferior de 2 pestañas (**Inicio / Perfil**) y `AnimatedContent` con crossfade (170 ms in / 90 ms out), replicando el patrón de `AdminScreen`. La ruta `driver/{cedula}` del `NavHost` no cambia; las dos pantallas son secciones internas. El avatar de la TopBar deja de hacer logout y pasa a abrir la pestaña Perfil.

### Componentes
- `DriverHomeSection`: contenido actual (saludo, vehículo asignado, reportar mantenimiento). En Fase 2 se le agrega la sección de notificaciones del conductor.
- `DriverProfileSection` (nuevo): cabecera (avatar tintado por estado + nombre + `StatusBadge`) → `KeyValueRow`s (cédula, teléfono, edad, licencia vence, vehículo asignado) → sección "Acceso" (usuario = cédula + botón **Cambiar contraseña**) → botón **Cerrar sesión**.
- `ChangeOwnPasswordDialog` (nuevo): contraseña actual + nueva + confirmar, con mostrar/ocultar y `Validators.validatePassword`.

### Cambio de contraseña self-service
Usa el endpoint existente `POST /api/auth/cambiar-password` (cédula + contraseña actual + nueva). Capas nuevas en la app: `AuthApiService.cambiarPassword`, `AuthRepository.changePassword`, método en ViewModel con callbacks éxito/error.

### Flujo de datos
ViewModels siguen con alcance de Activity (compartidos). El perfil lee del `DriverViewModel`/`AssignmentViewModel` ya cargados; el cambio de contraseña va por el `AuthRepository`.

### Errores
Contraseña actual incorrecta → mensaje del backend por snackbar. Validación local (mín. 6, coincidencia) antes de llamar. Sin sesión activa: no aplica (el conductor está logueado).

### Pruebas
Validación de contraseña (unit). Verificación por compilación (`compileDebugKotlin` + `assembleDebug`).

### Archivos
App: `MainActivity` (sin cambios de ruta), `DriverScreen` (refactor a shell + 2 secciones), `DriverProfileSection` (nuevo), `AuthApiService`, `AuthRepository`, `AuthViewModel`/DTO, `ChangeOwnPasswordDialog`.

> El aviso al admin de este cambio se engancha en Fase 2 (cuando exista el sistema de notificaciones).

---

## Fase 2 — Sistema de notificaciones (backend completo + paneles)

### Modelo de datos (backend)
Entidad `Notificacion`:
- `id`, `tipo` (enum `TipoNotificacion`: VEHICULO, CONDUCTOR, MANTENIMIENTO, ASIGNACION, SEGURIDAD)
- `mensaje` (String)
- `audiencia` (enum `AudienciaNotificacion`: ADMIN, CONDUCTOR)
- `conductorId` (nullable; presente cuando va dirigida a un conductor)
- `leida` (boolean, default false)
- `fechaCreacion` (LocalDateTime)

### Targeting
- `audiencia=ADMIN` → la ven todos los admins.
- `audiencia=CONDUCTOR` + `conductorId` → la ve solo ese conductor.

### Emisión
Un helper `NotificacionService.crear(...)` llamado donde hoy se hace `eventoService.publicar(...)`:
- Crear/editar/borrar vehículo → ADMIN.
- Crear asignación → ADMIN + CONDUCTOR(conductorId).
- Reportar/cambiar estado/ completar mantenimiento → ADMIN + el conductor del vehículo (si está asignado).
- Cambiar/restablecer contraseña → ADMIN (tipo SEGURIDAD). **Cierra el requisito (4).**

### API
- `GET /api/notificaciones` (feed admin) y `GET /api/notificaciones/conductor/{id}` (feed conductor).
- `PATCH /api/notificaciones/{id}/leida` y `PATCH /api/notificaciones/leer-todas` (por audiencia/conductor).
- Tras crear notificaciones, `eventoService.publicar("notificaciones")` (reusa el SSE existente).

### App
- `NotificacionViewModel` + `NotificacionRepository` + DTO + `NotificacionApiService`.
- `SseRefreshEffect` agrega el caso `"notificaciones"` → `reloadSilently()`.
- **Admin:** `NotificationPanel` (Card) en `DashboardSection`, **entre Resumen y Estadísticas**: chip de no-leídas, últimas N (p. ej. 5), acción "marcar leídas", estado vacío "Sin novedades".
- **Conductor:** `DriverNotificationsSection` en `DriverHomeSection` con su feed targeteado.

### Estética / motion
Reusa petróleo+ámbar, `LeadingTile`, `StatusBadge`, `EmptyState`, estilos de instrumento (monospace). Filas: ícono tintado por tipo + mensaje + timestamp en monospace + punto ámbar si no-leída. Entrada escalonada sutil y rápida, sin rebote (Emil/Jakub: el admin la ve seguido).

### Errores
Fallo de red al cargar/mar­car → silencioso en recarga por SSE; el snackbar solo en acción explícita del usuario.

### Pruebas
Backend: `NotificacionService` (creación + targeting + marcar leída) con mocks. App: compilación + assemble.

### Archivos
Backend: `Notificacion`, `TipoNotificacion`, `AudienciaNotificacion`, `NotificacionRepo`, `NotificacionService`, `NotificacionController`, `NotificacionResponseDTO`, hooks en los services existentes (Vehiculo/Conductor/Mantenimiento/Asignacion/Auth). App: `NotificacionResponseDto`, `NotificacionApiService`, `NotificacionRepository`, `NotificacionViewModel`, `NotificationPanel`, `DriverNotificationsSection`, `NotificationItem`, edición de `DashboardSection`, `DriverHomeSection`, `SseRefreshEffect`, `MainActivity` (instanciar el VM).

---

## Fuera de alcance (YAGNI)
- Push del SO (FCM): in-app únicamente.
- Preferencias de notificaciones por usuario.
- Paginación del feed (se trae un tope; si hace falta, después).

## Riesgos / dependencias
- La API sigue abierta (fase 2 de seguridad pendiente): los endpoints de notificaciones reciben `conductorId`/rol por parámetro; cuando se cierre la API, se derivan del JWT.
- `Notificacion` agrega una tabla nueva (Hibernate `ddl-auto=update` la crea sola).
- Fase 2 depende de Fase 1 solo para el aviso de password→admin; lo demás es independiente.
- Entrega: cada fase = su rama + PR a `develop`.
