package com.aubynsamuel.clipsync

import android.content.Context
import android.widget.Toast
import androidx.core.content.edit

const val tag = "BluetoothService"
const val key = "isDarkMode"

fun showToast(msg: String, context: Context) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun changeTheme(context: Context) {
    DarkMode.isDarkMode.value = !DarkMode.isDarkMode.value
    val sharedPreferences = context.getSharedPreferences("clipSyncCache", Context.MODE_PRIVATE)
    sharedPreferences.edit { putString(key, (DarkMode.isDarkMode.value).toString()) }
}

fun getTheme(context: Context) {
    val sharedPreferences = context.getSharedPreferences("clipSyncCache", Context.MODE_PRIVATE)
    DarkMode.isDarkMode.value = sharedPreferences.getString(key, false.toString()).toBoolean()
}

//fun createTempTextFile(context: Context, text: String, fileName: String = "clip.txt"): File? {
//    return try {
//        // Create a temporary file in the cache directory
//        val file = File(context.cacheDir, fileName)
//        file.writeText(text)
//        file
//    } catch (e: Exception) {
//        e.printStackTrace()
//        null
//    }
//}
//
//fun getFileUri(context: Context, file: File): android.net.Uri {
//    return FileProvider.getUriForFile(
//        context,
//        "${context.packageName}.fileProvider",
//        file
//    )
//}
//
//fun shareTextFile(context: Context, text: String) {
//    // Create a temporary text file from the provided text.
//    val file = createTempTextFile(context, text) ?: return
//    val fileUri = getFileUri(context, file)
//
//    // Build the sharing intent.
//    val shareIntent = Intent(Intent.ACTION_SEND).apply {
//        type = "text/plain"
//        putExtra(Intent.EXTRA_STREAM, fileUri)
//        // Optionally add a subject/extra text.
//        putExtra(Intent.EXTRA_SUBJECT, "Clipboard Text")
//        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//    }
//
//    // Launch the chooser.
//    context.startActivity(Intent.createChooser(shareIntent, "Share file via"))
//}