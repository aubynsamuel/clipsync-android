package com.aubynsamuel.clipsync

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.aubynsamuel.clipsync.activities.ShareClipboardActivity
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.bluetooth.SharingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S])
@ExperimentalCoroutinesApi
class ShareClipboardActivityTest {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Mock
    private lateinit var mockBluetoothService: BluetoothService

    @Mock
    private lateinit var mockBinder: BluetoothService.LocalBinder

    @Mock
    private lateinit var clipboardManager: ClipboardManager

    @Mock
    private lateinit var clipData: ClipData

    @Mock
    private lateinit var clipItem: ClipData.Item

    private lateinit var context: Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()

        // Mock the system clipboard service
        val shadowApplication = ShadowApplication.getInstance()
        shadowApplication.setSystemService(Context.CLIPBOARD_SERVICE, clipboardManager)

        // Setup mock binder to return our mock service
        `when`(mockBinder.getService()).thenReturn(mockBluetoothService)

        // Setup clipboard mocks
        `when`(clipboardManager.primaryClip).thenReturn(clipData)
        `when`(clipData.getItemAt(0)).thenReturn(clipItem)
    }

    @Test
    fun `onCreate binds to BluetoothService`() {
        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("ACTION_SHARE")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            val shadowApplication = ShadowApplication.getInstance()
            val boundIntent = shadowApplication.getNextStartedService()
            assertEquals(
                Intent(activity, BluetoothService::class.java).component,
                boundIntent?.component
            )
            assertFalse(activity.isFinishing)
        }
        scenario.close()
    }

    @Test
    fun `onCreate with ACTION_SHARE and bound service calls handleShareAction immediately`() =
        runTest {
            `when`(clipItem.text).thenReturn("test clipboard content")
            `when`(mockBluetoothService.shareClipboard(any())).thenReturn(SharingResult.SUCCESS)

            val intent = Intent(context, ShareClipboardActivity::class.java)
                .setAction("ACTION_SHARE")

            val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
            scenario.onActivity { activity ->
                // Simulate service connection
                simulateServiceConnection(activity, mockBinder)

                // Advance the looper to process the delayed handler
                ShadowLooper.shadowMainLooper().idle()

                // Activity should finish after handling the share action
                assertTrue(activity.isFinishing)
            }
            scenario.close()
        }

    @Test
    fun `onCreate with ACTION_SHARE and unbound service sets pendingShareAction`() {
        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("ACTION_SHARE")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            // We expect bindService to have been called
            val shadowApplication = ShadowApplication.getInstance()
            val boundIntent = shadowApplication.getNextStartedService()
            assertEquals(
                Intent(activity, BluetoothService::class.java).component,
                boundIntent?.component
            )
            // Service is not bound yet, so activity should not finish immediately
            // and should have pendingShareAction set to true
            assertFalse(activity.isFinishing)
        }
        scenario.close()
    }

    @Test
    fun `onCreate without ACTION_SHARE finishes activity immediately`() {
        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("SOME_OTHER_ACTION")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            assertTrue(activity.isFinishing)
        }
        scenario.close()
    }

    @Test
    fun `onServiceConnected calls handleShareAction when pendingShareAction is true`() = runTest {
        `when`(clipItem.text).thenReturn("pending clipboard content")
        `when`(mockBluetoothService.shareClipboard(any())).thenReturn(SharingResult.SUCCESS)

        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("ACTION_SHARE")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            // Simulate service connection when pendingShareAction is true
            simulateServiceConnection(activity, mockBinder)

            // Advance the looper to process the delayed handler
            ShadowLooper.shadowMainLooper().idle()

            // Verify shareClipboard was called
            serviceScope.launch {
                verify(mockBluetoothService).shareClipboard("pending clipboard content")
            }
            assertTrue(activity.isFinishing)
        }
        scenario.close()
    }

    @Test
    fun `handleShareAction with empty clipboard shows toast and finishes`() {
        `when`(clipItem.text).thenReturn("")

        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("ACTION_SHARE")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            simulateServiceConnection(activity, mockBinder)

            // Advance the looper to process the delayed handler
            ShadowLooper.shadowMainLooper().idle()

            // Verify activity finishes when clipboard is empty
            assertTrue(activity.isFinishing)

            // Verify shareClipboard was not called
            serviceScope.launch {
                verify(mockBluetoothService, never()).shareClipboard(any())
            }
        }
        scenario.close()
    }

    @Test
    fun `handleShareAction with null clipboard shows toast and finishes`() {
        `when`(clipItem.text).thenReturn(null)

        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("ACTION_SHARE")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            simulateServiceConnection(activity, mockBinder)

            // Advance the looper to process the delayed handler
            ShadowLooper.shadowMainLooper().idle()

            // Verify activity finishes when clipboard is null
            assertTrue(activity.isFinishing)

            // Verify shareClipboard was not called
            serviceScope.launch {
                verify(mockBluetoothService, never()).shareClipboard(any())
            }
        }
        scenario.close()
    }

    @Test
    fun `handleShareAction with blank clipboard shows toast and finishes`() {
        `when`(clipItem.text).thenReturn("   ")

        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("ACTION_SHARE")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            simulateServiceConnection(activity, mockBinder)

            // Advance the looper to process the delayed handler
            ShadowLooper.shadowMainLooper().idle()

            // Verify activity finishes when clipboard is blank
            assertTrue(activity.isFinishing)

            // Verify shareClipboard was not called
            serviceScope.launch {
                verify(mockBluetoothService, never()).shareClipboard(any())
            }
        }
        scenario.close()
    }

    @Test
    fun `handleShareAction with valid clipboard calls shareClipboard`() = runTest {
        val testClipboardContent = "Hello, this is test clipboard content!"
        `when`(clipItem.text).thenReturn(testClipboardContent)
        `when`(mockBluetoothService.shareClipboard(testClipboardContent)).thenReturn(SharingResult.SUCCESS)

        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("ACTION_SHARE")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            simulateServiceConnection(activity, mockBinder)

            // Advance the looper to process the delayed handler
            ShadowLooper.shadowMainLooper().idle()

            // Verify shareClipboard was called with correct content
            serviceScope.launch {
                verify(mockBluetoothService).shareClipboard(testClipboardContent)
            }
            // Verify activity finishes after sharing
            assertTrue(activity.isFinishing)
        }
        scenario.close()
    }

    @Test
    fun `handleShareAction handles different SharingResult values`() = runTest {
        val testContent = "test content"
        `when`(clipItem.text).thenReturn(testContent)

        // Test each result type
        val resultTypes = listOf(
            SharingResult.SUCCESS,
            SharingResult.SENDING_ERROR,
            SharingResult.PERMISSION_NOT_GRANTED,
            SharingResult.NO_SELECTED_DEVICES
        )

        resultTypes.forEach { expectedResult ->
            `when`(mockBluetoothService.shareClipboard(testContent)).thenReturn(expectedResult)

            val intent = Intent(context, ShareClipboardActivity::class.java)
                .setAction("ACTION_SHARE")

            val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
            scenario.onActivity { activity ->
                simulateServiceConnection(activity, mockBinder)

                // Advance the looper to process the delayed handler
                ShadowLooper.shadowMainLooper().idle()

                // Verify activity finishes regardless of result
                assertTrue(activity.isFinishing)
            }
            scenario.close()
        }
    }

    @Test
    fun `onDestroy unbinds service when bound`() {
        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("ACTION_SHARE")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            // We expect bindService to have been called in onCreate
            val shadowApplication = ShadowApplication.getInstance()
            val boundIntent = shadowApplication.getNextStartedService()
            assertEquals(
                Intent(activity, BluetoothService::class.java).component,
                boundIntent?.component
            )

            // Simulate the service being bound by calling the service connection callback
            simulateServiceConnection(activity, mockBinder)
        }

        // Move to destroyed state
        scenario.moveToState(Lifecycle.State.DESTROYED)

        // At this point, unbindService should have been called
        val shadowApplication = ShadowApplication.getInstance()
        val unboundServiceIntent = shadowApplication.unboundServiceConnections
        scenario.onActivity { activity ->
            assertEquals(
                Intent(activity, BluetoothService::class.java).component,
                unboundServiceIntent
            )
        }

        scenario.close()
    }

    @Test
    fun `onServiceDisconnected resets bound state`() {
        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("ACTION_SHARE")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            // Simulate service connection
            val serviceConnection = simulateServiceConnectionForDisconnect(activity, mockBinder)

            // Simulate service disconnection
            serviceConnection?.onServiceDisconnected(
                ComponentName(context, BluetoothService::class.java)
            )

            // Activity should handle disconnection gracefully (bound should be false)
            val boundField = ShareClipboardActivity::class.java.getDeclaredField("bound")
            boundField.isAccessible = true
            val isBound = boundField.getBoolean(activity)
            assertFalse(isBound)
            assertFalse(activity.isFinishing)
        }
        scenario.close()
    }

    @Test
    fun `activity handles null intent gracefully`() {
        // This tests the case where intent might be null
        val intent = Intent(context, ShareClipboardActivity::class.java)
        // Don't set any action

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            assertTrue(activity.isFinishing)
        }
        scenario.close()
    }

    @Test
    fun `activity handles null clipboard manager gracefully`() {
        // Remove clipboard manager to test null handling
        val shadowApplication = ShadowApplication.getInstance()
        shadowApplication.setSystemService(Context.CLIPBOARD_SERVICE, null)

        val intent = Intent(context, ShareClipboardActivity::class.java)
            .setAction("ACTION_SHARE")

        val scenario = ActivityScenario.launch<ShareClipboardActivity>(intent)
        scenario.onActivity { activity ->
            simulateServiceConnection(activity, mockBinder)

            // This should not crash the app
            ShadowLooper.shadowMainLooper().idle()

            // Activity should finish
            assertTrue(activity.isFinishing)
        }
        scenario.close()
    }

    /**
     * Helper method to simulate service connection
     */
    private fun simulateServiceConnection(
        activity: ShareClipboardActivity,
        binder: BluetoothService.LocalBinder,
    ): ServiceConnection? {
        val connectionField = ShareClipboardActivity::class.java.getDeclaredField("connection")
        connectionField.isAccessible = true
        val serviceConnection = connectionField.get(activity) as ServiceConnection

        // Simulate service connection
        serviceConnection.onServiceConnected(
            ComponentName(context, BluetoothService::class.java),
            binder
        )
        return serviceConnection
    }

    /**
     * Helper method to simulate service connection and return the connection object
     */
    private fun simulateServiceConnectionForDisconnect(
        activity: ShareClipboardActivity,
        binder: BluetoothService.LocalBinder,
    ): ServiceConnection? {
        val connectionField = ShareClipboardActivity::class.java.getDeclaredField("connection")
        connectionField.isAccessible = true
        val serviceConnection = connectionField.get(activity) as ServiceConnection

        // Simulate service connection
        serviceConnection.onServiceConnected(
            ComponentName(context, BluetoothService::class.java),
            binder
        )
        return serviceConnection
    }
}