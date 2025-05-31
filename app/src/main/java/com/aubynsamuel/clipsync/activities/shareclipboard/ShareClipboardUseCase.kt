package com.aubynsamuel.clipsync.activities.shareclipboard

import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.bluetooth.SharingResult

class ShareClipboardUseCase(
    private val clipboardRepository: ClipboardRepository,
    private val bluetoothService: BluetoothService,
) {
    suspend fun execute(): Enum<*> {
        val clipText = clipboardRepository.getClipboardText()
            ?: return SharingResult.CLIPBOARD_EMPTY

        return bluetoothService.shareClipboard(clipText)
    }
}