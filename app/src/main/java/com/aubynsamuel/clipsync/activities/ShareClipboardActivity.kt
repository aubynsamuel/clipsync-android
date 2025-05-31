package com.aubynsamuel.clipsync.activities

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.aubynsamuel.clipsync.activities.shareclipboard.AndroidClipboardRepository
import com.aubynsamuel.clipsync.activities.shareclipboard.BluetoothServiceConnection
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardPresenter
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardUseCase
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardView
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.core.showToast
import kotlinx.coroutines.launch

class ShareClipboardActivity : ComponentActivity(), ShareClipboardView {
    companion object {
        const val ACTION_SHARE = "ACTION_SHARE"
        private const val SHARE_DELAY_MS = 300L
    }

    private var presenter: ShareClipboardPresenter? = null
    private var serviceConnection: BluetoothServiceConnection? = null
    private var bound = false
    private var pendingShareAction = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupServiceConnection()
        bindToBluetoothService()
        handleIntent()
    }

    private fun setupServiceConnection() {
        serviceConnection = BluetoothServiceConnection(
            onServiceConnected = { service ->
                bound = true
                initializePresenter(service)
                if (pendingShareAction) {
                    executeShareAction()
                    pendingShareAction = false
                }
            },
            onServiceDisconnected = {
                bound = false
                presenter = null
            }
        )
    }

    private fun bindToBluetoothService() {
        val intent = Intent(this, BluetoothService::class.java)
        bindService(intent, serviceConnection!!, BIND_AUTO_CREATE)
    }

    private fun initializePresenter(bluetoothService: BluetoothService) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipboardRepository = AndroidClipboardRepository(clipboardManager)
        val shareClipboardUseCase = ShareClipboardUseCase(clipboardRepository, bluetoothService)
        presenter = ShareClipboardPresenter(shareClipboardUseCase, this)
    }

    private fun handleIntent() {
        when (intent?.action) {
            ACTION_SHARE -> {
                if (bound) {
                    executeShareAction()
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

    private fun executeShareAction() {
        handler.postDelayed({
            lifecycleScope.launch {
                presenter?.handleShare()
            }
        }, SHARE_DELAY_MS)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            serviceConnection?.let { unbindService(it) }
            bound = false
        }
        handler.removeCallbacksAndMessages(null)
    }

    override fun showMessage(message: String) {
        showToast(message, this)
    }

    override fun finishActivity() {
        finish()
    }
}