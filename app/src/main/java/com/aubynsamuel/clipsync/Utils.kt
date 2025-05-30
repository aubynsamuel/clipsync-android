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