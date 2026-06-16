# Sistema de notificaciones (Fase 2) — Plan de implementación

**Goal:** Notificaciones in-app: el admin ve actividad de la flota; el conductor ve avisos targeteados (su asignación, mantenimiento de su vehículo). Cierra el aviso password→admin de la Fase 1.

**Architecture:** Entidad `Notificacion` en el backend con `audiencia` (ADMIN/CONDUCTOR) + `conductorId` opcional. Se emite donde hoy se hace `eventoService.publicar(...)`; tras emitir se publica el ping SSE `"notificaciones"` para que las apps recarguen. La app trae el feed por REST y lo refresca por SSE.

**Tech:** Spring Boot/JPA (backend) · Kotlin/Compose/Retrofit (app). Ramas: backend `feat/notificaciones` desde develop; app `claude/notificaciones-ui` desde origin/develop.

---

## Backend (repo Api---CarCare)

### B1 — Enums
- `TipoNotificacion { VEHICULO, CONDUCTOR, MANTENIMIENTO, ASIGNACION, SEGURIDAD }`
- `AudienciaNotificacion { ADMIN, CONDUCTOR }`

### B2 — Entidad `Notificacion`
Campos: `id`, `tipo` (@Enumerated STRING), `mensaje` (length 300), `audiencia` (@Enumerated STRING), `conductorId` (nullable), `leida` (boolean default false), `fechaCreacion` (LocalDateTime, default now).

### B3 — `NotificacionRepo`
- `findTop50ByAudienciaOrderByFechaCreacionDesc(AudienciaNotificacion)`
- `findTop50ByConductorIdOrderByFechaCreacionDesc(Long)`
- `findByAudienciaAndLeidaFalse(...)` / `findByConductorIdAndLeidaFalse(...)` para marcar todas.

### B4 — `NotificacionService`
- `paraAdmin(tipo, mensaje)`, `paraConductor(tipo, mensaje, conductorId)`.
- `mantenimientoDeVehiculo(tipo, mensaje, vehiculoId)`: crea ADMIN + (si hay asignación ACTIVE en ese vehículo) CONDUCTOR. Inyecta `AsignacionRepo`.
- `listarAdmin()`, `listarConductor(id)`, `marcarLeida(id)`, `marcarTodasAdmin()`, `marcarTodasConductor(id)`.
- Cada método de creación publica el ping: `eventoService.publicar("notificaciones")`.

### B5 — `NotificacionResponseDTO` + `NotificacionController`
- `GET /api/notificaciones` (admin), `GET /api/notificaciones/conductor/{id}`.
- `PATCH /api/notificaciones/{id}/leida`, `PATCH /api/notificaciones/leer-todas?conductorId=` (si viene, conductor; si no, admin).

### B6 — Emisión (en controllers, junto a `eventoService.publicar`)
- Vehiculo: crear/actualizar/eliminar/estado → `paraAdmin(VEHICULO, ...)`.
- Conductor: crear/actualizar/eliminar/estado → `paraAdmin(CONDUCTOR, ...)`; `restablecerPassword` → `paraConductor(SEGURIDAD, "Un administrador cambió tu contraseña", id)`.
- Mantenimiento: crear/actualizar/estado → `mantenimientoDeVehiculo(MANTENIMIENTO, ..., vehiculoId)`; eliminar → `paraAdmin`.
- Asignacion: crear → `paraAdmin` + `paraConductor(ASIGNACION, "Se te asignó el vehículo X", conductorId)`; completar → ambos; eliminar → `paraAdmin`.
- Auth: `cambiarPassword` (self-service) → `paraAdmin(SEGURIDAD, "El conductor {cedula} cambió su contraseña")`.

### B7 — Test
`NotificacionServiceTest` (mocks): `paraAdmin` crea con audiencia ADMIN y publica SSE; `mantenimientoDeVehiculo` con asignación activa crea 2 (ADMIN+CONDUCTOR); sin asignación crea 1.

### B8 — Verificar / commit / PR
`mvnw compile` + test; commit; push; PR a develop.

---

## App (repo CarCare)

### A1 — DTO + API + repo
- `NotificacionResponseDto` (id, tipo, mensaje, audiencia, conductorId, leida, fechaCreacion).
- `NotificacionApiService`: listar(), listarConductor(id), marcarLeida(id), leerTodas(conductorId?).
- `NotificacionRepository` → mapea a modelo `Notificacion` (dominio app).

### A2 — Modelo + ViewModel
- `model/Notificacion.kt` (Identifiable) con `tipo: NotificationType` (enum app espejo), `read`, `timestamp`.
- `NotificacionViewModel`: `items`, `unreadCount` (derivado), `loadForAdmin()`, `loadForConductor(id)`, `reloadSilently...`, `markRead(id)`, `markAllRead(...)`.

### A3 — UI componentes
- `NotificationItem` (LeadingTile por tipo + mensaje + timestamp monospace + punto ámbar si no-leída).
- `NotificationPanel` (admin): Card con header "Notificaciones" + chip no-leídas + últimas 5 + "marcar leídas" + EmptyState.
- `DriverNotificationsSection` (conductor): lista compacta en Inicio.

### A4 — Integración
- `MainActivity`: crea `NotificacionViewModel`, lo pasa a Admin y Driver.
- `AdminScreen`: `LaunchedEffect` carga feed admin; SSE `"notificaciones"` → reload; `DashboardSection` recibe el panel entre Resumen y Estadísticas.
- `DriverScreen`: carga feed conductor por `conductorId` (AuthSession); SSE reload; sección en `DriverHomeSection`.
- `SseRefreshEffect` ya enruta por nombre de entidad → ambas screens agregan caso `"notificaciones"`.

### A5 — Verificar / commit / PR
`compileDebugKotlin` + `assembleDebug`; commit; push; PR a develop.

---

## Notas
- API abierta (fase 2 seguridad pendiente): el feed del conductor se pide por `conductorId` (de AuthSession). Cuando se cierre la API, se deriva del JWT.
- `Notificacion` = tabla nueva (Hibernate `ddl-auto=update` la crea).
- Sin paginación (tope 50) ni push del SO.
