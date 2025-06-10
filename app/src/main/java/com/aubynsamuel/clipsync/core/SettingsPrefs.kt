package com.aubynsamuel.clipsync.core

import android.content.Context
import androidx.core.content.edit

const val autoCopyPrefsKey = "autoCopyPrefs"

fun setAutoCopy(context: Context, value: Boolean? = null) {
    Essentials.autoCopy = !Essentials.autoCopy
    val sharedPreferences = context.getSharedPreferences("clipSyncCache", Context.MODE_PRIVATE)
    sharedPreferences.edit {
        putString(
            autoCopyPrefsKey,
            (value.takeIf { it != null } ?: Essentials.autoCopy).toString()
        )
    }
}

fun getAutoCopy(context: Context) {
    val sharedPreferences = context.getSharedPreferences("clipSyncCache", Context.MODE_PRIVATE)
    val isCopy = sharedPreferences.getString(autoCopyPrefsKey, false.toString())
    Essentials.autoCopy = isCopy.toBoolean()
}