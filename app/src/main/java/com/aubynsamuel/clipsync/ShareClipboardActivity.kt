package com.aubynsamuel.clipsync

import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ShareClipboardActivity : ComponentActivity() {
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

    private fun handleShareAction() {
        Handler(Looper.getMainLooper()).postDelayed({
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipText = clipboardManager.primaryClip
                ?.getItemAt(0)
                ?.text
                .toString()
            if (clipText == "null" || clipText.isBlank()) {
                showToast("Clipboard is empty", this)
                finish()
                return@postDelayed
            }

            lifecycleScope.launch {
                val result = bluetoothService?.shareClipboard(clipText)

                when (result) {
                    SharingResult.SUCCESS -> showToast(
                        "Clipboard shared!",
                        this@ShareClipboardActivity
                    )

                    SharingResult.SENDING_ERROR -> showToast(
                        "Sending failed",
                        this@ShareClipboardActivity
                    )

                    SharingResult.PERMISSION_NOT_GRANTED -> showToast(
                        "Bluetooth permission not granted",
                        this@ShareClipboardActivity
                    )

                    SharingResult.NO_SELECTED_DEVICES -> showToast(
                        "No devices selected",
                        this@ShareClipboardActivity
                    )

                    else -> showToast("Sending failed", this@ShareClipboardActivity)
                }
                finish()
            }
        }, 300)
    }
}
