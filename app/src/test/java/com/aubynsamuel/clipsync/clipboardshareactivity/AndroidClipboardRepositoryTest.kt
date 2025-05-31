package com.aubynsamuel.clipsync.clipboardshareactivity

import android.content.ClipData
import android.content.ClipboardManager
import com.aubynsamuel.clipsync.activities.shareclipboard.AndroidClipboardRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

//@RunWith(AndroidJUnit4::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33]) // Use SDK 33 instead of 34 to avoid targetSdkVersion issues
class AndroidClipboardRepositoryTest {

    @Mock
    private lateinit var clipboardManager: ClipboardManager

    @Mock
    private lateinit var clipData: ClipData

    @Mock
    private lateinit var clipItem: ClipData.Item

    private lateinit var repository: AndroidClipboardRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = AndroidClipboardRepository(clipboardManager)
    }

    @Test
    fun `getClipboardText returns text when clipboard has valid content`() {
        // Given
        val expectedText = "Hello World"
        whenever(clipboardManager.primaryClip).thenReturn(clipData)
        whenever(clipData.getItemAt(0)).thenReturn(clipItem)
        whenever(clipItem.text).thenReturn(expectedText)

        // When
        val result = repository.getClipboardText()

        // Then
        assertEquals(expectedText, result)
    }

    @Test
    fun `getClipboardText returns null when clipboard is empty`() {
        // Given
        whenever(clipboardManager.primaryClip).thenReturn(null)

        // When
        val result = repository.getClipboardText()

        // Then
        assertNull(result)
    }

    @Test
    fun `getClipboardText returns null when clipboard text is null string`() {
        // Given
        whenever(clipboardManager.primaryClip).thenReturn(clipData)
        whenever(clipData.getItemAt(0)).thenReturn(clipItem)
        whenever(clipItem.text).thenReturn("null")

        // When
        val result = repository.getClipboardText()

        // Then
        assertNull(result)
    }

    @Test
    fun `getClipboardText returns null when clipboard text is blank`() {
        // Given
        whenever(clipboardManager.primaryClip).thenReturn(clipData)
        whenever(clipData.getItemAt(0)).thenReturn(clipItem)
        whenever(clipItem.text).thenReturn("   ")

        // When
        val result = repository.getClipboardText()

        // Then
        assertNull(result)
    }
}