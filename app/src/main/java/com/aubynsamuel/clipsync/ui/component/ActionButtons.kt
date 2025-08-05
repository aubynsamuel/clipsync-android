package com.aubynsamuel.clipsync.ui.component

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aubynsamuel.clipsync.activities.ShareClipboardActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ActionButtons(
    startBluetoothService: (Set<String>) -> Unit,
    stopBluetoothService: () -> Unit,
    selectedDeviceAddresses: Set<String>,
    scope: CoroutineScope,
    context: Context,
    isServiceBound: Boolean,
) {

    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
        Button(
            onClick = {
                if (isServiceBound)
                    stopBluetoothService()
                else startBluetoothService(selectedDeviceAddresses)
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isServiceBound) colorScheme.error else colorScheme.primaryContainer,
                contentColor = if (isServiceBound) colorScheme.onError else colorScheme.onPrimaryContainer
            ),
        ) { Text(if (isServiceBound) "Stop" else "Start", fontWeight = FontWeight.Bold) }

        Button(
            onClick = {
                scope.launch {
                    val intent = Intent(context, ShareClipboardActivity::class.java).apply {
                        action = "ACTION_SHARE"
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer
            ),
            enabled = isServiceBound && selectedDeviceAddresses.isNotEmpty()
        ) { Text("Share", fontWeight = FontWeight.Bold) }
    }
}