package com.soundboard.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// ── Palette ──────────────────────────────────────────────────────────────────
val Amber400   = Color(0xFFFFB946)
val Amber500   = Color(0xFFD4901A)
val Amber600   = Color(0xFFB07215)
val Amber100   = Color(0xFFFFF0C8)

val SurfaceDark   = Color(0xFF141414)
val Surface1      = Color(0xFF1E1E1E)
val Surface2      = Color(0xFF262626)
val Surface3      = Color(0xFF2E2E2E)
val Surface4      = Color(0xFF383838)

val OnSurface     = Color(0xFFE8E0D4)
val OnSurfaceDim  = Color(0xFF9A9080)
val OnSurfaceFaint= Color(0xFF5A5248)

val DangerRed     = Color(0xFFE05A5A)
val SuccessGreen  = Color(0xFF5ABE82)
val PlayingGlow   = Color(0xFF30C060)

// ── Tile preset colours ───────────────────────────────────────────────────────
val TilePresets = listOf(
    Color(0xFFD4901A), // Amber
    Color(0xFF1A7AD4), // Blue
    Color(0xFF4CAF50), // Green
    Color(0xFFE53935), // Red
    Color(0xFF8E24AA), // Purple
    Color(0xFF00ACC1), // Cyan
    Color(0xFFFF7043), // Deep Orange
    Color(0xFF43A047), // Medium Green
    Color(0xFF1E88E5), // Medium Blue
    Color(0xFFD81B60), // Pink
    Color(0xFF6D4C41), // Brown
    Color(0xFF546E7A), // Blue Grey
)

// ── Color Scheme ─────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = Amber400,
    onPrimary        = Color(0xFF1A0F00),
    primaryContainer = Amber600,
    secondary        = Amber500,
    background       = SurfaceDark,
    surface          = Surface1,
    surfaceVariant   = Surface2,
    onBackground     = OnSurface,
    onSurface        = OnSurface,
    onSurfaceVariant = OnSurfaceDim,
    outline          = Surface4,
    error            = DangerRed,
)

@Composable
fun SoundboardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
