package com.aubynsamuel.clipsync.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun DarkModeToggle(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Colors for light and dark states
    val backgroundColor by animateColorAsState(
        targetValue = if (isDarkMode)
            Color(0xFF2C2C2C) else Color(0xFFFFF9C4),
        animationSpec = tween(durationMillis = 500),
        label = "backgroundColorAnimation"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isDarkMode)
            Color(0xFFE0E0E0) else Color(0xFFFF9800),
        animationSpec = tween(durationMillis = 500),
        label = "iconColorAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isDarkMode)
            Color(0xFF505050) else Color(0xFFFFD54F),
        animationSpec = tween(durationMillis = 500),
        label = "borderColorAnimation"
    )

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = if (isDarkMode) 1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scaleAnimation"
    )

    // Rotation animation
    val rotation by animateFloatAsState(
        targetValue = if (isDarkMode) 360f else 0f,
        animationSpec = tween(
            durationMillis = 750,
            easing = FastOutSlowInEasing
        ),
        label = "rotationAnimation"
    )

    // Stars/rays animation for night/day modes
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val starPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "starPulseAnimation"
    )

    val moveX = animateFloatAsState(if (isDarkMode) 57f else 0f)

    val wheelColorAnimation =
        animateColorAsState(
            targetValue = if (isDarkMode) Color.White else Color.Black,
            animationSpec = tween(durationMillis = 1000),
            label = "wheelColorAnimation"
        )

    Row(
        modifier = Modifier
            .width(50.dp)
            .background(
                wheelColorAnimation.value,
                RoundedCornerShape(15.dp)
            )
            .animateContentSize()
            .clickable { onToggle() }

    ) {
        Box(
            modifier = modifier
                .graphicsLayer {
                    translationX = moveX.value
                }
                .size(30.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(2.dp, borderColor, CircleShape)
                .clickable { onToggle() }
                .scale(scale)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // The icon with rotation and pulsing effect
            Icon(
                imageVector = if (isDarkMode) Icons.Filled.Nightlight else Icons.Filled.WbSunny,
                contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
                    .graphicsLayer {
                        if (isDarkMode) {
                            scaleX = starPulse
                            scaleY = starPulse
                        }
                    }
            )
        }
    }
}