package com.aubynsamuel.clipsync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log

object ClipboardManager {
    private const val TAG = "ClipboardManager"
    private var lastClipboardText: String = ""

    fun getClipboardText(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        return if (clipboard.hasPrimaryClip()) {
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).text?.toString() ?: ""
            } else {
                ""
            }
        } else {
            ""
        }
    }

    fun setClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ClipSync Data", text)
        clipboard.setPrimaryClip(clip)

        // On Android 13+, a toast is automatically shown when clipboard is updated,
        // so we don't need to show our own notification
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "Clipboard updated with new text")
        }

        lastClipboardText = text
    }

    fun registerClipboardListener(context: Context, onClipboardChanged: (String) -> Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboard.addPrimaryClipChangedListener {
            val clipText = getClipboardText(context)

            // Only trigger if the text actually changed and isn't our last set value
            if (clipText.isNotEmpty() && clipText != lastClipboardText) {
                lastClipboardText = clipText
                onClipboardChanged(clipText)
            }
        }
    }
}