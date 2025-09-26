package com.example.urbanfix.ui.theme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

private val LightColors = lightColorScheme(
    primary = PurpleMain,
    onPrimary = WhiteFull,
    secondary = BlueMain,
    onSecondary = WhiteFull,
    background = GrayBg,
    onBackground = BlackFull,
    surface = WhiteFull,
    onSurface = BlackFull
)

@Composable
fun UrbanFixTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
