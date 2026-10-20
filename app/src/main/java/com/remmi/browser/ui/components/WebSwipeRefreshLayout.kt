package com.remmi.browser.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class WebSwipeRefreshLayout @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

  var canScrollUpCallback: (() -> Boolean)? = null

  private val touchSlop = android.view.ViewConfiguration.get(context).scaledTouchSlop
  private var startY = 0f
  private var startX = 0f
  private var isDraggingDown = false
  private var startedAtTop = false
  private var lockedOutForGesture = false

  init {
    setDistanceToTriggerSync((120 * resources.displayMetrics.density).toInt())
  }

  override fun canChildScrollUp(): Boolean {
    return canScrollUpCallback?.invoke() ?: super.canChildScrollUp()
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    if (!isEnabled) {
      return false
    }

    when (ev.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        startY = ev.y
        startX = ev.x
        isDraggingDown = false
        startedAtTop = !canChildScrollUp()
        lockedOutForGesture = !startedAtTop
        if (lockedOutForGesture) {
          return false
        }
      }
      MotionEvent.ACTION_MOVE -> {
        if (lockedOutForGesture || canChildScrollUp()) {
          lockedOutForGesture = true
          return false
        }
        val deltaY = ev.y - startY
        val deltaX = kotlin.math.abs(ev.x - startX)
        if (deltaY < -touchSlop || deltaX > kotlin.math.abs(deltaY)) {
          // User is scrolling down the page (finger moving up) or scrolling horizontally
          lockedOutForGesture = true
          return false
        }
        isDraggingDown = deltaY > touchSlop * 2
        if (!isDraggingDown) {
          return false
        }
      }
      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        isDraggingDown = false
        lockedOutForGesture = false
      }
    }

    return super.onInterceptTouchEvent(ev)
  }
}

