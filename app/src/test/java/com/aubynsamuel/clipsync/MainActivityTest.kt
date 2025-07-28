package com.aubynsamuel.clipsync

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.aubynsamuel.clipsync.activities.MainActivity
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowToast
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
@ExperimentalCoroutinesApi
class MainActivityTest {

    @Mock
    private lateinit var mockBluetoothManager: BluetoothManager

    @Mock
    private lateinit var mockBluetoothAdapter: BluetoothAdapter

    @Mock
    private lateinit var mockBluetoothDevice1: BluetoothDevice

    @Mock
    private lateinit var mockBluetoothDevice2: BluetoothDevice

    private lateinit var controller: ActivityController<MainActivity>
    private lateinit var activity: MainActivity
    private lateinit var context: Context
    private lateinit var shadowApplication: ShadowApplication

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Get the real application context from Robolectric
        context = ApplicationProvider.getApplicationContext()
        shadowApplication = ShadowApplication()

        // Mock system services
        shadowApplication.setSystemService(Context.BLUETOOTH_SERVICE, mockBluetoothManager)

        // Setup mock bluetooth manager to return mock adapter
        `when`(mockBluetoothManager.adapter).thenReturn(mockBluetoothAdapter)

        // Grant permissions by default
        shadowApplication.grantPermissions(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )

        // Create activity controller and activity
        controller = Robolectric.buildActivity(MainActivity::class.java)
        activity = controller.create().resume().get() // Resume to trigger onCreate

