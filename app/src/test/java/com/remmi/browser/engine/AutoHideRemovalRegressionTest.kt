package com.remmi.browser.engine

import android.app.Application
import android.content.Context
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import com.remmi.browser.security.PrivacyProfile
import com.remmi.browser.ui.components.WebSwipeRefreshLayout
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AutoHideRemovalRegressionTest {

  private lateinit var context: Context
  private lateinit var manager: GeckoEngineManager
  private lateinit var tabManager: TabManager

  private val testCallbacks = object : GeckoTabCallbacks {
    override fun onUrlChange(url: String) {}
    override fun onTitleChange(title: String) {}
    override fun onProgressChange(progress: Int) {}
    override fun onLoadingChange(isLoading: Boolean) {}
  }

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext<Application>()
    org.mozilla.gecko.GeckoAppShell.setApplicationContext(context)
    manager = GeckoEngineManager.getInstance(context)
    tabManager = TabManager.getInstance()
    tabManager.closeAllTabs()
    manager.setInitStateForTesting(GeckoEngineManager.GeckoInitState.READY)
    manager.uriLoaderForTest = { _, _, _ -> }
    manager.sessionOpenerForTest = { _, _ -> }
  }

  @After
  fun tearDown() = runBlocking {
    manager.uriLoaderForTest = null
    manager.sessionOpenerForTest = null
    manager.closeAllSessionsSafely()
    tabManager.closeAllTabs()
  }

  @Test
  fun test1_ScrollPositionsTrackedWithoutDirectionInference() = runBlocking {
    val tab = tabManager.createTab("https://example.com/scroll-test")
    val session = GeckoSession(GeckoSessionSettings.Builder().build())
    manager.setSessionForTesting(tab.id, session)
    val gv = GeckoView(context).apply { tag = tab.id }
    manager.attachView(tab.id, gv, PrivacyProfile.SHIELD, false, callbacks = testCallbacks)

    // Scroll changes update scroll position accurately
    session.scrollDelegate?.onScrollChanged(session, 0, 42)
    assertEquals(42, manager.getScrollY(tab.id))

    session.scrollDelegate?.onScrollChanged(session, 0, 50)
    assertEquals(50, manager.getScrollY(tab.id))

    session.scrollDelegate?.onScrollChanged(session, 0, 10)
    assertEquals(10, manager.getScrollY(tab.id))
  }

  @Test
  fun test2_WebSwipeRefreshLayout_NeverInterceptsUpwardGesture() {
    val swipeLayout = WebSwipeRefreshLayout(context)
    var canScrollUp = false
    swipeLayout.canScrollUpCallback = { canScrollUp }

    // ACTION_DOWN at y = 300
    val downEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 100f, 300f, 0)
    swipeLayout.onInterceptTouchEvent(downEvent)

    // ACTION_MOVE upward to y = 200 (deltaY = -100, upward scroll)
    val moveUpEvent = MotionEvent.obtain(0L, 10L, MotionEvent.ACTION_MOVE, 100f, 200f, 0)
    val interceptedUp = swipeLayout.onInterceptTouchEvent(moveUpEvent)
    assertFalse("Upward swipe gesture must NEVER be intercepted by SwipeRefreshLayout", interceptedUp)

    // When canScrollUp is true (page not at top), downward drag also must not intercept
    canScrollUp = true
    val moveDownMiddlePage = MotionEvent.obtain(0L, 20L, MotionEvent.ACTION_MOVE, 100f, 400f, 0)
    val interceptedMiddle = swipeLayout.onInterceptTouchEvent(moveDownMiddlePage)
    assertFalse("Downward gesture when page is not at top must NOT be intercepted", interceptedMiddle)
  }
}
