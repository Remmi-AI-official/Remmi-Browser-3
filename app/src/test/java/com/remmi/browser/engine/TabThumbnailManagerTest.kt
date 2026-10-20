package com.remmi.browser.engine

import android.app.Application
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@LooperMode(LooperMode.Mode.PAUSED)
class TabThumbnailManagerTest {

  private lateinit var context: Application
  private lateinit var thumbnailManager: TabThumbnailManager

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Application>()
    org.mozilla.gecko.GeckoAppShell.setApplicationContext(context)
    thumbnailManager = TabThumbnailManager.getInstance(context)
    thumbnailManager.clearAll()
    thumbnailManager.captureRequestCounter.set(0)
    thumbnailManager.captureExecutedCounter.set(0)
    thumbnailManager.captureSkippedCounter.set(0)
  }

  private suspend fun awaitCondition(timeoutMs: Long = 3000L, condition: () -> Boolean): Boolean {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < timeoutMs) {
      if (condition()) return true
      delay(20)
    }
    return condition()
  }

  @Test
  fun test1_SaveAndGetThumbnail_MemoryAndDisk() = runBlocking {
    val tabId = "tab_test_1"
    val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

    thumbnailManager.saveThumbnail(tabId, bitmap)

    val success = awaitCondition { thumbnailManager.getThumbnail(tabId) != null }
    assertTrue("Loaded thumbnail must not be null within timeout", success)

    val loaded = thumbnailManager.getThumbnail(tabId)
    assertNotNull("Loaded thumbnail must not be null", loaded)
    assertEquals(360, loaded!!.width) // Scaled width
    assertTrue(thumbnailManager.thumbnailVersions.value.containsKey(tabId))
  }

  @Test
  fun test2_RemoveThumbnailAndClearAll() = runBlocking {
    val tabId = "tab_test_2"
    val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

    thumbnailManager.saveThumbnail(tabId, bitmap)
    awaitCondition { thumbnailManager.getThumbnail(tabId) != null }
    assertNotNull(thumbnailManager.getThumbnail(tabId))

    thumbnailManager.removeThumbnail(tabId)
    awaitCondition { thumbnailManager.getThumbnail(tabId) == null }
    assertNull(thumbnailManager.getThumbnail(tabId))
    assertFalse(thumbnailManager.thumbnailVersions.value.containsKey(tabId))

    // Clear all
    val bitmapA = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val bitmapB = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    thumbnailManager.saveThumbnail("tab_a", bitmapA)
    thumbnailManager.saveThumbnail("tab_b", bitmapB)
    awaitCondition { thumbnailManager.getThumbnail("tab_a") != null && thumbnailManager.getThumbnail("tab_b") != null }
    thumbnailManager.clearAll()
    awaitCondition { thumbnailManager.getThumbnail("tab_a") == null && thumbnailManager.getThumbnail("tab_b") == null }
    assertNull(thumbnailManager.getThumbnail("tab_a"))
    assertNull(thumbnailManager.getThumbnail("tab_b"))
    assertTrue(thumbnailManager.thumbnailVersions.value.isEmpty())
  }

  @Test
  fun test3_DebouncedCapture_BatchesMultipleCalls() = runBlocking {
    val geckoView = GeckoView(context)
    val session = GeckoSession()
    geckoView.setSession(session)
    shadowOf(android.os.Looper.getMainLooper()).idle()

    val tabId = "tab_debounce_test"

    // Simulate 5 rapid capture requests (e.g. rapid NAV_STOP or subresources)
    for (i in 1..5) {
      thumbnailManager.captureGeckoView(tabId, geckoView, debounceMs = 200L)
    }

    assertEquals(5, thumbnailManager.captureRequestCounter.get())

    // Before debounce timer fires: 0 executions
    assertEquals(0, thumbnailManager.captureExecutedCounter.get())

    // Advance shadow looper past debounce duration
    shadowOf(android.os.Looper.getMainLooper()).runToEndOfTasks()

    // Exactly 1 execution occurred for the 5 calls
    assertEquals(1, thumbnailManager.captureExecutedCounter.get())
  }

  @Test
  fun test4_DuplicateCapture_WithinMinInterval_IsSkippedOrPostponed() = runBlocking {
    val geckoView = GeckoView(context)
    val session = GeckoSession()
    geckoView.setSession(session)
    shadowOf(android.os.Looper.getMainLooper()).idle()

    val tabId = "tab_duplicate_test"

    // First capture: immediate
    thumbnailManager.captureGeckoView(tabId, geckoView, debounceMs = 0L, force = true)
    shadowOf(android.os.Looper.getMainLooper()).idle()
    assertEquals(1, thumbnailManager.captureExecutedCounter.get())

    // Request non-forced capture immediately after (within MIN_CAPTURE_INTERVAL_MS)
    thumbnailManager.captureGeckoView(tabId, geckoView, debounceMs = 100L, force = false)
    shadowOf(android.os.Looper.getMainLooper()).idle()

    // Capture request was received, but skipped/postponed
    assertEquals(2, thumbnailManager.captureRequestCounter.get())
    assertTrue(thumbnailManager.captureSkippedCounter.get() >= 1)
  }

  @Test
  fun test5_ForceCapture_ExecutesImmediately() = runBlocking {
    val geckoView = GeckoView(context)
    val session = GeckoSession()
    geckoView.setSession(session)
    shadowOf(android.os.Looper.getMainLooper()).idle()

    val tabId = "tab_force_test"

    // Force capture
    thumbnailManager.captureGeckoView(tabId, geckoView, debounceMs = 0L, force = true)
    shadowOf(android.os.Looper.getMainLooper()).idle()

    assertEquals(1, thumbnailManager.captureExecutedCounter.get())
  }
}
