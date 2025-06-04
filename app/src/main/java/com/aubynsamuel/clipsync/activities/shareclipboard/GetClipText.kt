package com.aubynsamuel.clipsync.activities.shareclipboard

import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE

class GetClipTextUseCase(private val context: Context) {
    operator fun invoke(): String? {
        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        return clipboard.primaryClip?.getItemAt(0)?.text.toString()
    }
}