package com.aubynsamuel.clipsync.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeviceItem(onChecked: () -> Unit, checked: Boolean, name: String, address: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = { onChecked() }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onChecked() }
        )

        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = name, color = colorScheme.onBackground)
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface
            )
        }
    }
}