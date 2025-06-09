package com.aubynsamuel.clipsync.core

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import com.aubynsamuel.clipsync.bluetooth.SharingResult

fun showToast(msg: String, context: Context) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun getSharingResultMessage(sendingResult: SharingResult?): String {
    val sharingResultMessage = when (sendingResult) {
        SharingResult.SUCCESS -> "Clipboard shared!"
        SharingResult.SENDING_ERROR -> "Sending failed"
        SharingResult.PERMISSION_NOT_GRANTED -> "Bluetooth permission not granted"
        SharingResult.NO_SELECTED_DEVICES -> "No devices selected"
        else -> "Sending failed"
    }
    return sharingResultMessage
}

fun copyToClipboard(clipText: String, context: Context) {
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Received Text", clipText)
    clipboardManager.setPrimaryClip(clipData)

    // On Android 13+, the system shows its own toast when setting clipboard
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        showToast("Copied to clipboard", context)
    }
}