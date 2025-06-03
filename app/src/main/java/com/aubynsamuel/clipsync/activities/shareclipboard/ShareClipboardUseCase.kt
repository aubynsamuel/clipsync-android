package com.aubynsamuel.clipsync.activities.shareclipboard

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.bluetooth.SharingResult
import com.aubynsamuel.clipsync.core.showToast
import com.aubynsamuel.clipsync.core.tag

class ShareClipboardUseCase(
    private val context: Context,
) {
    val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    suspend fun execute(
        bluetoothService: BluetoothService?,
        essentialsBluetoothService: BluetoothService?,
        callBack: () -> Unit = {},
    ) {
        try {
            val clipText = clipboardManager.primaryClip
                ?.getItemAt(0)
                ?.text
                ?.toString()
                ?.takeIf { it != "null" && it.isNotBlank() }

            callBack()

            val result =
                bluetoothService?.shareClipboard(clipText.toString())
                    ?: essentialsBluetoothService?.shareClipboard(clipText.toString())
                    ?: SharingResult.SENDING_ERROR

            val message = when (result) {
                SharingResult.SUCCESS -> "Clipboard shared!"
                SharingResult.SENDING_ERROR -> "Sending failed"
                SharingResult.PERMISSION_NOT_GRANTED -> "Bluetooth permission not granted"
                SharingResult.NO_SELECTED_DEVICES -> "No devices selected"
                else -> "Sending failed"
            }

            showToast(message, context)

        } catch (e: Exception) {
            Log.e(tag, "Sending failed, reason: ${e.message}")
//            showToast("Sending failed", context)
        }
    }
}