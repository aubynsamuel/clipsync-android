// TransparentActivity.kt
package com.aubynsamuel.clipsync

import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity

class TransparentActivity : ComponentActivity() {
    private var bluetoothService: BluetoothService? = null
    private var bound = false
    private var pendingShareAction = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as BluetoothService.LocalBinder
            bluetoothService = binder.getService()
            bound = true

            if (pendingShareAction) {
                handleShareAction()
                pendingShareAction = false
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
            bluetoothService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bind to the service
        Intent(this, BluetoothService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }

        when (intent?.action) {
            "ACTION_SHARE" -> {
                if (bound) {
                    handleShareAction()
                } else {
                    pendingShareAction = true
                }
            }

            else -> {
                // If no action to handle, finish after binding
                if (!pendingShareAction) {
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    // Added delay to allow clipboard contents to be available.
    private fun handleShareAction() {
        Handler(Looper.getMainLooper()).postDelayed({
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

//            unnecessary checks
//            if (clipboardManager.hasPrimaryClip() &&
//                clipboardManager.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true
//            ) {
            val clipItem = clipboardManager.primaryClip?.getItemAt(0)
            val clipText = clipItem?.text?.toString() ?: ""

            if (clipText.isNotEmpty()) {
                bluetoothService?.shareClipboard(clipText)
                Toast.makeText(this, "Clipboard shared!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            }
//            } else {
//                Toast.makeText(this, "No text in clipboard", Toast.LENGTH_SHORT).show()
//            }
            finish()
        }, 300)
    }
}
