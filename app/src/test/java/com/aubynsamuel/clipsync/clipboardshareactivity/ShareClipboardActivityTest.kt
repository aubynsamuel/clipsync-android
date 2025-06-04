package com.aubynsamuel.clipsync.clipboardshareactivity

/**
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.aubynsamuel.clipsync.activities.ShareClipboardActivity
import com.aubynsamuel.clipsync.activities.shareclipboard.GetClipTextUseCase
import com.aubynsamuel.clipsync.activities.shareclipboard.ShareClipboardWorker
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock.Strictness
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ShareClipboardActivityTest {

// Ensures LiveData / ArchitectureComponents run instantly
@get:Rule
val instantTaskExecutorRule = InstantTaskExecutorRule()

private lateinit var context: Context
private var mocksInitialized = false

@Before
fun setUp() {
// Set up a test instance of WorkManager (synchronous executor).
context = ApplicationProvider.getApplicationContext()
val config = Configuration.Builder()
.setMinimumLoggingLevel(Log.VERBOSE)
.build()
WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
// Enable Mockito inline mock creation (for mockConstruction & mockStatic)
MockitoAnnotations.openMocks(this).strictness = Strictness.LENIENT
mocksInitialized = true
}

@After
fun tearDown() {
// Nothing special to tear down here
}

@Test
fun whenIntentActionNotShare_thenActivityFinishesImmediately() {
// Build an intent with action other than "ACTION_SHARE"
val intent = Intent(context, ShareClipboardActivity::class.java).apply {
action = "SOME_OTHER_ACTION"
}

// Create the ActivityController and start the activity
val controller: ActivityController<ShareClipboardActivity> =
buildActivity(ShareClipboardActivity::class.java, intent)
val activity = controller.create().start().resume().get()

// Since action != "ACTION_SHARE", onCreate() should immediately call finish()
// Robolectric tracks the "isFinishing()" flag.
assertTrue(
"Activity should have been finished immediately when action != ACTION_SHARE",
activity.isFinishing
)
}

@Test
fun whenIntentActionShare_thenGetClipTextIsCalledAndWorkIsEnqueued() {
// 1) Stub GetClipTextUseCase so that its constructor + invoke() returns "TEST_CLIP"
val constructorMock = mockConstruction(
GetClipTextUseCase::class.java
) { mockUseCase, contextMock ->
// Whenever invoke() is called on this instance, return a known string
`when`(mockUseCase.invoke()).thenReturn("TEST_CLIP")
}

// 2) Mock WorkManager.getInstance(...) so we can verify enqueue(...) was called
val workManagerMock = mock(WorkManager::class.java)
val workManagerStatic = mockStatic(WorkManager::class.java)
workManagerStatic.`when`<WorkManager> {
WorkManager.getInstance(any(Context::class.java))
}.thenReturn(workManagerMock)

// 3) Build an intent with action = "ACTION_SHARE"
val intent = Intent(context, ShareClipboardActivity::class.java).apply {
action = "ACTION_SHARE"
}

// 4) Create the activity (onCreate will schedule a Handler.postDelayed)
val controller: ActivityController<ShareClipboardActivity> =
buildActivity(ShareClipboardActivity::class.java, intent)
val activity = controller.create().start().resume().visible().get()

// At this point, handleShareAction() has called:
// Handler(Looper.getMainLooper()).postDelayed({ ... }, 300)
// We need to run Robolectric's UI thread looper until idle
ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

// 5) Verify GetClipTextUseCase was constructed exactly once
assertEquals(
"GetClipTextUseCase constructor should have been invoked exactly once",
1,
constructorMock.constructed().size
)

// 6) Capture the WorkRequest enqueued
val requestCaptor = ArgumentCaptor.forClass(OneTimeWorkRequest::class.java)
verify(workManagerMock, times(1)).enqueue(requestCaptor.capture())

val capturedRequest = requestCaptor.value
// Assert that the worker class is ShareClipboardWorker
val workerClassName = capturedRequest.workSpec.workerClassName
assertEquals(
"The enqueued WorkRequest should be for ShareClipboardWorker",
ShareClipboardWorker::class.java.name,
workerClassName
)

// Assert that the input data has KEY_CLIP_TEXT = "TEST_CLIP"
val inputData = capturedRequest.workSpec.input
assertEquals(
"InputData.KEY_CLIP_TEXT should match what GetClipTextUseCase.invoke() returned",
"TEST_CLIP",
inputData.getString(ShareClipboardWorker.KEY_CLIP_TEXT)
)

// 7) Finally, the activity should finish (isFinishing == true)
assertTrue(
"Activity should finish after scheduling the work",
activity.isFinishing
)

// Cleanup static mock
workManagerStatic.close()
constructorMock.close()
}
}
 */