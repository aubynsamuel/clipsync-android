package com.aubynsamuel.clipsync.widget.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

@Composable
fun ErrorContent(message: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ClipSync Widget Error",
            style = TextStyle().copy(
                fontSize = 16.sp,
                fontWeight = androidx.glance.text.FontWeight.Medium,
            )
        )

        Spacer(GlanceModifier.height(8.dp))

        Text(
            text = message,
            style = TextStyle().copy(
                fontSize = 14.sp,
            )
        )
    }
}
