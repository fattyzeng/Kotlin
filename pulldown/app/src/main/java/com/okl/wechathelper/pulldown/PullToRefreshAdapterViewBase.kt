package com.okl.wechathelper.pulldown

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import com.okl.wechathelper.R


/**
 * Created by lenovo on 2017/6/15.
 */
public abstract class PullToRefreshAdapterViewBase<T:AbsListView> :PullToRefreshBase<T>,AbsListView.OnScrollListener{

    private fun convertEmptyViewLayoutParams(lp: ViewGroup.LayoutParams?): FrameLayout.LayoutParams? {
        var newLp: FrameLayout.LayoutParams? = null

        if (null != lp) {
            newLp = FrameLayout.LayoutParams(lp)

            if (lp is LinearLayout.LayoutParams) {
                newLp.gravity = lp.gravity
            } else {
                newLp.gravity = Gravity.CENTER
            }
        }

        return newLp
    }

    private var mLastItemVisible: Boolean = false
    private var mOnScrollListener: AbsListView.OnScrollListener? = null
    private var mOnLastItemVisibleListener: PullToRefreshBase.OnLastItemVisibleListener? = null
    private var mEmptyView: View? = null

    private var mIndicatorIvTop: IndicatorLayout? = null
    private var mIndicatorIvBottom: IndicatorLayout? = null

    private var mShowIndicator: Boolean = false
    private var mScrollEmptyView = true

    constructor(context: Context): super(context) {

        mRefreshableView!!.setOnScrollListener(this)
    }

    constructor(context: Context, attrs: AttributeSet):   super(context, attrs) {

        mRefreshableView!!.setOnScrollListener(this)
    }

    constructor(context: Context, mode: PullToRefreshBase.Mode): super(context, mode) {

        mRefreshableView!!.setOnScrollListener(this)
    }

    constructor(context: Context, mode: PullToRefreshBase.Mode, animStyle: PullToRefreshBase.AnimationStyle):  super(context, mode, animStyle){

        mRefreshableView!!.setOnScrollListener(this)
    }


