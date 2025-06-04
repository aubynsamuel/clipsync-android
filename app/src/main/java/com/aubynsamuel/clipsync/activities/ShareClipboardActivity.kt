package com.aubynsamuel.clipsync.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aubynsamuel.clipsync.activities.shareclipboard.GetClipTextUseCase
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardWorker
import com.aubynsamuel.clipsync.core.tag

class ShareClipboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent?.action) {
            "ACTION_SHARE" -> handleShareAction()

            else -> finish()

        }
    }

    private fun handleShareAction() {
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val clipText = GetClipTextUseCase(this).invoke()
                Log.d(tag, "ShareClipboardActivity: Clipboard text: $clipText")
                val inputData = Data.Builder()
                    .putString(ShareClipboardWorker.KEY_CLIP_TEXT, clipText)
                    .build()

                val shareWorkRequest = OneTimeWorkRequestBuilder<ShareClipboardWorker>()
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(applicationContext).enqueue(shareWorkRequest)
                Log.d(tag, "ShareClipboardActivity: Enqueued ShareClipboardWorker.")
            } catch (e: Exception) {
                Log.e(tag, "ShareClipboardActivity: Failed to get clip text. Reason: ${e.message}")
            } finally {
                finish()
            }
        }, 300)
    }
}