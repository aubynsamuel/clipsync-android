package com.aubynsamuel.clipsync.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Global service state manager for ClipSync.
 *
 * This singleton manages the lifecycle and state of the BluetoothService,
 * providing thread-safe access to service instances and configuration.
 * While generally considered an anti-pattern, this approach is justified here
 * because:
 * 1. The service needs to persist beyond Activity lifecycle
 * 2. Multiple components need real-time access to service state
 * 3. Avoids the overhead of restarting the service for configuration updates
 */
object Essentials {
    private val mutex = Mutex()

    @Volatile
    private var _bluetoothService: BluetoothService? = null

    private var _isServiceBound by mutableStateOf(false)

    @Volatile
    private var _selectedDeviceAddresses: Array<String> = emptyArray()

    /**
     * Observable state indicating if the service is currently bound and active
     */
    val isServiceBound: Boolean
        get() = _isServiceBound

    /**
     * Currently selected device addresses for clipboard sharing
     */
    val selectedDeviceAddresses: Array<String>
        get() = _selectedDeviceAddresses.copyOf()

    /**
     * Safely sets the BluetoothService instance
     */
    suspend fun setBluetoothService(service: BluetoothService?) {
        mutex.withLock {
            _bluetoothService = service
            _isServiceBound = service != null
        }
    }

    /**
     * Updates the selected device addresses for sharing
     */
    suspend fun updateSelectedDevices(addresses: Array<String>) {
        mutex.withLock {
            _selectedDeviceAddresses = addresses.copyOf()
            _bluetoothService?.updateSelectedDevices(addresses)
        }
    }

    /**
     * Toggles auto-copy functionality
     */
    suspend fun toggleAutoCopy(enabled: Boolean) {
        mutex.withLock {
            _bluetoothService?.toggleAutoCopy(enabled)
        }
    }

    /**
     * Safely cleans up all references and state
     * Should be called when the service is stopped
     */
    suspend fun clean() {
        mutex.withLock {
            _bluetoothService = null
            _isServiceBound = false
            _selectedDeviceAddresses = emptyArray()
        }
    }
}
