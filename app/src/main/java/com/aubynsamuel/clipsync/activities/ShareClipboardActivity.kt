package com.aubynsamuel.clipsync.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.aubynsamuel.clipsync.activities.shareclipboard.BluetoothServiceConnection
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardUseCase
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import kotlinx.coroutines.launch

class ShareClipboardActivity : ComponentActivity() {
    private var bluetoothService: BluetoothService? = null
    private var bound = false
    private var pendingShareAction = false
    private val handler = Handler(Looper.getMainLooper())

    private val connection = BluetoothServiceConnection(
        onServiceConnected = { service ->
            bluetoothService = service
            bound = true

            if (pendingShareAction) {
                handleShareAction()
                pendingShareAction = false
            }
        },
        onServiceDisconnected = {
            bound = false
            bluetoothService = null
        }
    )

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
        handler.postDelayed({
            lifecycleScope.launch {
                ShareClipboardUseCase(this@ShareClipboardActivity).execute(
                    bluetoothService = bluetoothService,
                    essentialsBluetoothService = null,
                    callBack = { finish() }
                )
            }
        }, 300)
    }
}