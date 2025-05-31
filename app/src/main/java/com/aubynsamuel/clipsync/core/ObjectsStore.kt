package com.aubynsamuel.clipsync.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Essentials {
    var serviceStarted by mutableStateOf(false)
    var isDarkMode by mutableStateOf(false)

    @Volatile
    var addresses: Array<String> = emptyArray()
}