    fun getShowIndicator(): Boolean {
        return mShowIndicator
    }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int,
                          totalItemCount: Int) {

        if (DEBUG) {
            Log.d(LOG_TAG, "First Visible: " + firstVisibleItem + ". Visible Count: " + visibleItemCount
                    + ". Total Items:" + totalItemCount)
        }

        /**
         * Set whether the Last Item is Visible. lastVisibleItemIndex is a
         * zero-based index, so we minus one totalItemCount to check
         */
        if (null != mOnLastItemVisibleListener) {
            mLastItemVisible = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount - 1
        }

        // If we're showing the indicator, check positions...
        if (getShowIndicatorInternal()) {
            updateIndicatorViewsVisibility()
        }

        // Finally call OnScrollListener if we have one
        if (null != mOnScrollListener) {
            mOnScrollListener!!.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount)
        }
    }

    override fun onScrollStateChanged(view: AbsListView, state: Int) {

        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && null != mOnLastItemVisibleListener && mLastItemVisible) {
            mOnLastItemVisibleListener!!.onLastItemVisible()
        }

        if (null != mOnScrollListener) {
            mOnScrollListener!!.onScrollStateChanged(view, state)
        }
    }


    fun setAdapter(adapter: ListAdapter) {
        (mRefreshableView as AdapterView<ListAdapter>).setAdapter(adapter)
    }


    fun setEmptyView(newEmptyView: View?) {
        val refreshableViewWrapper = getRefreshableViewWrapper()

        if (null != newEmptyView) {

            newEmptyView.isClickable = true

            val newEmptyViewParent = newEmptyView.parent
            if (null != newEmptyViewParent && newEmptyViewParent is ViewGroup) {
                newEmptyViewParent.removeView(newEmptyView)
            }

            val lp = convertEmptyViewLayoutParams(newEmptyView.layoutParams)
            if (null != lp) {
                refreshableViewWrapper.addView(newEmptyView, lp)
            } else {
                refreshableViewWrapper.addView(newEmptyView)
            }
        }

        if (mRefreshableView is EmptyViewMethodAccessor) {
            (mRefreshableView as EmptyViewMethodAccessor).setEmptyViewInternal(newEmptyView!!)
        } else {
            mRefreshableView!!.emptyView = newEmptyView
        }
        mEmptyView = newEmptyView
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        mRefreshableView!!.onItemClickListener = listener
    }

    fun setOnLastItemVisibleListener(listener: PullToRefreshBase.OnLastItemVisibleListener) {
        mOnLastItemVisibleListener = listener
    }

    fun setOnScrollListener(listener: AbsListView.OnScrollListener) {
        mOnScrollListener = listener
    }

    fun setScrollEmptyView(doScroll: Boolean) {
        mScrollEmptyView = doScroll
    }

    fun setShowIndicator(showIndicator: Boolean) {
        mShowIndicator = showIndicator

        if (getShowIndicatorInternal()) {
            addIndicatorViews()
        } else {
            removeIndicatorViews()
        }
    }

    ;

     override fun onPullToRefresh() {
        super.onPullToRefresh()

        if (getShowIndicatorInternal()) {
            when (getCurrentMode()) {
                PullToRefreshBase.Mode.PULL_FROM_END -> mIndicatorIvBottom!!.pullToRefresh()
                PullToRefreshBase.Mode.PULL_FROM_START -> mIndicatorIvTop!!.pullToRefresh()
                else -> {
                }
            }
        }
    }

     override fun onRefreshing(doScroll: Boolean) {
        super.onRefreshing(doScroll)

        if (getShowIndicatorInternal()) {
            updateIndicatorViewsVisibility()
        }
    }

     override fun onReleaseToRefresh() {
        super.onReleaseToRefresh()

        if (getShowIndicatorInternal()) {
            when (getCurrentMode()) {
                PullToRefreshBase.Mode.PULL_FROM_END -> mIndicatorIvBottom!!.releaseToRefresh()
                PullToRefreshBase.Mode.PULL_FROM_START -> mIndicatorIvTop!!.releaseToRefresh()
                else -> {
                }
            }
        }
    }

     override fun onReset() {
        super.onReset()

        if (getShowIndicatorInternal()) {
            updateIndicatorViewsVisibility()
        }
    }

     override fun handleStyledAttributes(a: TypedArray) {

        mShowIndicator = a.getBoolean(R.styleable.PullToRefresh_ptrShowIndicator, !isPullToRefreshOverScrollEnabled())
    }

     override fun isReadyForPullStart(): Boolean {
        return isFirstItemVisible()
    }

     override fun isReadyForPullEnd(): Boolean {
        return isLastItemVisible()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (null != mEmptyView && !mScrollEmptyView) {
            mEmptyView!!.scrollTo(-l, -t)
        }
    }

     override fun updateUIForMode() {
        super.updateUIForMode()

        // Check Indicator Views consistent with new Mode
        if (getShowIndicatorInternal()) {
            addIndicatorViews()
        } else {
            removeIndicatorViews()
        }
    }

    private fun addIndicatorViews() {
        val mode = getMode()
        val refreshableViewWrapper = getRefreshableViewWrapper()

        if (mode.showHeaderLoadingLayout() && null == mIndicatorIvTop) {
            // If the mode can pull down, and we don't have one set already
            mIndicatorIvTop = IndicatorLayout(context, PullToRefreshBase.Mode.PULL_FROM_START)
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            params.rightMargin = resources.getDimensionPixelSize(R.dimen.indicator_right_padding)
            params.gravity = Gravity.TOP or Gravity.RIGHT
            refreshableViewWrapper.addView(mIndicatorIvTop, params)

        } else if (!mode.showHeaderLoadingLayout() && null != mIndicatorIvTop) {
            // If we can't pull down, but have a View then remove it
            refreshableViewWrapper.removeView(mIndicatorIvTop)
            mIndicatorIvTop = null
        }

        if (mode.showFooterLoadingLayout() && null == mIndicatorIvBottom) {
            // If the mode can pull down, and we don't have one set already
            mIndicatorIvBottom = IndicatorLayout(context, PullToRefreshBase.Mode.PULL_FROM_END)
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            params.rightMargin = resources.getDimensionPixelSize(R.dimen.indicator_right_padding)
            params.gravity = Gravity.BOTTOM or Gravity.RIGHT
            refreshableViewWrapper.addView(mIndicatorIvBottom, params)

        } else if (!mode.showFooterLoadingLayout() && null != mIndicatorIvBottom) {
            // If we can't pull down, but have a View then remove it
            refreshableViewWrapper.removeView(mIndicatorIvBottom)
            mIndicatorIvBottom = null
        }
    }

    private fun getShowIndicatorInternal(): Boolean {
        return mShowIndicator && isPullToRefreshEnabled()
    }

    private fun isFirstItemVisible(): Boolean {
        val adapter = mRefreshableView!!.adapter

        if (null == adapter || adapter.isEmpty) {
            if (DEBUG) {
                Log.d(LOG_TAG, "isFirstItemVisible. Empty View.")
            }
            return true

        } else {

            if (mRefreshableView!!.firstVisiblePosition <= 1) {
                val firstVisibleChild = mRefreshableView!!.getChildAt(0)
                if (firstVisibleChild != null) {
                    return firstVisibleChild.top >= mRefreshableView!!.top
                }
            }
        }

        return false
    }

    private fun
            isLastItemVisible(): Boolean {
        val adapter = mRefreshableView!!.adapter

        if (null == adapter || adapter.isEmpty) {
            if (DEBUG) {
                Log.d(LOG_TAG, "isLastItemVisible. Empty View.")
            }
            return true
        } else {
            val lastItemPosition = mRefreshableView!!.count - 1
            val lastVisiblePosition = mRefreshableView!!.lastVisiblePosition

            if (DEBUG) {
                Log.d(LOG_TAG, "isLastItemVisible. Last Item Position: " + lastItemPosition + " Last Visible Pos: "
                        + lastVisiblePosition)
            }


            if (lastVisiblePosition >= lastItemPosition - 1) {
                val childIndex = lastVisiblePosition - mRefreshableView!!.firstVisiblePosition
                val lastVisibleChild = mRefreshableView!!.getChildAt(childIndex)
                if (lastVisibleChild != null) {
                    return lastVisibleChild.bottom <= mRefreshableView!!.bottom
                }
            }
        }

        return false
    }

    private fun removeIndicatorViews() {
        if (null != mIndicatorIvTop) {
            getRefreshableViewWrapper().removeView(mIndicatorIvTop)
            mIndicatorIvTop = null
        }

        if (null != mIndicatorIvBottom) {
            getRefreshableViewWrapper().removeView(mIndicatorIvBottom)
            mIndicatorIvBottom = null
        }
    }

    private fun updateIndicatorViewsVisibility() {
        if (null != mIndicatorIvTop) {
            if (!isRefreshing() && isReadyForPullStart()) {
                if (!mIndicatorIvTop!!.isVisible()) {
                    mIndicatorIvTop!!.show()
                }
            } else {
                if (mIndicatorIvTop!!.isVisible()) {
                    mIndicatorIvTop!!.hide()
                }
            }
        }

        if (null != mIndicatorIvBottom) {
            if (!isRefreshing() && isReadyForPullEnd()) {
                if (!mIndicatorIvBottom!!.isVisible()) {
                    mIndicatorIvBottom!!.show()
                }
            } else {
                if (mIndicatorIvBottom!!.isVisible()) {
                    mIndicatorIvBottom!!.hide()
                }
            }
        }
    }
}