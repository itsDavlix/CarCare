package com.example.carcare.ui.theme

import androidx.compose.ui.graphics.Color

// ── Marca CarCare: "panel de operaciones de flota" ──────────────────
// Petróleo + ámbar de seguridad. Reemplaza la plantilla morada.

// Primario (petróleo)
val Petrol700 = Color(0xFF0E5A61)   // primary
val Petrol800 = Color(0xFF0A4244)   // primaryContainer / fondos profundos
val Petrol900 = Color(0xFF06302F)
val Petrol300 = Color(0xFF7FB6B0)   // primary en tema oscuro
val Petrol100 = Color(0xFFCFE6E2)

// Acento (ámbar)
val Amber = Color(0xFFF5A524)        // secondary / acentos, alertas suaves
val AmberDeep = Color(0xFFC97F12)
val AmberSoft = Color(0xFFFDF0D9)

// Acento del logo: aguja del velocímetro (mismo tono que StatusOutOfService, pero con rol de marca)
val NeedleRed = Color(0xFFE5484D)

// Neutros
val Ink = Color(0xFF14201F)          // texto principal
val Paper = Color(0xFFF3F5F2)        // background
val Surface = Color(0xFFFFFFFF)
val Line = Color(0xFFE2E7E4)
val Muted = Color(0xFF5C6B6A)

// Colores semánticos de estado (los mismos que ya usa el dashboard)
val StatusAvailable = Color(0xFF2E9E5B)
val StatusAssigned = Color(0xFF14B8A6)
val StatusInUse = Color(0xFF2D7FF9)
val StatusPendingReview = Color(0xFFB4791F)
val StatusMaintenance = Color(0xFFF59E0B)
val StatusOutOfService = Color(0xFFE5484D)
val StatusInactive = Color(0xFF98A2A0)