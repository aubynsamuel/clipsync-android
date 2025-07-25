package com.aubynsamuel.clipsync
//
//import android.content.ComponentName
//import com.aubynsamuel.clipsync.bluetooth.BluetoothService
//import com.aubynsamuel.clipsync.bluetooth.BluetoothServiceConnection
//import junit.framework.TestCase
//import org.junit.Before
//import org.junit.Test
//import org.mockito.Mock
//import org.mockito.MockitoAnnotations
//import org.mockito.kotlin.mock
//import org.mockito.kotlin.whenever
//
//class BluetoothServiceConnectionTest {
//
//    @Mock
//    private lateinit var binder: BluetoothService.LocalBinder
//
//    @Mock
//    private lateinit var bluetoothService: BluetoothService
//
//    private var onServiceConnectedCalled = false
//    private var onServiceDisconnectedCalled = false
//    private var connectedService: BluetoothService? = null
//
//    private lateinit var serviceConnection: BluetoothServiceConnection
//
//    @Before
//    fun setup() {
//        MockitoAnnotations.openMocks(this)
//
//        serviceConnection = BluetoothServiceConnection(
//            onServiceConnected = { service ->
//                onServiceConnectedCalled = true
//                connectedService = service
//            },
//            onServiceDisconnected = {
//                onServiceDisconnectedCalled = true
//            }
//        )
//    }
//
//    @Test
//    fun `onServiceConnected calls callback with service`() {
//        // Given
//        val componentName = mock<ComponentName>()
//        whenever(binder.getService()).thenReturn(bluetoothService)
//
//        // When
//        serviceConnection.onServiceConnected(componentName, binder)
//
//        // Then
//        TestCase.assertEquals(true, onServiceConnectedCalled)
//        TestCase.assertEquals(bluetoothService, connectedService)
//    }
//
//    @Test
//    fun `onServiceDisconnected calls callback`() {
//        // Given
//        val componentName = mock<ComponentName>()
//
//        // When
//        serviceConnection.onServiceDisconnected(componentName)
//
//        // Then
//        TestCase.assertEquals(true, onServiceDisconnectedCalled)
//    }
//}