package com.premium.auranotes.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryAccent, background = BackgroundDark, surface = SurfaceDark,
    onPrimary = Color.Black, onBackground = TextPrimaryDark, onSurface = TextPrimaryDark, surfaceVariant = Color(0xFF2A2A2A)
)
@Composable
fun AuraNotesTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, content = content)
}
