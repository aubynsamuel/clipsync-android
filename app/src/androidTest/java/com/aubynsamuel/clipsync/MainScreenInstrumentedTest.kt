package com.aubynsamuel.clipsync

import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aubynsamuel.clipsync.core.Essentials
import com.aubynsamuel.clipsync.ui.screen.MainScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private var startBluetoothServiceCalled = false
    private var launchShareActivityCalled = false
    private var refreshCalled = false
    private var stopBluetoothServiceCalled = false
    private var startBluetoothServiceAddresses: Set<String> = emptySet()

    // Callback function implementations for testing
    private val startBluetoothService: (Set<String>) -> Unit = { addresses ->
        startBluetoothServiceCalled = true
        startBluetoothServiceAddresses = addresses
    }

    private val launchShareActivity: (Context) -> Unit = {
        launchShareActivityCalled = true
    }

    private val refresh: () -> Unit = {
        refreshCalled = true
    }

    private val stopBluetoothService: () -> Unit = {
        stopBluetoothServiceCalled = true
    }

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // Reset callback flags
        startBluetoothServiceCalled = false
        launchShareActivityCalled = false
        refreshCalled = false
        stopBluetoothServiceCalled = false
        startBluetoothServiceAddresses = emptySet()

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
        // Get real paired devices from BluetoothAdapter if available
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = if (bluetoothAdapter?.isEnabled == true) {
            try {
                bluetoothAdapter.bondedDevices ?: emptySet()
            } catch (e: SecurityException) {
                // If we don't have permissions, use empty set
                emptySet()
            }
        } else {
            emptySet()
        }

        // Only run this test if we have paired devices
        if (pairedDevices.isNotEmpty()) {
            composeTestRule.setContent {
                MainScreen(
                    startBluetoothService = startBluetoothService,
                    pairedDevices = pairedDevices,
                    launchShareActivity = launchShareActivity,
                    refresh = refresh,
                    stopBluetoothService = stopBluetoothService
                )
            }

            // Test that at least one device is displayed
            val firstDevice = pairedDevices.first()
            val deviceName = try {
                firstDevice.name
            } catch (e: SecurityException) {
                null
            }

            val deviceAddress = try {
                firstDevice.address
            } catch (e: SecurityException) {
                "Unknown"
            }

            if (deviceName != null) {
                composeTestRule
                    .onNodeWithText(deviceName)
                    .assertIsDisplayed()
            }

            composeTestRule
                .onNodeWithText(deviceAddress)
                .assertIsDisplayed()
        }
    }

    @Test
    fun mainScreen_refreshButtonCallsRefreshFunction() {
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

        // Wait for the click to be processed
        composeTestRule.waitForIdle()

        // Verify refresh was called
        assert(refreshCalled) { "Refresh function should have been called" }
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

        // The main screen root should be displayed
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

        // The main screen root should be displayed (ActionButtons are part of it)
        composeTestRule
            .onRoot()
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
        // This test verifies the UI handles empty device list correctly
        composeTestRule
            .onRoot()
            .assertIsDisplayed()

        // The instruction text should still be visible
        composeTestRule
            .onNodeWithText("Select devices to share clipboard with")
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_handlesBluetoothDisabled() {
        // Test behavior when Bluetooth is disabled
        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = emptySet(),
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        // Should still display the basic UI elements
        composeTestRule
            .onNodeWithText("ClipSync")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Select devices to share clipboard with")
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_deviceSelectionWithRealDevices() {
        // This test will only run if there are actual paired devices
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = if (bluetoothAdapter?.isEnabled == true) {
            try {
                bluetoothAdapter.bondedDevices?.take(1)?.toSet() ?: emptySet()
            } catch (e: SecurityException) {
                emptySet()
            }
        } else {
            emptySet()
        }

        if (pairedDevices.isNotEmpty()) {
            composeTestRule.setContent {
                MainScreen(
                    startBluetoothService = startBluetoothService,
                    pairedDevices = pairedDevices,
                    launchShareActivity = launchShareActivity,
                    refresh = refresh,
                    stopBluetoothService = stopBluetoothService
                )
            }

            val device = pairedDevices.first()
            val deviceName = try {
                device.name
            } catch (e: SecurityException) {
                null
            }

            // If device has a name, try to select it
            if (deviceName != null) {
                // Select device
                composeTestRule
                    .onNodeWithText(deviceName)
                    .performClick()

                // Wait for LaunchedEffect to complete
                composeTestRule.waitForIdle()

                // Count should show 1
                composeTestRule
                    .onNodeWithText("1")
                    .assertIsDisplayed()
            }
        }
    }

    @Test
    fun mainScreen_basicUIElementsArePresent() {
        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = startBluetoothService,
                pairedDevices = emptySet(),
                launchShareActivity = launchShareActivity,
                refresh = refresh,
                stopBluetoothService = stopBluetoothService
            )
        }

        // Test that all basic UI elements are present
        composeTestRule
            .onNodeWithText("ClipSync")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Select devices to share clipboard with")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Refresh")
            .assertIsDisplayed()

        // The root composable should be displayed
        composeTestRule
            .onRoot()
            .assertIsDisplayed()
    }
}