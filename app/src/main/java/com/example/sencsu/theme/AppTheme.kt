package com.example.sencsu.theme


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==============================================================================
// DÉFINITION DES COULEURS
// ==============================================================================

object AppColors {
    // Couleurs primaires
    val BrandBlue = Color(0xFF0052CC)
    val BrandBlueDark = Color(0xFF003D99)
    val BrandBlueLite = Color(0xFFE6F0FF)

    // Couleurs de statut
    val StatusGreen = Color(0xFF34D399)
    val StatusGreenDark = Color(0xFF059669)
    val StatusOrange = Color(0xFFFB923C)
    val StatusOrangeDark = Color(0xFFDC2626)
    val StatusRed = Color(0xFFEF4444)

    // Couleurs de fond
    val AppBackground = Color(0xFFFBFDFF)
    val SurfaceBackground = Color(0xFFFFFFFF)
    val SurfaceAlt = Color(0xFFF8FAFC)

    // Couleurs de texte
    val TextMain = Color(0xFF0F172A)
    val TextSub = Color(0xFF64748B)
    val TextDisabled = Color(0xFF94A3B8)
    val TextMuted = Color(0xFFCBD5E1)

    // Couleurs de bordure
    val BorderColor = Color(0xFFE2E8F0)
    val BorderColorLight = Color(0xFFF1F5F9)

    // Couleurs additionnelles
    val SuccessLight = Color(0xFFD1FAE5)
    val WarningLight = Color(0xFFFEF3C7)
    val DangerLight = Color(0xFFFEE2E2)
    val InfoLight = Color(0xFFDEBEFF)
}

// ==============================================================================
// FORMES PERSONNALISÉES
// ==============================================================================

object AppShapes {
    val LargeRadius: RoundedCornerShape = RoundedCornerShape(24.dp)
    val MediumRadius: RoundedCornerShape = RoundedCornerShape(16.dp)
    val SmallRadius: RoundedCornerShape = RoundedCornerShape(12.dp)
    val ExtraSmallRadius: RoundedCornerShape = RoundedCornerShape(8.dp)
    val CircleRadius: RoundedCornerShape = RoundedCornerShape(50)
}

// ==============================================================================
// DIMENSIONS ET ESPACEMENTS
// ==============================================================================

object AppDimensions {
    // Espacements
    val paddingXSmall: Dp = 4.dp
    val paddingSmall: Dp = 8.dp
    val paddingMedium: Dp = 16.dp
    val paddingLarge: Dp = 20.dp
    val paddingXLarge: Dp = 24.dp
    val paddingXXLarge: Dp = 32.dp

    // Hauteurs
    val buttonHeight: Dp = 56.dp
    val buttonSmallHeight: Dp = 40.dp
    val appBarHeight: Dp = 64.dp

    // Largeurs/Tailles
    val cardElevation: Dp = 2.dp
    val borderWidth: Dp = 1.dp
    val dividerThickness: Dp = 0.5.dp

    // Icônes
    val iconSmall: Dp = 16.dp
    val iconMedium: Dp = 24.dp
    val iconLarge: Dp = 32.dp
    val iconXLarge: Dp = 48.dp

    // Images
    val profileImageSize: Dp = 110.dp
    val avatarSize: Dp = 50.dp
    val thumbnailSize: Dp = 100.dp
}

// ==============================================================================
// ÉLÉVATION ET OMBRES
// ==============================================================================

object AppElevation {
    val card: Dp = 0.dp
    val button: Dp = 4.dp
    val fab: Dp = 6.dp
    val dialog: Dp = 24.dp
}

// ==============================================================================
// DURATIONS (Animations)
// ==============================================================================

object AppDurations {
    const val SHORT: Long = 150L
    const val MEDIUM: Long = 300L
    const val LONG: Long = 500L
}

// ==============================================================================
// SCHÉMA DE COULEUR MATERIAL 3
// ==============================================================================

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = AppColors.BrandBlue,
        secondary = AppColors.StatusGreen,
        tertiary = AppColors.StatusOrange,
        error = AppColors.StatusRed,
        background = AppColors.AppBackground,
        surface = AppColors.SurfaceBackground,
        outline = AppColors.BorderColor,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// ==============================================================================
// UTILITAIRES DE COULEUR
// ==============================================================================

/**
 * Extension pour faciliter l'utilisation des couleurs avec transparence
 */
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)

/**
 * Extension pour obtenir la couleur complémentaire
 */
fun Color.getComplementary(): Color {
    val r = 255 - (this.red * 255).toInt()
    val g = 255 - (this.green * 255).toInt()
    val b = 255 - (this.blue * 255).toInt()
    return Color(r, g, b)
}

/**
 * Extension pour assombrir une couleur
 */
fun Color.darken(factor: Float = 0.2f): Color {
    return Color(
        red = (this.red * (1 - factor)).coerceIn(0f, 1f),
        green = (this.green * (1 - factor)).coerceIn(0f, 1f),
        blue = (this.blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}

/**
 * Extension pour éclaircir une couleur
 */
fun Color.lighten(factor: Float = 0.2f): Color {
    return Color(
        red = (this.red + (1 - this.red) * factor).coerceIn(0f, 1f),
        green = (this.green + (1 - this.green) * factor).coerceIn(0f, 1f),
        blue = (this.blue + (1 - this.blue) * factor).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}