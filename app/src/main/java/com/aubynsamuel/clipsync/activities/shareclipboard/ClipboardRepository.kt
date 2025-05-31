package com.aubynsamuel.clipsync.activities.shareclipboard

import android.content.ClipboardManager

interface ClipboardRepository {
    fun getClipboardText(): String?
}

class AndroidClipboardRepository(private val clipboardManager: ClipboardManager) :
    ClipboardRepository {
    override fun getClipboardText(): String? {
        return clipboardManager.primaryClip
            ?.getItemAt(0)
            ?.text
            ?.toString()
            ?.takeIf { it != "null" && it.isNotBlank() }
    }
}