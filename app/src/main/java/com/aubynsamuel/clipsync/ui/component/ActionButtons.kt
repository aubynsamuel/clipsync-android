package com.aubynsamuel.clipsync.ui.component

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardUseCase
import com.aubynsamuel.clipsync.core.Essentials
import com.aubynsamuel.clipsync.core.Essentials.serviceStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ActionButtons(
    startBluetoothService: (Set<String>) -> Unit,
    stopBluetoothService: () -> Unit,
    selectedDeviceAddresses: Set<String>,
    scope: CoroutineScope,
    context: Context,
) {

    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
        Button(
            onClick = {
                if (serviceStarted)
                    stopBluetoothService()
                else startBluetoothService(selectedDeviceAddresses)
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (serviceStarted) colorScheme.error else colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
        ) { Text(if (serviceStarted) "Stop" else "Start") }

        Button(
            onClick = {
                scope.launch {
                    if (!serviceStarted) {
                        startBluetoothService(selectedDeviceAddresses)
                        delay(500)
                    }

                    ShareClipboardUseCase(context).execute(
                        bluetoothService = null,
                        essentialsBluetoothService = Essentials.bluetoothService,
                    )
                }
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            enabled = serviceStarted && selectedDeviceAddresses.isNotEmpty()
        ) { Text("Share") }
    }
}