package com.aubynsamuel.clipsync.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.core.showToast

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ACTION_DISMISS" -> {
                val serviceIntent = Intent(context, BluetoothService::class.java)
                context.stopService(serviceIntent)

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(1001)
            }

            "ACTION_COPY" -> {
                val clipText = intent.getStringExtra("CLIP_TEXT") ?: return

                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Received Text", clipText)
                clipboardManager.setPrimaryClip(clipData)

                // On Android 13+, the system shows its own toast when setting clipboard
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    showToast("Copied to clipboard", context)
                }

                // Dismiss the notification
                val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
                if (notificationId != 0) {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(notificationId)
                }
            }
        }
    }
}