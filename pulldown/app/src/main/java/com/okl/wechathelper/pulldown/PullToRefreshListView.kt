package com.okl.wechathelper.pulldown

import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ListAdapter
import android.widget.ListView
import com.okl.wechathelper.R


/**
 * Created by lenovo on 2017/6/12.
 */
class PullToRefreshListView:PullToRefreshAdapterViewBase<ListView> {


    private var mHeaderLoadingView: LoadingLayout?=null
    private var mFooterLoadingView: LoadingLayout?=null

    private var mLvFooterLoadingFrame: FrameLayout? = null

    private var mListViewExtrasEnabled: Boolean = false

    constructor(context: Context): super(context) {

    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {

    }

    constructor(context: Context, mode: PullToRefreshBase.Mode):  super(context, mode)  {

    }

    constructor(context: Context, mode: PullToRefreshBase.Mode, style: PullToRefreshBase.AnimationStyle):  super(context, mode, style) {

    }

    override fun getPullToRefreshScrollDirection(): PullToRefreshBase.Orientation {
        return PullToRefreshBase.Orientation.VERTICAL
    }



     override fun onRefreshing(doScroll: Boolean) {

        val adapter = mRefreshableView!!.adapter
        if (!mListViewExtrasEnabled || !getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty) {
            super.onRefreshing(doScroll)
            return
        }

        super.onRefreshing(false)

        var origLoadingView: LoadingLayout?=null
        var listViewLoadingView: LoadingLayout?=null
        var oppositeListViewLoadingView: LoadingLayout?=null
        var selection: Int=0
        var scrollToY: Int=0

        when (getCurrentMode()) {
            PullToRefreshBase.Mode.MANUAL_REFRESH_ONLY, PullToRefreshBase.Mode.PULL_FROM_END -> {
                origLoadingView = getFooterLayout()
                listViewLoadingView = mFooterLoadingView!!
                oppositeListViewLoadingView = mHeaderLoadingView!!
                selection = mRefreshableView!!.count - 1
                scrollToY = scrollY - getFooterSize()
            }
            PullToRefreshBase.Mode.PULL_FROM_START -> {
                origLoadingView = getHeaderLayout()
                listViewLoadingView = mHeaderLoadingView!!
                oppositeListViewLoadingView = mFooterLoadingView!!
                selection = 0
                scrollToY = scrollY + getHeaderSize()
            }
        }

        origLoadingView!!.reset()
        origLoadingView!!.hideAllViews()
        oppositeListViewLoadingView!!.visibility = View.GONE
         listViewLoadingView!!.visibility = View.VISIBLE
        listViewLoadingView!!.refreshing()

        if (doScroll) {
            disableLoadingLayoutVisibilityChanges()

            setHeaderScroll(scrollToY)


            mRefreshableView!!.setSelection(selection)

            smoothScrollTo(0)
        }
    }

      override fun onReset() {

        if (!mListViewExtrasEnabled) {
            super.onReset()
            return
        }

        var originalLoadingLayout: LoadingLayout?=null
        var listViewLoadingLayout: LoadingLayout?=null
        var scrollToHeight: Int=0
        var selection: Int=0
        var scrollLvToEdge: Boolean=false

        when (getCurrentMode()) {
            PullToRefreshBase.Mode.MANUAL_REFRESH_ONLY, PullToRefreshBase.Mode.PULL_FROM_END -> {
                originalLoadingLayout = getFooterLayout()
                listViewLoadingLayout = mFooterLoadingView!!
                selection = mRefreshableView!!.count - 1
                scrollToHeight = getFooterSize()
                scrollLvToEdge = Math.abs(mRefreshableView!!.lastVisiblePosition - selection) <= 1
            }
            PullToRefreshBase.Mode.PULL_FROM_START-> {
                originalLoadingLayout = getHeaderLayout()
                listViewLoadingLayout = mHeaderLoadingView!!
                scrollToHeight = -getHeaderSize()
                selection = 0
                scrollLvToEdge = Math.abs(mRefreshableView!!.firstVisiblePosition - selection) <= 1
            }
        }


        if (listViewLoadingLayout!!.visibility == View.VISIBLE) {

            originalLoadingLayout!!.showInvisibleViews()

            listViewLoadingLayout.visibility = View.GONE

            if (scrollLvToEdge && getState() !== PullToRefreshBase.State.MANUAL_REFRESHING) {
                mRefreshableView!!.setSelection(selection)
                setHeaderScroll(scrollToHeight)
            }
        }

        super.onReset()
    }

     override fun createLoadingLayoutProxy(includeStart: Boolean, includeEnd: Boolean): LoadingLayoutProxy {
        var proxy = super.createLoadingLayoutProxy(includeStart, includeEnd)

        if (mListViewExtrasEnabled) {
            var mode = getMode()

            if (includeStart && mode.showHeaderLoadingLayout()) {
                proxy.addLayout(mHeaderLoadingView!!)
            }
            if (includeEnd && mode.showFooterLoadingLayout()) {
                proxy.addLayout(mFooterLoadingView!!)
            }
        }

        return proxy
    }

     fun createListView(context: Context, attrs: AttributeSet): ListView {
        var lv: ListView
            lv = InternalListViewSDK9(context, attrs)

        return lv
    }

    override fun createRefreshableView(context: Context, attrs: AttributeSet?): ListView {
        var lv = createListView(context, attrs!!)

        lv.id = android.R.id.list
        return lv
    }

    override fun handleStyledAttributes(a: TypedArray) {
        super.handleStyledAttributes(a)

        mListViewExtrasEnabled = a.getBoolean(R.styleable.PullToRefresh_ptrListViewExtrasEnabled, true)

        if (mListViewExtrasEnabled) {
            var lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL)

            var frame = FrameLayout(context)
            mHeaderLoadingView = createLoadingLayout(context, PullToRefreshBase.Mode.PULL_FROM_START, a)
            mHeaderLoadingView!!.visibility = View.GONE
            frame.addView(mHeaderLoadingView, lp)
            mRefreshableView!!.addHeaderView(frame, null, false)

            mLvFooterLoadingFrame = FrameLayout(context)
            mFooterLoadingView = createLoadingLayout(context, PullToRefreshBase.Mode.PULL_FROM_END, a)
            mFooterLoadingView!!.visibility = View.GONE
            mLvFooterLoadingFrame!!.addView(mFooterLoadingView, lp)


            if (!a.hasValue(R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled)) {
                setScrollingWhileRefreshingEnabled(true)
            }
        }
    }

