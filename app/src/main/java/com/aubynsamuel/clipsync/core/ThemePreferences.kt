package com.aubynsamuel.clipsync.core

import android.content.Context
import androidx.core.content.edit
import com.aubynsamuel.clipsync.core.Essentials.isDarkMode

const val tag = "BluetoothService"
const val key = "isDarkMode"

fun changeTheme(context: Context, value: Boolean? = null) {
    isDarkMode = !isDarkMode
    val sharedPreferences = context.getSharedPreferences("clipSyncCache", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putString(
            key,
            (value.takeIf { it != null } ?: isDarkMode).toString())
    }
}

fun getTheme(context: Context) {
    val sharedPreferences = context.getSharedPreferences("clipSyncCache", Context.MODE_PRIVATE)
    isDarkMode = sharedPreferences.getString(key, false.toString()).toBoolean()
}