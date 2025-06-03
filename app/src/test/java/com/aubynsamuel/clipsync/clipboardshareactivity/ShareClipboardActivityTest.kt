package com.aubynsamuel.clipsync.clipboardshareactivity

/**
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.aubynsamuel.clipsync.activities.ShareClipboardActivity
import com.aubynsamuel.clipsync.bluetooth.BluetoothService
import com.aubynsamuel.clipsync.bluetooth.SharingResult
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ShareClipboardActivityTest {

private lateinit var context: Context
private lateinit var mockBluetoothService: BluetoothService
private lateinit var mockBinder: BluetoothService.LocalBinder
private lateinit var scenario: ActivityScenario<ShareClipboardActivity>

@Before
fun setup() {
context = ApplicationProvider.getApplicationContext()
mockBluetoothService = mock(BluetoothService::class.java)
mockBinder = mock(BluetoothService.LocalBinder::class.java)
`when`(mockBinder.getService()).thenReturn(mockBluetoothService)

// Intercept service binding
val shadowApplication = shadowOf(context)
shadowApplication.setComponentNameAndServiceForBindService(
Intent(context, BluetoothService::class.java).component,
object : IBinder {
override fun queryLocalInterface(descriptor: String): IBinder? {
return mockBinder
}

override fun getInterfaceDescriptor(): String? = null
override fun pingBinder(): Boolean = false
override fun isBinderAlive(): Boolean = true
override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) {}
override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean =
false
}
)
}

@After
fun tearDown() {
if (::scenario.isInitialized) {
scenario.close()
}
ShadowToast.reset()
}

@Test
fun `activity finishes immediately when action is not ACTION_SHARE`() {
// Given
val intent = Intent(context, ShareClipboardActivity::class.java)

// When
scenario = ActivityScenario.launch(intent)

// Then
assertTrue(scenario.state == Lifecycle.State.DESTROYED)
}

@Test
fun `activity attempts to bind service and handle share action on ACTION_SHARE`() {
// Given
val intent = Intent(context, ShareClipboardActivity::class.java).apply {
action = "ACTION_SHARE"
}

// When
scenario = ActivityScenario.launch(intent)

// Then
// Verify that bindService was called
val shadowApplication = shadowOf(context)
val boundIntent = shadowApplication.peekNextStartedService()
assertTrue(boundIntent?.component?.className == BluetoothService::class.java.name)

// We can't directly verify handleShareAction is called here synchronously
// as it depends on the service connection. We'll test the sharing logic
// in a separate test once the service is "connected".
}

@Test
fun `activity shares clipboard content successfully on ACTION_SHARE`() {
// Given
val clipboardText = "Test clipboard content"
val intent = Intent(context, ShareClipboardActivity::class.java).apply {
action = "ACTION_SHARE"
}

// Mock clipboard
val clipboardManager =
context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
val clipData = android.content.ClipData.newPlainText("text", clipboardText)
clipboardManager.setPrimaryClip(clipData)

// Mock BluetoothService to return success
`when`(mockBluetoothService.shareClipboard(clipboardText)).thenReturn(SharingResult.SUCCESS)

// When
scenario = ActivityScenario.launch(intent)
scenario.onActivity { activity ->
// Simulate service connection
val serviceIntent = Intent(activity, BluetoothService::class.java)
activity.onServiceConnected(null, mockBinder)

// Allow the delayed handleShareAction to execute
Thread.sleep(500) // Wait for the Handler's postDelayed

// Then
verify(mockBluetoothService).shareClipboard(clipboardText)
assertTrue(ShadowToast.showedToast("Clipboard shared!"))
assertTrue(activity.isFinishing)
}
}

@Test
fun `activity shows 'Clipboard is empty' toast when clipboard is empty`() {
// Given
val intent = Intent(context, ShareClipboardActivity::class.java).apply {
action = "ACTION_SHARE"
}

// Mock an empty clipboard
val clipboardManager =
context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
clipboardManager.setPrimaryClip(android.content.ClipData.newPlainText("text", ""))

// When
scenario = ActivityScenario.launch(intent)
scenario.onActivity { activity ->
// Simulate service connection
val serviceIntent = Intent(activity, BluetoothService::class.java)
activity.onServiceConnected(null, mockBinder)

// Allow the delayed handleShareAction to execute
Thread.sleep(500)

// Then
assertTrue(ShadowToast.showedToast("Clipboard is empty"))
assertTrue(activity.isFinishing)
}
}

@Test
fun `activity shows 'Sending failed' toast when sharing fails`() {
// Given
val clipboardText = "Test clipboard content"
val intent = Intent(context, ShareClipboardActivity::class.java).apply {
action = "ACTION_SHARE"
}

// Mock clipboard
val clipboardManager =
context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
val clipData = android.content.ClipData.newPlainText("text", clipboardText)
clipboardManager.setPrimaryClip(clipData)

// Mock BluetoothService to return a failure
`when`(mockBluetoothService.shareClipboard(clipboardText)).thenReturn(SharingResult.SENDING_ERROR)

// When
scenario = ActivityScenario.launch(intent)
scenario.onActivity { activity ->
// Simulate service connection
val serviceIntent = Intent(activity, BluetoothService::class.java)
activity.onServiceConnected(null, mockBinder)

// Allow the delayed handleShareAction to execute
Thread.sleep(500)

// Then
verify(mockBluetoothService).shareClipboard(clipboardText)
assertTrue(ShadowToast.showedToast("Sending failed"))
assertTrue(activity.isFinishing)
}
}
}
 */