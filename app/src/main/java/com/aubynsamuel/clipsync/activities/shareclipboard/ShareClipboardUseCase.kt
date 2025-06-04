package com.aubynsamuel.clipsync.activities.shareclipboard

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.bluetooth.SharingResult
import com.aubynsamuel.clipsync.core.getSharingResultMessage
import com.aubynsamuel.clipsync.core.showToast
import com.aubynsamuel.clipsync.core.tag

class ShareClipboardUseCase(
    private val context: Context,
) {
    val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    suspend fun execute(bluetoothService: BluetoothService?) {
        try {
            val clipText = GetClipTextUseCase(context).invoke()

            val result =
                bluetoothService?.shareClipboard(clipText.toString())
                    ?: SharingResult.SENDING_ERROR

            val message = getSharingResultMessage(result)

            showToast(message, context)

        } catch (e: Exception) {
            Log.e(tag, "Sending failed, reason: ${e.message}")
            showToast("Sending failed", context)
        }
    }
}