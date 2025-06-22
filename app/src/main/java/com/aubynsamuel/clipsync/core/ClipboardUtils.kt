package com.aubynsamuel.clipsync.core

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

fun getClipText(context: Context): String? {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    if (clipboard.primaryClip?.itemCount == 0) return null
    val clipItem = clipboard.primaryClip?.getItemAt(0) ?: return null
    val clipText = clipItem.text ?: return null
    return clipText.toString()
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