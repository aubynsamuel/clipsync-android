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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ShareClipboardWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    lateinit var bluetoothService: BluetoothServiceConnection

    override suspend fun doWork(): Result {
        val clipText = inputData.getString(KEY_CLIP_TEXT)
        if (clipText.isNullOrEmpty()) {
            Log.e(tag, "ShareClipboardWorker: No clip text provided.")
            sharingResultNotification(
                "Sharing Failed",
                "Clipboard is empty",
                applicationContext
            )
            return Result.failure()
        }

        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        return suspendCancellableCoroutine { continuation ->
            bluetoothService = BluetoothServiceConnection(
                onServiceConnected = { service ->
                    Log.d(tag, "ShareClipboardWorker: BluetoothService connected.")
                    serviceScope.launch {
                        try {
                            val result = service.shareClipboard(clipText)
                            if (result != SharingResult.SUCCESS) {
                                val message = getSharingResultMessage(result)
                                sharingResultNotification(
                                    "Sharing Failed",
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
                                "Sharing Failed",
                                "Make sure the receiving device is ready",
                                applicationContext
                            )
                            continuation.resume(Result.failure())
                        } finally {
                            applicationContext.unbindService(bluetoothService)
                        }
                    }
                },
                onServiceDisconnected = {
                    Log.d(tag, "ShareClipboardWorker: BluetoothService disconnected.")
                }
            )

            val intent = Intent(applicationContext, BluetoothService::class.java)
            val serviceBound =
                applicationContext.bindService(intent, bluetoothService, Context.BIND_AUTO_CREATE)

            if (!serviceBound) {
                Log.e(tag, "ShareClipboardWorker: Failed to bind to BluetoothService.")
                sharingResultNotification(
                    "Sharing Failed",
                    "Make bluetooth is turned on",
                    applicationContext
                )
                continuation.resume(Result.failure())
                return@suspendCancellableCoroutine
            }
        }
    }

    companion object {
        const val KEY_CLIP_TEXT = "clip_text"
    }
}