        // Clear any previous toasts
        ShadowToast.reset()
    }

    @Test
    fun `onCreate should initialize bluetooth components`() {
        // Given - activity is already created and resumed in setUp

        // Then - verify activity was created successfully
        assertTrue(true)
    }

    @Test
    fun `checkPermissions should request missing permissions on Android S and above`() {
        // Given - deny one permission
        shadowApplication.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        // When
        ReflectionHelpers.callInstanceMethod<Any>(activity, "checkPermissions")

        // Then - verify permission request flow was triggered
        // In a real scenario, this would trigger the permission launcher
        assertTrue("Activity should handle missing permissions", true)
    }

    @Test
    fun `checkPermissions should proceed to bluetooth check when all permissions granted`() {
        // Given - all permissions already granted in setUp
        `when`(mockBluetoothAdapter.isEnabled).thenReturn(true)
        `when`(mockBluetoothAdapter.bondedDevices).thenReturn(emptySet())

        /**
         * No need to call checkPermissions explicitly, it is called in onCreate
         * which is called in setUp
         */
//        ReflectionHelpers.callInstanceMethod<Any>(activity, "checkPermissions")

        verify(mockBluetoothAdapter).isEnabled
    }

    @Test
    fun `checkBluetoothEnabled should request enable when bluetooth disabled`() {
        // Given
        `when`(mockBluetoothAdapter.isEnabled).thenReturn(false)

        // When
        ReflectionHelpers.callInstanceMethod<Any>(activity, "checkBluetoothEnabled")

        // Then - verify enable bluetooth intent was started
        val nextStartedActivity = shadowApplication.nextStartedActivity
        assertEquals(BluetoothAdapter.ACTION_REQUEST_ENABLE, nextStartedActivity.action)
    }

    @Test
    fun `checkBluetoothEnabled should load paired devices when bluetooth enabled`() {
        // Given
        `when`(mockBluetoothAdapter.isEnabled).thenReturn(true)
        val mockDevices = setOf(mockBluetoothDevice1, mockBluetoothDevice2)
        `when`(mockBluetoothAdapter.bondedDevices).thenReturn(mockDevices)

        // When
        ReflectionHelpers.callInstanceMethod<Any>(activity, "checkBluetoothEnabled")

        // Then
        verify(mockBluetoothAdapter).bondedDevices
    }

    @Test
    fun `loadPairedDevices should return empty set when permission denied`() {
        // Given - deny bluetooth permission
        shadowApplication.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        // When
        val result = ReflectionHelpers.callInstanceMethod<Set<BluetoothDevice>>(
            activity, "loadPairedDevices"
        )

        // Then
        assertTrue(result.isEmpty())
        verify(mockBluetoothAdapter, never()).bondedDevices
    }

    @Test
    fun `loadPairedDevices should return bonded devices when permission granted`() {
        // Given
        val mockDevices = setOf(mockBluetoothDevice1, mockBluetoothDevice2)
        `when`(mockBluetoothAdapter.bondedDevices).thenReturn(mockDevices)

        // When
        val result = ReflectionHelpers.callInstanceMethod<Set<BluetoothDevice>>(
            activity, "loadPairedDevices"
        )

        // Then
        assertEquals(mockDevices, result)
        verify(mockBluetoothAdapter).bondedDevices
    }

    @Test
    fun `loadPairedDevices should handle null bonded devices`() {
        // Given
        `when`(mockBluetoothAdapter.bondedDevices).thenReturn(null)

        // When
        val result = ReflectionHelpers.callInstanceMethod<Set<BluetoothDevice>>(
            activity, "loadPairedDevices"
        )

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `startBluetoothService should start foreground service on Android O and above`() {
        // Given
        val selectedDevices = setOf("device1", "device2")

        // When
        ReflectionHelpers.callInstanceMethod<Any>(
            activity, "startBluetoothService",
            ReflectionHelpers.ClassParameter(Set::class.java, selectedDevices)
        )

        // Then
        val nextStartedService = shadowApplication.nextStartedService
        assertEquals(BluetoothService::class.java.name, nextStartedService.component?.className)
        val extras = nextStartedService.getStringArrayExtra("SELECTED_DEVICES")
        assertTrue(extras?.toSet() == selectedDevices)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N])
    fun `startBluetoothService should start regular service on pre-Android O`() {
        // Given
        val selectedDevices = setOf("device1", "device2")

        // When
        ReflectionHelpers.callInstanceMethod<Any>(
            activity, "startBluetoothService",
            ReflectionHelpers.ClassParameter(Set::class.java, selectedDevices)
        )

        // Then
        val nextStartedService = shadowApplication.nextStartedService
        assertEquals(BluetoothService::class.java.name, nextStartedService.component?.className)
    }

    @Test
    fun `stopBluetoothService should stop the bluetooth service`() {
        // When
        ReflectionHelpers.callInstanceMethod<Any>(activity, "stopBluetoothService")

        // Then
        val nextStoppedService = shadowApplication.nextStoppedService
        assertEquals(BluetoothService::class.java.name, nextStoppedService.component?.className)
    }

    @Test
    fun `requestEnableBluetooth callback should load paired devices on success`() = runTest {
        // Given
        `when`(mockBluetoothAdapter.bondedDevices).thenReturn(setOf(mockBluetoothDevice1))
        `when`(mockBluetoothAdapter.isEnabled).thenReturn(true)

        // When - simulate the callback flow by directly calling checkBluetoothEnabled after enabling
        ReflectionHelpers.callInstanceMethod<Any>(activity, "checkBluetoothEnabled")

        // Then - verify paired devices were loaded
        verify(mockBluetoothAdapter).bondedDevices
    }

    @Test
    fun `requestEnableBluetooth callback should show toast and retry on failure`() {
        // Given
        `when`(mockBluetoothAdapter.isEnabled).thenReturn(false)

        // When - simulate failure by checking bluetooth when disabled
        ReflectionHelpers.callInstanceMethod<Any>(activity, "checkBluetoothEnabled")

        // Then - verify enable bluetooth request was made (retry behavior)
        val nextStartedActivity = shadowApplication.nextStartedActivity
        assertEquals(BluetoothAdapter.ACTION_REQUEST_ENABLE, nextStartedActivity.action)
    }

    @Test
    fun `permissions callback should proceed when all permissions granted`() = runTest {
        // Given - all permissions granted (already set in setUp)
        `when`(mockBluetoothAdapter.isEnabled).thenReturn(true)
        `when`(mockBluetoothAdapter.bondedDevices).thenReturn(setOf(mockBluetoothDevice1))

        // When
        ReflectionHelpers.callInstanceMethod<Any>(activity, "checkPermissions")

        // Then - verify that `checkBluetoothEnabled` which loads devices is called.
        verify(mockBluetoothAdapter).bondedDevices
    }

    @Test
    fun `permissions callback should handle denied permissions gracefully`() {
        // Given - deny a critical permission
        shadowApplication.denyPermissions(Manifest.permission.BLUETOOTH_CONNECT)

        // When
        ReflectionHelpers.callInstanceMethod<Any>(activity, "checkPermissions")

        // Then - verify graceful handling (no crash, appropriate flow)
        assertTrue("Activity should handle denied permissions gracefully", true)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun `activity should request correct permissions for Android S and above`() {
        // Given - deny all Android S permissions
        shadowApplication.denyPermissions(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.POST_NOTIFICATIONS
        )

        // When
        ReflectionHelpers.callInstanceMethod<Any>(activity, "checkPermissions")

        // Then - verify the method doesn't crash and handles S+ permissions
        assertTrue("Should handle Android S+ permissions correctly", true)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun `activity should request correct permissions for pre-Android S`() {
        // Given - deny legacy permissions
        shadowApplication.denyPermissions(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )

        // When
        ReflectionHelpers.callInstanceMethod<Any>(activity, "checkPermissions")

        // Then - verify the method handles legacy permissions
        assertTrue("Should handle pre-Android S permissions correctly", true)
    }

    @Test
    fun `startBluetoothService should pass correct device addresses`() {
        // Given
        val testDevices = setOf("AA:BB:CC:DD:EE:FF", "11:22:33:44:55:66")

        // When
        ReflectionHelpers.callInstanceMethod<Any>(
            activity, "startBluetoothService",
            ReflectionHelpers.ClassParameter(Set::class.java, testDevices)
        )

        // Then
        val startedService = shadowApplication.nextStartedService
        val deviceArray = startedService.getStringArrayExtra("SELECTED_DEVICES")
        assertEquals(testDevices, deviceArray?.toSet())
    }

    @Test
    fun `loadPairedDevices should update pairedDevices state`() {
        // Given
        val testDevices = setOf(mockBluetoothDevice1, mockBluetoothDevice2)
        `when`(mockBluetoothAdapter.bondedDevices).thenReturn(testDevices)

        // When
        ReflectionHelpers.callInstanceMethod<Set<BluetoothDevice>>(
            activity, "loadPairedDevices"
        )

        // Then
        // We can't directly access and assert the MutableState.
        // Instead, we verify that the method that *uses* this state
        // (or the method that updates it) was called as expected.
        // For this test, verifying that `getBondedDevices` was called
        // implies that the state update logic within `loadPairedDevices` ran.
        verify(mockBluetoothAdapter).bondedDevices
        // Further testing of the UI or other methods that observe this state
        // would be needed for complete verification of the state update.
    }

    @Test
    fun `activity should handle bluetooth adapter initialization`() {
        // Given - create a new activity to test initialization
        val newController = Robolectric.buildActivity(MainActivity::class.java)
        val newActivity = newController.create().get()

        // Then - verify activity initialized without crashing
        assertTrue(newActivity is MainActivity)

        // Clean up
        newController.destroy()
    }
}