package com.aubynsamuel.clipsync.activities.shareclipboard

import com.aubynsamuel.clipsync.bluetooth.SharingResult

class ShareClipboardPresenter(
    private val shareClipboardUseCase: ShareClipboardUseCase,
    private val view: ShareClipboardView,
) {
    suspend fun handleShare() {
        val result = shareClipboardUseCase.execute()

        val message = when (result) {
            SharingResult.SUCCESS -> "Clipboard shared!"
            SharingResult.SENDING_ERROR -> "Sending failed"
            SharingResult.PERMISSION_NOT_GRANTED -> "Bluetooth permission not granted"
            SharingResult.NO_SELECTED_DEVICES -> "No devices selected"
            SharingResult.CLIPBOARD_EMPTY -> "Clipboard is empty"
            else -> "Sending failed"
        }

        view.showMessage(message)
        view.finishActivity()
    }
}