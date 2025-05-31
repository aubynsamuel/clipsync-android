package com.aubynsamuel.clipsync.clipboardshareactivity

import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardPresenter
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardUseCase
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardView
import com.aubynsamuel.clipsync.bluetooth.SharingResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ShareClipboardPresenterTest {

    @Mock
    private lateinit var shareClipboardUseCase: ShareClipboardUseCase

    @Mock
    private lateinit var view: ShareClipboardView

    private lateinit var presenter: ShareClipboardPresenter

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        presenter = ShareClipboardPresenter(shareClipboardUseCase, view)
    }

    @Test
    fun `handleShare shows success message and finishes when sharing succeeds`() = runTest {
        // Given
        whenever(shareClipboardUseCase.execute()).thenReturn(SharingResult.SUCCESS)

        // When
        presenter.handleShare()

        // Then
        verify(view).showMessage("Clipboard shared!")
        verify(view).finishActivity()
    }

    @Test
    fun `handleShare shows error message when clipboard is empty`() = runTest {
        // Given
        whenever(shareClipboardUseCase.execute()).thenReturn(SharingResult.CLIPBOARD_EMPTY)

        // When
        presenter.handleShare()

        // Then
        verify(view).showMessage("Clipboard is empty")
        verify(view).finishActivity()
    }

    @Test
    fun `handleShare shows permission error message`() = runTest {
        // Given
        whenever(shareClipboardUseCase.execute()).thenReturn(SharingResult.PERMISSION_NOT_GRANTED)

        // When
        presenter.handleShare()

        // Then
        verify(view).showMessage("Bluetooth permission not granted")
        verify(view).finishActivity()
    }

    @Test
    fun `handleShare shows no devices selected message`() = runTest {
        // Given
        whenever(shareClipboardUseCase.execute()).thenReturn(SharingResult.NO_SELECTED_DEVICES)

        // When
        presenter.handleShare()

        // Then
        verify(view).showMessage("No devices selected")
        verify(view).finishActivity()
    }

    @Test
    fun `handleShare shows sending error message`() = runTest {
        // Given
        whenever(shareClipboardUseCase.execute()).thenReturn(SharingResult.SENDING_ERROR)

        // When
        presenter.handleShare()

        // Then
        verify(view).showMessage("Sending failed")
        verify(view).finishActivity()
    }
}