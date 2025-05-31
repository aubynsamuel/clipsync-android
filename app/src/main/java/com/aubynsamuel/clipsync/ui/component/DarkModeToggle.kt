package com.aubynsamuel.clipsync.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun DarkModeToggle(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isDarkMode)
            Color(0xFFFFF9C4) else Color(0xFF2C2C2C),
        animationSpec = tween(durationMillis = 500),
        label = "backgroundColorAnimation"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isDarkMode)
            Color(0xFFFF9800) else Color(0xFFE0E0E0),
        animationSpec = tween(durationMillis = 500),
        label = "iconColorAnimation"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isDarkMode)
            Color(0xFFFFD54F) else Color(0xFF505050),
        animationSpec = tween(durationMillis = 500),
        label = "borderColorAnimation"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isDarkMode) 360f else 0f,
        animationSpec = tween(
            durationMillis = 750,
            easing = FastOutSlowInEasing
        ),
        label = "rotationAnimation"
    )

    val moveX = animateFloatAsState(if (isDarkMode) 57f else 0f)

    val wheelColorAnimation =
        animateColorAsState(
            targetValue = if (isDarkMode) Color.Black else Color.White,
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
            .clickable { onToggle() },
//        horizontalArrangement = if (isDarkMode) Arrangement.Start else Arrangement.End
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
                .padding(8.dp),
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Filled.WbSunny else Icons.Filled.Nightlight,
                contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                tint = iconColor,
                modifier = Modifier
                    .size(30.dp)
                    .rotate(rotation)
            )
        }
    }
}