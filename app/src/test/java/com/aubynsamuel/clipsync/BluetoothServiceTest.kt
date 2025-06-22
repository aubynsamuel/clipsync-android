package com.aubynsamuel.clipsync

/**
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.bluetooth.SharingResult
import com.aubynsamuel.clipsync.core.Essentials
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.timeout
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
@ExperimentalCoroutinesApi
class BluetoothServiceTest {

@Mock
private lateinit var bluetoothManager: BluetoothManager

@Mock
private lateinit var bluetoothAdapter: BluetoothAdapter

@Mock
private lateinit var serverSocket: BluetoothServerSocket

@Mock
private lateinit var clientSocket: BluetoothSocket

@Mock
private lateinit var remoteDevice: BluetoothDevice

private lateinit var controller: ServiceController<BluetoothService>
private lateinit var service: BluetoothService
private lateinit var context: Context

private val testUuid: UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
private val testDeviceAddress = "00:11:22:33:AA:BB"

@Before
fun setUp() {
MockitoAnnotations.openMocks(this)

// Get the real application context from Robolectric
context = ApplicationProvider.getApplicationContext<Context>()

// Mock the system services
val shadowApplication = ShadowApplication.getInstance()
shadowApplication.setSystemService(Context.BLUETOOTH_SERVICE, bluetoothManager)

// Setup mocks
`when`(bluetoothManager.adapter).thenReturn(bluetoothAdapter)
`when`(bluetoothAdapter.listenUsingRfcommWithServiceRecord(any(), any())).thenReturn(
serverSocket
)
`when`(bluetoothAdapter.getRemoteDevice(testDeviceAddress)).thenReturn(remoteDevice)

// Mock permissions - grant by default
shadowApplication.grantPermissions(Manifest.permission.BLUETOOTH_CONNECT)

// Create service controller and service
controller = Robolectric.buildService(BluetoothService::class.java)
service = controller.create().get()

// Reset Essentials state
Essentials.addresses = arrayOf()
Essentials.serviceStarted = false
}

@After
fun tearDown() {
// Clean up
controller.destroy()
Essentials.addresses = arrayOf()
Essentials.serviceStarted = false
}

@Test
fun `onStartCommand starts bluetooth server`() {
val intent = Intent().putExtra("SELECTED_DEVICES", arrayOf(testDeviceAddress))
controller.withIntent(intent).startCommand(0, 1)

// Give some time for the coroutine to execute
Thread.sleep(100)

verify(bluetoothAdapter).listenUsingRfcommWithServiceRecord("ClipSync", testUuid)
}

@Test
fun `shareClipboard returns NO_SELECTED_DEVICES when addresses are empty`() = runTest {
// Ensure no devices are selected
Essentials.addresses = arrayOf()
service.updateSelectedDevices(selectedDeviceAddresses.toTypedArray())

val result = service.shareClipboard("test")
Assert.assertEquals(SharingResult.NO_SELECTED_DEVICES, result)
}

@Test
fun `shareClipboard returns PERMISSION_NOT_GRANTED when permission is missing`() = runTest {
// Deny the permission
val shadowApplication = ShadowApplication.getInstance()
shadowApplication.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)

Essentials.addresses = arrayOf(testDeviceAddress)
service.updateSelectedDevices(selectedDeviceAddresses.toTypedArray())

val result = service.shareClipboard("test clipboard")
Assert.assertEquals(SharingResult.PERMISSION_NOT_GRANTED, result)
}

@Test
fun `shareClipboard returns SUCCESS on successful send`() = runTest {
val outputStream = ByteArrayOutputStream()
`when`(remoteDevice.createRfcommSocketToServiceRecord(testUuid)).thenReturn(clientSocket)
`when`(clientSocket.outputStream).thenReturn(outputStream)

Essentials.addresses = arrayOf(testDeviceAddress)
service.updateSelectedDevices(selectedDeviceAddresses.toTypedArray())

val result = service.shareClipboard("hello")

Assert.assertEquals(SharingResult.SUCCESS, result)

// Verify the JSON was written correctly
val sentData = outputStream.toString().trim()
val json = JSONObject(sentData)
Assert.assertEquals("hello", json.getString("clip"))

verify(clientSocket).connect()
verify(clientSocket).close()
}

@Test
fun `shareClipboard returns SENDING_ERROR on connection failure`() = runTest {
`when`(remoteDevice.createRfcommSocketToServiceRecord(testUuid)).thenReturn(clientSocket)
`when`(clientSocket.connect()).thenThrow(IOException("Connection failed!"))

Essentials.addresses = arrayOf(testDeviceAddress)
service.updateSelectedDevices(selectedDeviceAddresses.toTypedArray())

val result = service.shareClipboard("test")
Assert.assertEquals(SharingResult.SENDING_ERROR, result)
}

@Test
fun `server handles incoming connection and parses data`() {
val testMessage = "{\"clip\":\"test clip data from another device\"}\n"
val inputStream = ByteArrayInputStream(testMessage.toByteArray())
val incomingSocket: BluetoothSocket = mock {
on { this.inputStream } doReturn inputStream
}

// Mock serverSocket.accept() to return the incoming socket once, then throw exception to break the loop
`when`(serverSocket.accept())
.thenReturn(incomingSocket)
.thenThrow(IOException("Server stopped"))

// Start the service to initialize the server
controller.startCommand(0, 1)

// Give the background thread time to run and process the connection
Thread.sleep(500)

// Verify the socket was closed after processing
verify(incomingSocket, timeout(1000)).close()
}

@Test
fun `updateSelectedDevices updates addresses correctly`() = runTest {
val testAddresses = arrayOf("11:22:33:44:55:66", "AA:BB:CC:DD:EE:FF")
Essentials.addresses = testAddresses

service.updateSelectedDevices(selectedDeviceAddresses.toTypedArray())

// We can't directly access selectedDeviceAddresses, but we can test the behavior
// by trying to share clipboard - it should not return NO_SELECTED_DEVICES
// Mock the required dependencies for shareClipboard
val mockDevice1 = mock<BluetoothDevice>()
val mockDevice2 = mock<BluetoothDevice>()
val mockSocket1 = mock<BluetoothSocket>()
val mockSocket2 = mock<BluetoothSocket>()
val outputStream1 = ByteArrayOutputStream()
val outputStream2 = ByteArrayOutputStream()

`when`(bluetoothAdapter.getRemoteDevice(testAddresses[0])).thenReturn(mockDevice1)
`when`(bluetoothAdapter.getRemoteDevice(testAddresses[1])).thenReturn(mockDevice2)
`when`(mockDevice1.createRfcommSocketToServiceRecord(testUuid)).thenReturn(mockSocket1)
`when`(mockDevice2.createRfcommSocketToServiceRecord(testUuid)).thenReturn(mockSocket2)
`when`(mockSocket1.outputStream).thenReturn(outputStream1)
`when`(mockSocket2.outputStream).thenReturn(outputStream2)

val result = service.shareClipboard("test")
// Should not be NO_SELECTED_DEVICES since we have addresses
assert(result != SharingResult.NO_SELECTED_DEVICES)
}

@Test
fun `service sets serviceStarted flag in onCreate`() {
// Reset the flag first to test the actual behavior
Essentials.serviceStarted = false

// Create a new service to trigger onCreate
val newController = Robolectric.buildService(BluetoothService::class.java)
val newService = newController.create().get()

// Check if flag is set after onCreate
Assert.assertEquals(true, Essentials.serviceStarted)

// Clean up
newController.destroy()
}

@Test
fun `service clears serviceStarted flag in onDestroy`() {
controller.destroy()
Assert.assertEquals(false, Essentials.serviceStarted)
}
}
 **/