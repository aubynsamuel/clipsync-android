package com.aubynsamuel.clipsync.activities.shareclipboard

import android.content.Context
import android.util.Log
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.bluetooth.SharingResult
import com.aubynsamuel.clipsync.core.getSharingResultMessage
import com.aubynsamuel.clipsync.core.showToast
import com.aubynsamuel.clipsync.core.tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShareClipboardUseCase(
    private val context: Context,
) {
    suspend fun execute(bluetoothService: BluetoothService?) {
        try {
            val clipText = GetClipTextUseCase(context).invoke()

            if (!clipText.isNullOrEmpty()) {
                val result = withContext(Dispatchers.IO) {
                    bluetoothService?.shareClipboard(clipText)
                        ?: SharingResult.SENDING_ERROR
                }
                val message = getSharingResultMessage(result)
                showToast(message, context)
            } else {
                Log.e(tag, "ShareClipboardWorker: No clip text provided.")
                showToast("Clipboard is empty", context)
            }

        } catch (e: Exception) {
            Log.e(tag, "Sending failed, reason: ${e.message}")
            showToast("Sending failed", context)
        }
    }
}