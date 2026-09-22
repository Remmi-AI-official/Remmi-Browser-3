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

  private var startY = 0f
  private var isDraggingDown = false

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
        isDraggingDown = false
      }
      MotionEvent.ACTION_MOVE -> {
        val deltaY = ev.y - startY
        isDraggingDown = deltaY > 0
        if (canChildScrollUp()) {
          return false
        }
      }
      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        isDraggingDown = false
      }
    }

    return super.onInterceptTouchEvent(ev)
  }
}

