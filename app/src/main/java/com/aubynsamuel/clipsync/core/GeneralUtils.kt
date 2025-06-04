package com.aubynsamuel.clipsync.core

import android.content.Context
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
