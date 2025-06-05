package com.aubynsamuel.clipsync.clipboardshareactivity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardUseCase
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.bluetooth.SharingResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShareClipboardUseCaseTest {

    @Mock
    private lateinit var clipboardManager: ClipboardManager

    @Mock
    private lateinit var bluetoothService: BluetoothService

    @Mock
    private lateinit var clipData: ClipData

    @Mock
    private lateinit var clipItem: ClipData.Item

    private lateinit var context: Context
    private lateinit var shareClipboardUseCase: ShareClipboardUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = RuntimeEnvironment.getApplication()

        // Mock context to return our mocked clipboard manager
        val contextSpy = spy(context)
        doReturn(clipboardManager).`when`(contextSpy).getSystemService(CLIPBOARD_SERVICE)

        shareClipboardUseCase = ShareClipboardUseCase(contextSpy)
    }

    @After
    fun tearDown() {
        ShadowToast.reset()
    }

    @Test
    fun `execute with valid clipboard text and bluetoothService should share successfully`() =
        runTest {
            // Given
            val clipText = "Test clipboard content"
            `when`(clipboardManager.primaryClip).thenReturn(clipData)
            `when`(clipData.getItemAt(0)).thenReturn(clipItem)
            `when`(clipItem.text).thenReturn(clipText)
            `when`(bluetoothService.shareClipboard(clipText)).thenReturn(SharingResult.SUCCESS)

            // When
            shareClipboardUseCase.execute(bluetoothService)

            // Then
            verify(bluetoothService).shareClipboard(clipText)
            assertEquals("Clipboard shared!", ShadowToast.getTextOfLatestToast())
        }

    @Test
    fun `execute with null bluetoothService should return SENDING_ERROR`() =
        runTest {
            // Given
            val clipText = "Test clipboard content"
            `when`(clipboardManager.primaryClip).thenReturn(clipData)
            `when`(clipData.getItemAt(0)).thenReturn(clipItem)
            `when`(clipItem.text).thenReturn(clipText)

            // When
            shareClipboardUseCase.execute(null)

            // Then
            assertEquals("Sending failed", ShadowToast.getTextOfLatestToast())
        }

    @Test
    fun `execute with empty clipboard should send null string and handle result`() = runTest {
        // Given
        `when`(clipboardManager.primaryClip).thenReturn(null)
        `when`(bluetoothService.shareClipboard("null")).thenReturn(SharingResult.SENDING_ERROR)

        // When
        shareClipboardUseCase.execute(bluetoothService)

        // Then
        verify(bluetoothService).shareClipboard("null")
        assertEquals("Sending failed", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `execute with blank clipboard text should send null string`() = runTest {
        // Given
        val clipText = "  " // blank text
        `when`(clipboardManager.primaryClip).thenReturn(clipData)
        `when`(clipData.getItemAt(0)).thenReturn(clipItem)
        `when`(clipItem.text).thenReturn(clipText)
        `when`(bluetoothService.shareClipboard("null")).thenReturn(SharingResult.SENDING_ERROR)

        // When
        shareClipboardUseCase.execute(bluetoothService)

        // Then
        verify(bluetoothService).shareClipboard("  ")
        assertEquals("Sending failed", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `execute with null clipboard text should send null string`() = runTest {
        // Given
        `when`(clipboardManager.primaryClip).thenReturn(clipData)
        `when`(clipData.getItemAt(0)).thenReturn(clipItem)
        `when`(clipItem.text).thenReturn("null")
        `when`(bluetoothService.shareClipboard("null")).thenReturn(SharingResult.SENDING_ERROR)

        // When
        shareClipboardUseCase.execute(bluetoothService)

        // Then
        verify(bluetoothService).shareClipboard("null")
        assertEquals("Sending failed", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `execute should show correct message for SENDING_ERROR result`() = runTest {
        // Given
        val clipText = "Test content"
        `when`(clipboardManager.primaryClip).thenReturn(clipData)
        `when`(clipData.getItemAt(0)).thenReturn(clipItem)
        `when`(clipItem.text).thenReturn(clipText)
        `when`(bluetoothService.shareClipboard(clipText)).thenReturn(SharingResult.SENDING_ERROR)

        // When
        shareClipboardUseCase.execute(bluetoothService)

        // Then
        assertEquals("Sending failed", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `execute should show correct message for PERMISSION_NOT_GRANTED result`() = runTest {
        // Given
        val clipText = "Test content"
        `when`(clipboardManager.primaryClip).thenReturn(clipData)
        `when`(clipData.getItemAt(0)).thenReturn(clipItem)
        `when`(clipItem.text).thenReturn(clipText)
        `when`(bluetoothService.shareClipboard(clipText)).thenReturn(SharingResult.PERMISSION_NOT_GRANTED)

        // When
        shareClipboardUseCase.execute(bluetoothService)

        // Then
        assertEquals("Bluetooth permission not granted", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `execute should show correct message for NO_SELECTED_DEVICES result`() = runTest {
        // Given
        val clipText = "Test content"
        `when`(clipboardManager.primaryClip).thenReturn(clipData)
        `when`(clipData.getItemAt(0)).thenReturn(clipItem)
        `when`(clipItem.text).thenReturn(clipText)
        `when`(bluetoothService.shareClipboard(clipText)).thenReturn(SharingResult.NO_SELECTED_DEVICES)

        // When
        shareClipboardUseCase.execute(bluetoothService)

        // Then
        assertEquals("No devices selected", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `execute should show default error message for unknown result`() = runTest {
        // Given
        val clipText = "Test content"
        `when`(clipboardManager.primaryClip).thenReturn(clipData)
        `when`(clipData.getItemAt(0)).thenReturn(clipItem)
        `when`(clipItem.text).thenReturn(clipText)
        `when`(bluetoothService.shareClipboard(clipText)).thenReturn(null)

        // When
        shareClipboardUseCase.execute(bluetoothService)

        // Then
        assertEquals("Sending failed", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun `execute should handle exception and show error message`() = runTest {
        // Given
        `when`(clipboardManager.primaryClip).thenThrow(RuntimeException("Test exception"))

        // When
        shareClipboardUseCase.execute(bluetoothService)

        // Then
        assertEquals("Sending failed", ShadowToast.getTextOfLatestToast())
    }
}