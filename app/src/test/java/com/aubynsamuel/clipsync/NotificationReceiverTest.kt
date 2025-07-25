package com.aubynsamuel.clipsync

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.notification.NotificationReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S]) // Testing with Android 12 (API 31)
class NotificationReceiverTest {

    private lateinit var notificationReceiver: NotificationReceiver
    private lateinit var context: Context
    private lateinit var shadowApplication: ShadowApplication

    @Mock
    private lateinit var mockNotificationManager: NotificationManager

    @Mock
    private lateinit var mockClipboardManager: ClipboardManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        notificationReceiver = NotificationReceiver()
        context = ApplicationProvider.getApplicationContext()
        shadowApplication = ShadowApplication()

        // Mock system services
        shadowApplication.setSystemService(Context.NOTIFICATION_SERVICE, mockNotificationManager)
        shadowApplication.setSystemService(Context.CLIPBOARD_SERVICE, mockClipboardManager)
    }

    @Test
    fun `onReceive with ACTION_DISMISS stops service and cancels notification`() {
        // Arrange
        val intent = Intent().apply {
            action = "ACTION_DISMISS"
        }

        // Act
        notificationReceiver.onReceive(context, intent)

        // Assert
        // Verify service was stopped
        val nextStoppedService = shadowApplication.nextStoppedService
        assertEquals(BluetoothService::class.java.name, nextStoppedService.component?.className)

        // Verify notification was cancelled
        verify(mockNotificationManager).cancel(1001)
    }

    @Test
    fun `onReceive with ACTION_COPY copies text to clipboard and shows toast on older Android`() {
        // Arrange
        val testClipText = "Test clipboard text"
        val testNotificationId = 12345
        val intent = Intent().apply {
            action = "ACTION_COPY"
            putExtra("CLIP_TEXT", testClipText)
            putExtra("NOTIFICATION_ID", testNotificationId)
        }

        val clipDataCaptor = ArgumentCaptor.forClass(ClipData::class.java)

        // Act
        notificationReceiver.onReceive(context, intent)

        // Assert
        // Verify clipboard was set
        verify(mockClipboardManager).setPrimaryClip(clipDataCaptor.capture())

        // Verify the ClipData contains correct text
        val capturedClipData = clipDataCaptor.value
        assertEquals(testClipText, capturedClipData.getItemAt(0).text.toString())
        assertEquals("Received Text", capturedClipData.description.label.toString())

        // Verify toast was shown (on Android < 13) using Robolectric's ShadowToast
        val latestToast = ShadowToast.getLatestToast()
        assertEquals("Copied to clipboard", ShadowToast.getTextOfLatestToast())

        // Verify notification was cancelled with correct ID
        verify(mockNotificationManager).cancel(testNotificationId)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU]) // Android 13 (API 33)
    fun `onReceive with ACTION_COPY does not show toast on Android 13+`() {
        // Arrange
        val testClipText = "Test clipboard text"
        val testNotificationId = 12345
        val intent = Intent().apply {
            action = "ACTION_COPY"
            putExtra("CLIP_TEXT", testClipText)
            putExtra("NOTIFICATION_ID", testNotificationId)
        }

        // Clear any previous toasts
        ShadowToast.reset()

        // Act
        notificationReceiver.onReceive(context, intent)

        // Assert
        // Verify clipboard was still set
        verify(mockClipboardManager).setPrimaryClip(any())

        // Verify toast was NOT shown (on Android 13+)
        assertNull("No toast should be shown on Android 13+", ShadowToast.getLatestToast())

        // Verify notification was still cancelled
        verify(mockNotificationManager).cancel(testNotificationId)
    }

    @Test
    fun `onReceive with ACTION_COPY and null CLIP_TEXT returns early`() {
        // Arrange
        val intent = Intent().apply {
            action = "ACTION_COPY"
            // No CLIP_TEXT extra
        }

        // Clear any previous toasts
        ShadowToast.reset()

        // Act
        notificationReceiver.onReceive(context, intent)

        // Assert
        // Verify nothing was called since we returned early
        verify(mockClipboardManager, never()).setPrimaryClip(any())
        assertNull("No toast should be shown when CLIP_TEXT is null", ShadowToast.getLatestToast())
        verify(mockNotificationManager, never()).cancel(any<Int>())
    }

    @Test
    fun `onReceive with ACTION_COPY and empty CLIP_TEXT still processes`() {
        // Arrange
        val testClipText = ""
        val testNotificationId = 12345
        val intent = Intent().apply {
            action = "ACTION_COPY"
            putExtra("CLIP_TEXT", testClipText)
            putExtra("NOTIFICATION_ID", testNotificationId)
        }

        // Act
        notificationReceiver.onReceive(context, intent)

        // Assert
        // Verify clipboard was set with empty text
        verify(mockClipboardManager).setPrimaryClip(any())
        assertEquals("Copied to clipboard", ShadowToast.getTextOfLatestToast())
        verify(mockNotificationManager).cancel(testNotificationId)
    }

    @Test
    fun `onReceive with ACTION_COPY and no NOTIFICATION_ID does not cancel notification`() {
        // Arrange
        val testClipText = "Test text"
        val intent = Intent().apply {
            action = "ACTION_COPY"
            putExtra("CLIP_TEXT", testClipText)
            // No NOTIFICATION_ID extra (defaults to 0)
        }

        // Act
        notificationReceiver.onReceive(context, intent)

        // Assert
        // Verify clipboard operations still happened
        verify(mockClipboardManager).setPrimaryClip(any())
        assertEquals("Copied to clipboard", ShadowToast.getTextOfLatestToast())

        // Verify notification was NOT cancelled since ID was 0
        verify(mockNotificationManager, never()).cancel(any<Int>())
    }

    @Test
    fun `onReceive with unknown action does nothing`() {
        // Arrange
        val intent = Intent().apply {
            action = "UNKNOWN_ACTION"
        }

        // Clear any previous toasts
        ShadowToast.reset()

        // Act
        notificationReceiver.onReceive(context, intent)

        // Assert
        // Verify no interactions occurred
        verify(mockNotificationManager, never()).cancel(any<Int>())
        verify(mockClipboardManager, never()).setPrimaryClip(any())
        assertNull("No toast should be shown for unknown action", ShadowToast.getLatestToast())

        // Verify no services were stopped
        val nextStoppedService = shadowApplication.nextStoppedService
        assertNull(nextStoppedService)
    }

    @Test
    fun `onReceive with null action does nothing`() {
        // Arrange
        val intent = Intent() // No action set

        // Clear any previous toasts
        ShadowToast.reset()

        // Act
        notificationReceiver.onReceive(context, intent)

        // Assert
        // Verify no interactions occurred
        verify(mockNotificationManager, never()).cancel(any<Int>())
        verify(mockClipboardManager, never()).setPrimaryClip(any())
        assertNull("No toast should be shown for null action", ShadowToast.getLatestToast())
    }

    @Test
    fun `onReceive with ACTION_COPY verifies ClipData creation parameters`() {
        // Arrange
        val testClipText = "Sample clipboard content"
        val intent = Intent().apply {
            action = "ACTION_COPY"
            putExtra("CLIP_TEXT", testClipText)
            putExtra("NOTIFICATION_ID", 999)
        }

        val clipDataCaptor = ArgumentCaptor.forClass(ClipData::class.java)

        // Act
        notificationReceiver.onReceive(context, intent)

        // Assert
        verify(mockClipboardManager).setPrimaryClip(clipDataCaptor.capture())

        val capturedClipData = clipDataCaptor.value
        // Verify ClipData properties
        assertEquals("Received Text", capturedClipData.description.label.toString())
        assertEquals(1, capturedClipData.itemCount)
        assertEquals(testClipText, capturedClipData.getItemAt(0).text.toString())
    }

    @Test
    fun `onReceive with ACTION_DISMISS only cancels notification 1001`() {
        // Arrange
        val intent = Intent().apply {
            action = "ACTION_DISMISS"
        }

        // Act
        notificationReceiver.onReceive(context, intent)

        // Assert
        // Verify only notification 1001 was cancelled, not any other ID
        verify(mockNotificationManager, times(1)).cancel(1001)
        verify(mockNotificationManager, never()).cancel(0)
        verify(mockNotificationManager, never()).cancel(999)
    }
}