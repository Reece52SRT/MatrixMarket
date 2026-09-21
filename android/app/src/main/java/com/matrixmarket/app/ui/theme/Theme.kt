package com.matrixmarket.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val MatrixMarketColorScheme = lightColorScheme(
    primary = RoyalPurple,
    onPrimary = White,
    secondary = NeonPurple,
    background = LavenderLight,
    surface = White,
    surfaceVariant = LavenderCard,
    onBackground = TextDark,
    onSurface = TextDark,
    error = androidx.compose.ui.graphics.Color(0xFFE94B4B)
)

private val MatrixMarketTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)
)

@Composable
fun MatrixMarketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MatrixMarketColorScheme,
        typography = MatrixMarketTypography,
        content = content
    )
}
