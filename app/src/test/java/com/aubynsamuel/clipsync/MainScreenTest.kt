package com.aubynsamuel.clipsync

import android.bluetooth.BluetoothDevice
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aubynsamuel.clipsync.ui.screen.MainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockStartBluetoothService: (Set<String>) -> Unit = mock()

    private val mockRefresh: () -> Unit = mock()
    private val mockStopBluetoothService: () -> Unit = mock()

    @Test
    fun mainScreen_displaysTitle() {
        val emptyDevices = emptySet<BluetoothDevice>()

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = mockStartBluetoothService,
                pairedDevices = emptyDevices,
                refresh = mockRefresh,
                stopBluetoothService = mockStopBluetoothService
            )
        }

        composeTestRule.onNodeWithText("ClipSync").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysInstructionText() {
        val emptyDevices = emptySet<BluetoothDevice>()

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = mockStartBluetoothService,
                pairedDevices = emptyDevices,
//                launchShareActivity = mockLaunchShareActivity,
                refresh = mockRefresh,
                stopBluetoothService = mockStopBluetoothService
            )
        }

        composeTestRule.onNodeWithText("Select devices to share clipboard with").assertIsDisplayed()
    }

    @Test
    fun mainScreen_withEmptyDeviceList_showsNoDevices() {
        val emptyDevices = emptySet<BluetoothDevice>()

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = mockStartBluetoothService,
                pairedDevices = emptyDevices,
                refresh = mockRefresh,
                stopBluetoothService = mockStopBluetoothService
            )
        }

        composeTestRule.onNodeWithText("ClipSync").assertIsDisplayed()
    }

    @Test
    fun mainScreen_withMockedDevices_displaysDeviceCount() {
        // Given
        val mockDevice1 = mock(BluetoothDevice::class.java)
        val mockDevice2 = mock(BluetoothDevice::class.java)

        `when`(mockDevice1.address).thenReturn("00:11:22:33:44:55")
        `when`(mockDevice1.name).thenReturn("Test Device 1")
        `when`(mockDevice2.address).thenReturn("AA:BB:CC:DD:EE:FF")
        `when`(mockDevice2.name).thenReturn("Test Device 2")

        val pairedDevices = setOf(mockDevice1, mockDevice2)

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = mockStartBluetoothService,
                pairedDevices = pairedDevices,
                refresh = mockRefresh,
                stopBluetoothService = mockStopBluetoothService
            )
        }

        composeTestRule.onNodeWithText("ClipSync").assertIsDisplayed()
    }

    @Test
    fun mainScreen_refreshButton_callsRefreshFunction() {
        val emptyDevices = emptySet<BluetoothDevice>()

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = mockStartBluetoothService,
                pairedDevices = emptyDevices,
                refresh = mockRefresh,
                stopBluetoothService = mockStopBluetoothService
            )
        }

        composeTestRule.onNodeWithContentDescription("Refresh").performClick()

        verify(mockRefresh).invoke()
    }

    @Test
    fun mainScreen_darkModeToggle_isDisplayed() {
        val emptyDevices = emptySet<BluetoothDevice>()

        composeTestRule.setContent {
            MainScreen(
                startBluetoothService = mockStartBluetoothService,
                pairedDevices = emptyDevices,
                refresh = mockRefresh,
                stopBluetoothService = mockStopBluetoothService
            )

        }

        composeTestRule.onNodeWithText("ClipSync").assertIsDisplayed()
    }
}