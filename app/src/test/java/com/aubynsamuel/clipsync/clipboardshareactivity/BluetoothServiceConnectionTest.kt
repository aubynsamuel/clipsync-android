package com.aubynsamuel.clipsync.clipboardshareactivity

import android.content.ComponentName
import com.aubynsamuel.clipsync.activities.shareclipboard.BluetoothServiceConnection
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class BluetoothServiceConnectionTest {

    @Mock
    private lateinit var binder: BluetoothService.LocalBinder

    @Mock
    private lateinit var bluetoothService: BluetoothService

    private var onServiceConnectedCalled = false
    private var onServiceDisconnectedCalled = false
    private var connectedService: BluetoothService? = null

    private lateinit var serviceConnection: BluetoothServiceConnection

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        serviceConnection = BluetoothServiceConnection(
            onServiceConnected = { service ->
                onServiceConnectedCalled = true
                connectedService = service
            },
            onServiceDisconnected = {
                onServiceDisconnectedCalled = true
            }
        )
    }

    @Test
    fun `onServiceConnected calls callback with service`() {
        // Given
        val componentName = mock<ComponentName>()
        whenever(binder.getService()).thenReturn(bluetoothService)

        // When
        serviceConnection.onServiceConnected(componentName, binder)

        // Then
        assertEquals(true, onServiceConnectedCalled)
        assertEquals(bluetoothService, connectedService)
    }

    @Test
    fun `onServiceDisconnected calls callback`() {
        // Given
        val componentName = mock<ComponentName>()

        // When
        serviceConnection.onServiceDisconnected(componentName)

        // Then
        assertEquals(true, onServiceDisconnectedCalled)
    }
}