    @TargetApi(9)
    internal inner class InternalListViewSDK9 : ListView, EmptyViewMethodAccessor {

        constructor(context: Context, attrs: AttributeSet):super(context, attrs){

        }
      override   fun overScrollBy(deltaX: Int, deltaY: Int, scrollX: Int, scrollY: Int, scrollRangeX: Int,
                                  scrollRangeY: Int, maxOverScrollX: Int, maxOverScrollY: Int, isTouchEvent: Boolean): Boolean {

            val returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
                    scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent)

            OverscrollHelper().overScrollBy(this@PullToRefreshListView, deltaX, scrollX, deltaY, scrollY, isTouchEvent)

            return returnValue
        }

        private var mAddedLvFooter = false

        override fun dispatchDraw(canvas: Canvas) {

            try {
                super.dispatchDraw(canvas)
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }

        }

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

            try {
                return super.dispatchTouchEvent(ev)
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
                return false
            }

        }

        override  fun setAdapter(adapter: ListAdapter) {

            if (null != mLvFooterLoadingFrame && !mAddedLvFooter) {
                addFooterView(mLvFooterLoadingFrame, null, false)
                mAddedLvFooter = true
            }

            super.setAdapter(adapter)
        }

        override fun setEmptyView(emptyView: View) {
            this@PullToRefreshListView.setEmptyView(emptyView)

        }


        override fun setEmptyViewInternal(emptyView: View) {
            setEmptyView(emptyView)
        }

    }




}