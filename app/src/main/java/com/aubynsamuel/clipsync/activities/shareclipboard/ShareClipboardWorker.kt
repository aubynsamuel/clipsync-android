package com.aubynsamuel.clipsync.activities.shareclipboard

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.bluetooth.SharingResult
import com.aubynsamuel.clipsync.core.getSharingResultMessage
import com.aubynsamuel.clipsync.core.tag
import com.aubynsamuel.clipsync.notification.sharingResultNotification
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ShareClipboardWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val clipText = inputData.getString(KEY_CLIP_TEXT)
        if (clipText.isNullOrEmpty()) {
            Log.e(tag, "ShareClipboardWorker: No clip text provided.")
            sharingResultNotification(
                "Clipboard Sharing Failed",
                "Clipboard is empty",
                applicationContext
            )
            return Result.failure()
        }

        var bluetoothService: BluetoothService? = null
        var bound = false

        val connection = BluetoothServiceConnection(
            onServiceConnected = { service ->
                bluetoothService = service
                bound = true
                Log.d(tag, "ShareClipboardWorker: BluetoothService connected.")
            },
            onServiceDisconnected = {
                bound = false
                bluetoothService = null
                Log.d(tag, "ShareClipboardWorker: BluetoothService disconnected.")
            }
        )

        return suspendCancellableCoroutine { continuation ->
            val intent = Intent(applicationContext, BluetoothService::class.java)
            val serviceBound =
                applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)

            if (!serviceBound) {
                Log.e(tag, "ShareClipboardWorker: Failed to bind to BluetoothService.")
                sharingResultNotification(
                    "Clipboard Sharing Failed",
                    "Make bluetooth is turned on",
                    applicationContext
                )
                continuation.resume(Result.failure())
                return@suspendCancellableCoroutine
            }

            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            handler.postDelayed({
                runBlocking {
                    if (bluetoothService != null) {
                        try {
                            val result = bluetoothService?.shareClipboard(clipText)
                            if (result != SharingResult.SUCCESS) {
                                val message = getSharingResultMessage(result)
                                sharingResultNotification(
                                    "Clipboard Sharing Failed",
                                    message,
                                    applicationContext
                                )
                            }
                            Log.d(tag, "ShareClipboardWorker: Clipboard shared successfully.")
                            continuation.resume(Result.success())
                        } catch (e: Exception) {
                            Log.e(
                                tag,
                                "ShareClipboardWorker: Error sharing clipboard: ${e.message}"
                            )
                            sharingResultNotification(
                                "Clipboard Sharing Failed",
                                "Make sure the receiving device is ready",
                                applicationContext
                            )
                            continuation.resume(Result.failure())
                        } finally {
                            if (bound) {
                                applicationContext.unbindService(connection)
                            }
                        }
                    } else {
                        Log.e(
                            tag,
                            "ShareClipboardWorker: BluetoothService not available after binding attempt."
                        )
                        sharingResultNotification(
                            "Clipboard Sharing Failed",
                            "Make bluetooth is turned on",
                            applicationContext
                        )
                        continuation.resume(Result.failure())
                    }
                }
            }, 500)
        }
    }

    companion object {
        const val KEY_CLIP_TEXT = "clip_text"
    }
}