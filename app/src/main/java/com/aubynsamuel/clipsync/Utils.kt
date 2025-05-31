package com.aubynsamuel.clipsync

import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import com.aubynsamuel.clipsync.Essentials.isDarkMode

const val tag = "BluetoothService"
const val key = "isDarkMode"

fun showToast(msg: String, context: Context) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun changeTheme(context: Context) {
    isDarkMode = !isDarkMode
    val sharedPreferences = context.getSharedPreferences("clipSyncCache", Context.MODE_PRIVATE)
    sharedPreferences.edit { putString(key, (isDarkMode).toString()) }
}

fun getTheme(context: Context) {
    val sharedPreferences = context.getSharedPreferences("clipSyncCache", Context.MODE_PRIVATE)
    isDarkMode = sharedPreferences.getString(key, false.toString()).toBoolean()
}