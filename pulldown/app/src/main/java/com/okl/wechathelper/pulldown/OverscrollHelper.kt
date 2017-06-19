package com.okl.wechathelper.pulldown

import android.util.Log
import android.view.View


/**
 * Created by lenovo on 2017/6/12.
 */
class OverscrollHelper {
    val LOG_TAG = "OverscrollHelper"
    val DEFAULT_OVERSCROLL_SCALE = 1f


    fun overScrollBy(view: PullToRefreshBase<*>, deltaX: Int, scrollX: Int,
                     deltaY: Int, scrollY: Int, isTouchEvent: Boolean) {
        overScrollBy(view, deltaX, scrollX, deltaY, scrollY, 0, isTouchEvent)
    }


    fun overScrollBy(view: PullToRefreshBase<*>, deltaX: Int, scrollX: Int,
                     deltaY: Int, scrollY: Int, scrollRange: Int, isTouchEvent: Boolean) {
        overScrollBy(view, deltaX, scrollX, deltaY, scrollY, scrollRange, 0, DEFAULT_OVERSCROLL_SCALE, isTouchEvent)
    }


    fun overScrollBy(view: PullToRefreshBase<*>, deltaX: Int, scrollX: Int,
                     deltaY: Int, scrollY: Int, scrollRange: Int, fuzzyThreshold: Int,
                     scaleFactor: Float, isTouchEvent: Boolean) {

        val deltaValue: Int
        val currentScrollValue: Int
        val scrollValue: Int
        when (view.getPullToRefreshScrollDirection()) {
            PullToRefreshBase.Orientation.HORIZONTAL -> {
                deltaValue = deltaX
                scrollValue = scrollX
                currentScrollValue = view.scrollX
            }
            PullToRefreshBase.Orientation.VERTICAL->{
                deltaValue = deltaY
                scrollValue = scrollY
                currentScrollValue = view.scrollY
            }
        }

        if (view.isPullToRefreshOverScrollEnabled() && !view.isRefreshing()) {
            val mode = view.getMode()

            if (mode.permitsPullToRefresh() && !isTouchEvent && deltaValue != 0) {
                val newScrollValue = deltaValue + scrollValue

                if (true) {
                    Log.d(LOG_TAG, "OverScroll. DeltaX: " + deltaX + ", ScrollX: " + scrollX + ", DeltaY: " + deltaY
                            + ", ScrollY: " + scrollY + ", NewY: " + newScrollValue + ", ScrollRange: " + scrollRange
                            + ", CurrentScroll: " + currentScrollValue)
                }

                if (newScrollValue < 0 - fuzzyThreshold) {
                    if (mode.showHeaderLoadingLayout()) {
                        if (currentScrollValue == 0) {
                            view.setState(PullToRefreshBase.State.OVERSCROLLING)
                        }

                        view.setHeaderScroll((scaleFactor * (currentScrollValue + newScrollValue)).toInt())
                    }
                } else if (newScrollValue > scrollRange + fuzzyThreshold) {

                    if (mode.showFooterLoadingLayout()) {

                        if (currentScrollValue == 0) {
                            view.setState(PullToRefreshBase.State.OVERSCROLLING)
                        }

                        view.setHeaderScroll((scaleFactor * (currentScrollValue + newScrollValue - scrollRange)).toInt())
                    }
                } else if (Math.abs(newScrollValue) <= fuzzyThreshold || Math.abs(newScrollValue - scrollRange) <= fuzzyThreshold) {

                    view.setState(PullToRefreshBase.State.RESET)
                }
            } else if (isTouchEvent && PullToRefreshBase.State.OVERSCROLLING === view.getState()) {

                view.setState(PullToRefreshBase.State.RESET)
            }
        }
    }

    public fun isAndroidOverScrollEnabled(view: View): Boolean {
        return view.getOverScrollMode() !== View.OVER_SCROLL_NEVER
    }
}