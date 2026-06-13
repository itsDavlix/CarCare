package com.example.carcare.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Formas de marca. Material las usa por defecto en sus componentes
 * (chips → small, Card/diálogos → medium, hojas grandes → large), así que
 * definirlas aquí da radios consistentes en TODA la app sin tocar cada widget.
 *
 *  - small  (10dp): chips, badges, contenedores de icono.
 *  - medium (16dp): tarjetas de lista, diálogos, campos.
 *  - large  (24dp): superficies destacadas / hero.
 */
val CarCareShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
