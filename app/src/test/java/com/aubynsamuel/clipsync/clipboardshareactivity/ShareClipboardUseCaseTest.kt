package com.aubynsamuel.clipsync.clipboardshareactivity

import com.aubynsamuel.clipsync.activities.shareclipboard.ClipboardRepository
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardUseCase
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.bluetooth.SharingResult
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ShareClipboardUseCaseTest {

    @Mock
    private lateinit var clipboardRepository: ClipboardRepository

    @Mock
    private lateinit var bluetoothService: BluetoothService

    private lateinit var useCase: ShareClipboardUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ShareClipboardUseCase(clipboardRepository, bluetoothService)
    }

    @Test
    fun `execute returns CLIPBOARD_EMPTY when clipboard is empty`() = runTest {
        // Given
        whenever(clipboardRepository.getClipboardText()).thenReturn(null)

        // When
        val result = useCase.execute()

        // Then
        assertEquals(SharingResult.CLIPBOARD_EMPTY, result)
        verify(bluetoothService, never()).shareClipboard(any())
    }

    @Test
    fun `execute returns SUCCESS when sharing succeeds`() = runTest {
        // Given
        val clipText = "Hello World"
        whenever(clipboardRepository.getClipboardText()).thenReturn(clipText)
        whenever(bluetoothService.shareClipboard(clipText)).thenReturn(SharingResult.SUCCESS)

        // When
        val result = useCase.execute()

        // Then
        assertEquals(SharingResult.SUCCESS, result)
        verify(bluetoothService).shareClipboard(clipText)
    }

    @Test
    fun `execute returns SENDING_ERROR when sharing fails`() = runTest {
        // Given
        val clipText = "Hello World"
        whenever(clipboardRepository.getClipboardText()).thenReturn(clipText)
        whenever(bluetoothService.shareClipboard(clipText)).thenReturn(SharingResult.SENDING_ERROR)

        // When
        val result = useCase.execute()

        // Then
        assertEquals(SharingResult.SENDING_ERROR, result)
    }
}