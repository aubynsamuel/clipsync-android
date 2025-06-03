package com.aubynsamuel.clipsync.clipboardshareactivity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Suite::class)
@Suite.SuiteClasses(
    BluetoothServiceConnectionTest::class,
    ShareClipboardUseCaseTest::class,
//    ShareClipboardActivityTest::class,
)
class ShareClipboardTestSuite