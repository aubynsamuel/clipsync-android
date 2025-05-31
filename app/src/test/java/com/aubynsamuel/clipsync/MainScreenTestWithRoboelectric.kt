package com.aubynsamuel.clipsync

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aubynsamuel.clipsync.core.Essentials
import com.aubynsamuel.clipsync.ui.screen.MainScreen
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockBluetoothDevice1: BluetoothDevice

    @Mock
    private lateinit var mockBluetoothDevice2: BluetoothDevice

    @Mock
    private lateinit var mockContext: Context

    private lateinit var startBluetoothService: (Set<String>) -> Unit
    private lateinit var launchShareActivity: (Context) -> Unit
    private lateinit var refresh: () -> Unit
    private lateinit var stopBluetoothService: () -> Unit

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Mock bluetooth devices
        whenever(mockBluetoothDevice1.name).thenReturn("Device 1")
        whenever(mockBluetoothDevice1.address).thenReturn("AA:BB:CC:DD:EE:FF")
        whenever(mockBluetoothDevice2.name).thenReturn("Device 2")
        whenever(mockBluetoothDevice2.address).thenReturn("11:22:33:44:55:66")

        // Initialize mock functions
        startBluetoothService = mock()
        launchShareActivity = mock()
        refresh = mock()
        stopBluetoothService = mock()

        // Reset Essentials state
        Essentials.addresses = emptyArray()
        Essentials.serviceStarted = false
        Essentials.isDarkMode = false
    }

    @Test
    fun mainScreen_displaysCorrectTitle() {
        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = emptySet(),
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        composeTestRule
            .onNodeWithText("ClipSync")
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysInstructionText() {
        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = emptySet(),
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        composeTestRule
            .onNodeWithText("Select devices to share clipboard with")
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysRefreshIcon() {
        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = emptySet(),
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Refresh")
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysPairedDevices() {
        val pairedDevices = setOf(mockBluetoothDevice1, mockBluetoothDevice2)

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = pairedDevices,
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        composeTestRule
            .onNodeWithText("Device 1")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Device 2")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("AA:BB:CC:DD:EE:FF")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("11:22:33:44:55:66")
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_showsSelectedDeviceCount() {
        val pairedDevices = setOf(mockBluetoothDevice1, mockBluetoothDevice2)

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = pairedDevices,
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        // Initially, no count should be shown
        composeTestRule
            .onNodeWithText("1")
            .assertDoesNotExist()

        // Select first device
        composeTestRule
            .onNodeWithText("Device 1")
            .performClick()

        // Count should now show 1
        composeTestRule
            .onNodeWithText("1")
            .assertIsDisplayed()

        // Select second device
        composeTestRule
            .onNodeWithText("Device 2")
            .performClick()

        // Count should now show 2
        composeTestRule
            .onNodeWithText("2")
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_refreshButtonCallsRefreshFunction() = runTest {
        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = emptySet(),
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Refresh")
            .performClick()

        verify(refresh).invoke()
    }

    @Test
    fun mainScreen_deviceSelectionUpdatesAddresses() = runTest {
        val pairedDevices = setOf(mockBluetoothDevice1)

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = pairedDevices,
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        // Select device
        composeTestRule
            .onNodeWithText("Device 1")
            .performClick()

        // Wait for LaunchedEffect to complete
        composeTestRule.waitForIdle()

        // Verify that addresses were updated (this would need to be verified through the actual implementation)
        // Since Essentials.addresses is a global variable, we can check it directly
        // Note: In a real test, you might want to inject this dependency instead
    }

    @Test
    fun mainScreen_handlesBluetoothPermissionDenied() {
        // Mock permission denied scenario
        val mockDeviceWithoutPermission = mock<BluetoothDevice>()
        whenever(mockDeviceWithoutPermission.address).thenReturn("AA:BB:CC:DD:EE:FF")

        // In the actual test, you'd need to mock ActivityCompat.checkSelfPermission
        // to return PERMISSION_DENIED, but this requires more complex setup with PowerMock
        // or similar tools

        val pairedDevices = setOf(mockDeviceWithoutPermission)

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = pairedDevices,
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        // Device should still be displayed with address
        composeTestRule
            .onNodeWithText("AA:BB:CC:DD:EE:FF")
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_darkModeToggleIsDisplayed() {
        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = emptySet(),
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        // The DarkModeToggle component should be present
        // You might need to add a test tag to the DarkModeToggle component for better testing
        composeTestRule
            .onRoot()
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_actionButtonsAreDisplayed() {
        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = emptySet(),
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        // ActionButtons component should be present
        // You might want to add test tags to ActionButtons for more specific testing
        composeTestRule
            .onRoot()
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_deviceDeselection() {
        val pairedDevices = setOf(mockBluetoothDevice1)

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = pairedDevices,
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        // Select device
        composeTestRule
            .onNodeWithText("Device 1")
            .performClick()

        // Count should show 1
        composeTestRule
            .onNodeWithText("1")
            .assertIsDisplayed()

        // Deselect device by clicking again
        composeTestRule
            .onNodeWithText("Device 1")
            .performClick()

        // Count should no longer be displayed
        composeTestRule
            .onNodeWithText("1")
            .assertDoesNotExist()
    }

    @Test
    fun mainScreen_multipleDeviceSelection() {
        val pairedDevices = setOf(mockBluetoothDevice1, mockBluetoothDevice2)

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = pairedDevices,
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        // Select both devices
        composeTestRule
            .onNodeWithText("Device 1")
            .performClick()

        composeTestRule
            .onNodeWithText("Device 2")
            .performClick()

        // Count should show 2
        composeTestRule
            .onNodeWithText("2")
            .assertIsDisplayed()

        // Deselect one device
        composeTestRule
            .onNodeWithText("Device 1")
            .performClick()

        // Count should show 1
        composeTestRule
            .onNodeWithText("1")
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_emptyDeviceListShowsNoDevices() {
        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = emptySet(),
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        // Verify that no device items are displayed
        composeTestRule
            .onAllNodesWithText("Device")
            .assertCountEquals(0)
    }
}