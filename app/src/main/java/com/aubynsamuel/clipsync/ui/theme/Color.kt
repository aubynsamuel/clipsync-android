package com.aubynsamuel.clipsync.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.aubynsamuel.clipsync.R

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object Colors {
    val primary: Color @Composable get() = colorResource(R.color.primary)
    val secondary: Color @Composable get() = colorResource(R.color.secondary)
    val accent: Color @Composable get() = colorResource(R.color.accent)
    val stopBg: Color @Composable get() = colorResource(R.color.stop_bg)

    val background: Color @Composable get() = colorResource(R.color.background)
    val surface: Color @Composable get() = colorResource(R.color.surface)

    val onPrimary: Color @Composable get() = colorResource(R.color.on_primary)
    val textDark: Color @Composable get() = colorResource(R.color.text_dark)
    val textMedium: Color @Composable get() = colorResource(R.color.text_medium)
    val textLight: Color @Composable get() = colorResource(R.color.text_light)

    val success: Color @Composable get() = colorResource(R.color.success)
    val error: Color @Composable get() = colorResource(R.color.error)
}