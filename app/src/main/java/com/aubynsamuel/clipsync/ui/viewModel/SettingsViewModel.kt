package com.aubynsamuel.clipsync.ui.viewModel

import androidx.lifecycle.ViewModel
import com.aubynsamuel.clipsync.core.SettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val settingsPrefs: SettingsPreferences) : ViewModel() {
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _autoCopy = MutableStateFlow(true)
    val autoCopy: StateFlow<Boolean> = _autoCopy

    init {
        getSettings()
    }

    private fun getSettings() {
        _autoCopy.value = settingsPrefs.getAutoCopy()
        _isDarkMode.value = settingsPrefs.getTheme()
    }

    fun toggleAutoCopy() {
        settingsPrefs.toggleAutoCopy(!_autoCopy.value)
        getSettings()
    }

    fun switchTheme() {
        settingsPrefs.changeTheme(!_isDarkMode.value)
        getSettings()
    }

    fun resetSettings() {
        settingsPrefs.toggleAutoCopy(true)
        settingsPrefs.changeTheme(false)
        getSettings()
    }
}