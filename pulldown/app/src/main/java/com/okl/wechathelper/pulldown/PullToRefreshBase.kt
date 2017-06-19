package com.okl.wechathelper.pulldown

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.okl.wechathelper.R

/**
 * Created by lenovo on 2017/6/10.
 */
abstract class PullToRefreshBase<T : View> : LinearLayout, IPullToRefresh<T> {
    val DEBUG = false

    val USE_HW_LAYERS = false;

    val LOG_TAG = "PullToRefresh";

    val FRICTION = 2.0f;

    val SMOOTH_SCROLL_DURATION_MS: Long = 200;
    val SMOOTH_SCROLL_LONG_DURATION_MS: Long = 325;
    val DEMO_SCROLL_INTERVAL: Long = 225;

    val STATE_STATE = "ptr_state";
    val STATE_MODE = "ptr_mode";
    val STATE_CURRENT_MODE = "ptr_current_mode";
    val STATE_SCROLLING_REFRESHING_ENABLED = "ptr_disable_scrolling";
    val STATE_SHOW_REFRESHING_VIEW = "ptr_show_refreshing_view";
    val STATE_SUPER = "ptr_super";

    // ===========================================================
    // Fields
    // ===========================================================

    var mTouchSlop: Int=0
    var mLastMotionX: Float=0f
    var mLastMotionY: Float=0f
    var mInitialMotionX: Float=0f
    var mInitialMotionY: Float=0f

    var mIsBeingDragged: Boolean=true
    var mState: State = State.RESET;
    var mMode: Mode = Mode.getDefault();

    var mCurrentMode: Mode= Mode.getDefault()
     override fun getMode(): Mode {


         return  mMode;
    }

    var mRefreshableView: T?=null
    var mRefreshableViewWrapper: FrameLayout?=null

    var mShowViewWhileRefreshing: Boolean = true;
    var mScrollingWhileRefreshingEnabled: Boolean = false;
    var mFilterTouchEvents: Boolean = true;
    var mOverScrollEnabled: Boolean = true;
    var mLayoutVisibilityChangesEnabled: Boolean = true;

    public var mScrollAnimationInterpolator: Interpolator?=null
    var mLoadingAnimationStyle: AnimationStyle= AnimationStyle.getDefault()

    var mHeaderLayout: LoadingLayout?=null
    var mFooterLayout: LoadingLayout?=null
    var mOnRefreshListener: OnRefreshListener<T>?=null
    var mOnRefreshListener2: OnRefreshListener2<T>?=null
    var mOnPullEventListener: OnPullEventListener<T>?=null

    var mCurrentSmoothScrollRunnable: SmoothScrollRunnable?=null


    constructor(context: Context) : super(context) {

        init(context, null);
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs);
    }

    constructor(context: Context, mode: Mode) : super(context) {
        mMode = mode;
        init(context, null);
    }

    constructor(context: Context, mode: Mode, animStyle: AnimationStyle) : super(context) {

        mMode = mode;
        mLoadingAnimationStyle = animStyle;
        init(context, null);
    }

    override fun getShowViewWhileRefreshing(): Boolean {
        return  mShowViewWhileRefreshing;
    }

    override fun getState(): State {
        return mState;
    }


    override fun setOnRefreshListener(listener: OnRefreshListener2<T>) {
        this.mOnRefreshListener2=listener;
    }
    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {


        var refreshableView: T = mRefreshableView!!;

        if (refreshableView is ViewGroup) {
            (refreshableView as ViewGroup).addView(child, index, params);
        } else {
            throw  UnsupportedOperationException("Refreshable View is not a ViewGroup so can't addView");
        }
    }

    override  fun demo(): Boolean {
        if (mMode.showHeaderLoadingLayout() && isReadyForPullStart()) {
            smoothScrollToAndBack(-getHeaderSize() * 2);
            return true;
        } else if (mMode.showFooterLoadingLayout() && isReadyForPullEnd()) {
            smoothScrollToAndBack(getFooterSize() * 2);
            return true;
        }

        return false;
    }

    override fun getCurrentMode(): Mode {
        return mCurrentMode;
    }

    override fun getFilterTouchEvents(): Boolean {
        return mFilterTouchEvents;
    }

    override fun getLoadingLayoutProxy(): ILoadingLayout {
        return getLoadingLayoutProxy(true, true);
    }

    override fun getLoadingLayoutProxy(includeStart: Boolean, includeEnd: Boolean):ILoadingLayout {
        return createLoadingLayoutProxy(includeStart, includeEnd) ;
    }


    fun isDisableScrollingWhileRefreshing(): Boolean {
        return !isScrollingWhileRefreshingEnabled();
    }

    override fun isPullToRefreshEnabled(): Boolean {
        return mMode.permitsPullToRefresh();
    }

    override fun isPullToRefreshOverScrollEnabled(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && mOverScrollEnabled
                && OverscrollHelper().isAndroidOverScrollEnabled(mRefreshableView!!);
    }

    override fun isRefreshing(): Boolean {
        return mState == State.REFRESHING || mState == State.MANUAL_REFRESHING;
    }

    override fun isScrollingWhileRefreshingEnabled(): Boolean {
        return mScrollingWhileRefreshingEnabled;
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        if (!isPullToRefreshEnabled()) {
            return false;
        }

        var action = event.getAction();

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
            return true;
        }

        when (action) {
            MotionEvent.ACTION_MOVE -> {
                if (!mScrollingWhileRefreshingEnabled && isRefreshing()) {
                    return true;
                }

                if (isReadyForPull()) {
                    var y = event.getY()
                    var x = event.getX();
                    var diff: Float = 0f
                    var oppositeDiff: Float = 0f
                    var absDiff: Float = 0f


                    when (getPullToRefreshScrollDirection()) {
                        Orientation.HORIZONTAL -> {
                            diff = x - mLastMotionX
                            oppositeDiff = y - mLastMotionY;

                        }
                        Orientation.VERTICAL -> {
                            diff = y - mLastMotionY;
                            oppositeDiff = x - mLastMotionX;
                        }
                    }
                    absDiff = Math.abs(diff);

                    if (absDiff > mTouchSlop && (!mFilterTouchEvents || absDiff > Math.abs(oppositeDiff))) {
                        if (mMode.showHeaderLoadingLayout() && diff >= 1f && isReadyForPullStart()) {
                            mLastMotionY = y;
                            mLastMotionX = x;
                            mIsBeingDragged = true;
                            if (mMode == Mode.BOTH) {
                                mCurrentMode = Mode.PULL_FROM_START;
                            }
                        } else if (mMode.showFooterLoadingLayout() && diff <= -1f && isReadyForPullEnd()) {
                            mLastMotionY = y;
                            mLastMotionX = x;
                            mIsBeingDragged = true;
                            if (mMode == Mode.BOTH) {
                                mCurrentMode = Mode.PULL_FROM_END;
                            }
                        }
                    }
                }
            }
            MotionEvent.ACTION_DOWN -> {
                if (isReadyForPull()) {
                    mInitialMotionY = event.getY()
                    mLastMotionY = event.getY()
                    mLastMotionX = event.getX()
                    mInitialMotionX = event.getX();
                    mIsBeingDragged = false;
                }
            }
        }

        return mIsBeingDragged;
    }

    override fun onRefreshComplete() {
        if (isRefreshing()) {
            setState(State.RESET,false);
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (!isPullToRefreshEnabled()) {
            return false;
        }

        // If we're refreshing, and the flag is set. Eat the event
        if (!mScrollingWhileRefreshingEnabled && isRefreshing()) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            return false;
        }

        when (event.getAction()) {
            MotionEvent.ACTION_MOVE -> {
                if (mIsBeingDragged) {
                    mLastMotionY = event.getY();
                    mLastMotionX = event.getX();
                    pullEvent();
                    return true;
                }
            }

            MotionEvent.ACTION_DOWN -> {
                if (isReadyForPull()) {
                    mInitialMotionY = event.getY();
                    mLastMotionY = event.getY();
                    mInitialMotionX = event.getX();
                    mLastMotionX = event.getX()

                    return true;
                }
            }

            MotionEvent.ACTION_UP -> {
                if (mIsBeingDragged) {
                    mIsBeingDragged = false;

                    if (mState == State.RELEASE_TO_REFRESH
                            && (null != mOnRefreshListener || null != mOnRefreshListener2)) {
                        setState(State.REFRESHING, true);
                        return true;
                    }

                    // If we're already refreshing, just scroll back to the top
                    if (isRefreshing()) {
                        smoothScrollTo(0);
                        return true;
                    }

                    // If we haven't returned by here, then we're not in a state
                    // to pull, so just reset
                    setState(State.RESET,false);

                    return true;
                }
            }
        }

        return false;
    }

    override  fun setScrollingWhileRefreshingEnabled(allowScrollingWhileRefreshing: Boolean) {
        mScrollingWhileRefreshingEnabled = allowScrollingWhileRefreshing;
    }



    fun setDisableScrollingWhileRefreshing(disableScrollingWhileRefreshing: Boolean) {
        setScrollingWhileRefreshingEnabled(!disableScrollingWhileRefreshing);
    }

    override   fun setFilterTouchEvents(filterEvents: Boolean) {
        mFilterTouchEvents = filterEvents;
    }


    fun setLastUpdatedLabel(label: CharSequence) {
        getLoadingLayoutProxy().setLastUpdatedLabel(label);
    }


    fun setLoadingDrawable(drawable: Drawable) {
        getLoadingLayoutProxy().setLoadingDrawable(drawable);
    }

    fun setLoadingDrawable(drawable: Drawable, mode: Mode) {
        getLoadingLayoutProxy(mode.showHeaderLoadingLayout(), mode.showFooterLoadingLayout()).setLoadingDrawable(
                drawable);
    }

    override fun setLongClickable(longClickable: Boolean) {
        getRefreshableView().setLongClickable(longClickable);
    }

    override  fun setMode(mode: Mode) {
        if (mode != mMode) {
            if (DEBUG) {
                Log.d(LOG_TAG, "Setting mode to: " + mode);
            }
            mMode = mode;
            updateUIForMode();
        }
    }

   override fun setOnPullEventListener( listener:OnPullEventListener<T>)
    {
        mOnPullEventListener = listener;
    }


     override fun setOnRefreshListener( listener:OnRefreshListener<T>)
    {
        mOnRefreshListener = listener;
    }

      fun  setOnRefreshListener1( listener:OnRefreshListener2<T>)
    {
        mOnRefreshListener2 = listener;
    }
     fun setPullLabel( pullLabel:CharSequence)
    {
        getLoadingLayoutProxy().setPullLabel(pullLabel);
    }
         fun setPullLabel( pullLabel:CharSequence,  mode:Mode){
        getLoadingLayoutProxy(mode.showHeaderLoadingLayout(), mode.showFooterLoadingLayout()).setPullLabel(pullLabel);
    }


      fun setPullToRefreshEnabled( enable:Boolean)
    {
        setMode(if(enable)  Mode . getDefault () else Mode.DISABLED);
    }

    override fun  setPullToRefreshOverScrollEnabled( enabled:Boolean)
    {
        mOverScrollEnabled = enabled;
    }

    override fun setRefreshing()
    {
        setRefreshing(true);
    }

    override fun setRefreshing(doScroll:Boolean)
    {
        if (!isRefreshing()) {
            setState(State.MANUAL_REFRESHING, doScroll);
        }
    }

     fun setRefreshingLabel( refreshingLabel:CharSequence)
    {
        getLoadingLayoutProxy().setRefreshingLabel(refreshingLabel);
    }

     fun setRefreshingLabel( refreshingLabel:CharSequence,  mode:Mode)
    {
        getLoadingLayoutProxy(mode.showHeaderLoadingLayout(), mode.showFooterLoadingLayout()).setRefreshingLabel(
                refreshingLabel);
    }

     fun setReleaseLabel( releaseLabel:CharSequence)
    {
        setReleaseLabel(releaseLabel, Mode.BOTH);
    }

     fun setReleaseLabel( releaseLabel:CharSequence,  mode:Mode)
    {
        getLoadingLayoutProxy(mode.showHeaderLoadingLayout(), mode.showFooterLoadingLayout()).setReleaseLabel(
                releaseLabel);
    }


    override fun setScrollAnimationInterpolator( interpolator:Interpolator)
    {
        mScrollAnimationInterpolator = interpolator;
    }

    override fun setShowViewWhileRefreshing( showView:Boolean)
    {
        mShowViewWhileRefreshing = showView;
    }

    abstract fun getPullToRefreshScrollDirection(): Orientation;

    fun setState(state:State){
        setState(state,false)
    }

    fun setState( state:State,  params:Boolean)
    {
        mState = state;
        if (DEBUG) {
            Log.d(LOG_TAG, "State: " + mState.name);
        }

        when(mState) {
             State.RESET->
            onReset();
            State.PULL_TO_REFRESH ->
            onPullToRefresh()
             State.RELEASE_TO_REFRESH->
            onReleaseToRefresh();
            State.REFRESHING->
                onRefreshing(params);
             State.MANUAL_REFRESHING ->
             onRefreshing(params)
        }

        if (null != mOnPullEventListener) {
            mOnPullEventListener!!.onPullEvent(this, mState, mCurrentMode);
        }
    }

    fun addViewInternal(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params);
    }

    fun addViewInternal(child: View, params: ViewGroup.LayoutParams) {
        super.addView(child, -1, params);
    }

    fun createLoadingLayout(context: Context, mode: Mode, attrs: TypedArray): LoadingLayout {
        var layout:LoadingLayout =mLoadingAnimationStyle!!.createLoadingLayout(context,mode,getPullToRefreshScrollDirection(),attrs)
        layout.setVisibility(View.INVISIBLE);
        return layout;
    }

    open fun  createLoadingLayoutProxy( includeStart:Boolean, includeEnd:Boolean):LoadingLayoutProxy
    {
        var proxy =  LoadingLayoutProxy();

        if (includeStart && mMode.showHeaderLoadingLayout()) {
            proxy.addLayout(mHeaderLayout!!);
        }
    if (includeEnd && mMode.showFooterLoadingLayout()) {
        proxy.addLayout(mFooterLayout!!);
    }

    return proxy;
}

abstract fun createRefreshableView(context: Context, attrs: AttributeSet?): T;

fun disableLoadingLayoutVisibilityChanges() {
    mLayoutVisibilityChangesEnabled = false;
}

fun getFooterLayout(): LoadingLayout {
    return mFooterLayout!!;
}

fun getFooterSize(): Int {
    return mFooterLayout!!.getContentSize();
}

fun getHeaderLayout(): LoadingLayout {
    return mHeaderLayout!!;
}

    fun getHeaderSize(): Int {
        return mHeaderLayout!!.getContentSize();
    }

    fun getPullToRefreshScrollDurationLonger(): Long {
        return SMOOTH_SCROLL_LONG_DURATION_MS;
    }

    fun getPullToRefreshScrollDuration(): Long {
        return SMOOTH_SCROLL_DURATION_MS;
    }
   fun  getRefreshableView():View{
       return  mRefreshableView!!;
   }

    fun getRefreshableViewWrapper():  FrameLayout{
        return mRefreshableViewWrapper!!;
    }

    open fun handleStyledAttributes(a: TypedArray) {
    }

    abstract fun isReadyForPullEnd(): Boolean
    abstract fun isReadyForPullStart(): Boolean

    fun onPtrRestoreInstanceState(savedInstanceState: Bundle) {
    }


    fun onPtrSaveInstanceState(saveState: Bundle) {
    }


    open fun onPullToRefresh() {
        when (mCurrentMode) {
            Mode.PULL_FROM_END ->
                mFooterLayout!!.pullToRefresh();
            Mode.PULL_FROM_END ->
                mHeaderLayout!!.pullToRefresh();

        }
    }


    public open fun onRefreshing(doScroll: Boolean) {
        if (mMode.showHeaderLoadingLayout()) {
            mHeaderLayout!!.refreshing();
        }
        if (mMode.showFooterLoadingLayout()) {
            mFooterLayout!!.refreshing();
        }

        if (doScroll) {
            if (mShowViewWhileRefreshing) {

                var listener = object : OnSmoothScrollFinishedListener {
                    override fun onSmoothScrollFinished() {
                        callRefreshListener()
                    }
                }


                when (mCurrentMode) {
                    Mode.MANUAL_REFRESH_ONLY->

                        smoothScrollTo(getFooterSize(), listener);
                    Mode.PULL_FROM_START -> {
                        smoothScrollTo(getFooterSize(), listener);
                    }
                    Mode.PULL_FROM_END ->
                        smoothScrollTo(-getHeaderSize(), listener);

                }
            } else {
                smoothScrollTo(0);
            }
        } else {
            callRefreshListener();
        }
    }

    open fun onReleaseToRefresh() {
        when (mCurrentMode) {
            Mode.PULL_FROM_START ->
                mFooterLayout!!.releaseToRefresh();
            Mode.PULL_FROM_END ->
                mHeaderLayout!!.releaseToRefresh();

        }
    }

    open fun onReset() {
        mIsBeingDragged = false;
        mLayoutVisibilityChangesEnabled = true;

        mHeaderLayout!!.reset();
        mFooterLayout!!.reset();

        smoothScrollTo(0);
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            var bundle = state as Bundle;

            setMode(Mode.mapIntToValue(bundle.getInt(STATE_MODE, 0)));
            mCurrentMode = Mode.mapIntToValue(bundle.getInt(STATE_CURRENT_MODE, 0));

            mScrollingWhileRefreshingEnabled = bundle.getBoolean(STATE_SCROLLING_REFRESHING_ENABLED, false);
            mShowViewWhileRefreshing = bundle.getBoolean(STATE_SHOW_REFRESHING_VIEW, true);

            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));

            var viewState = State.mapIntToValue(bundle.getInt(STATE_STATE, 0));
            if (viewState == State.REFRESHING || viewState == State.MANUAL_REFRESHING) {
                setState(viewState, true);
            }

            onPtrRestoreInstanceState(bundle);
            return;
        }

        super.onRestoreInstanceState(state);
    }

    override fun onSaveInstanceState(): Parcelable {
        var bundle = Bundle();

        onPtrSaveInstanceState(bundle);

        bundle.putInt(STATE_STATE, mState.getIntValue());
        bundle.putInt(STATE_MODE, mMode.getIntValue());
        bundle.putInt(STATE_CURRENT_MODE, mCurrentMode.getIntValue());
        bundle.putBoolean(STATE_SCROLLING_REFRESHING_ENABLED, mScrollingWhileRefreshingEnabled);
        bundle.putBoolean(STATE_SHOW_REFRESHING_VIEW, mShowViewWhileRefreshing);
        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());

        return bundle;
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (DEBUG) {
            Log.d(LOG_TAG, String.format("onSizeChanged. W: %d, H: %d", w, h));
        }

        super.onSizeChanged(w, h, oldw, oldh);

        refreshLoadingViewsSize();
        refreshRefreshableViewSize(w, h);



    var runable = object : Runnable {
        override fun run() {
            requestLayout();
        }
    }
    post(runable);
}
    fun refreshLoadingViewsSize() {
        var maximumPullScroll = (getMaximumPullScroll() * 6/5);

        var pLeft = getPaddingLeft();
        var pTop = getPaddingTop();
        var pRight = getPaddingRight();
        var pBottom = getPaddingBottom();

        when (getPullToRefreshScrollDirection()) {
            Orientation.HORIZONTAL -> {
                if (mMode.showHeaderLoadingLayout()) {
                    mHeaderLayout!!.setWidth(maximumPullScroll);
                    pLeft = -maximumPullScroll;
                } else {
                    pLeft = 0;
                }

                if (mMode.showFooterLoadingLayout()) {
                    mFooterLayout!!.setWidth(maximumPullScroll);
                    pRight = -maximumPullScroll;
                } else {
                    pRight = 0;
                }
            }
            Orientation.VERTICAL -> {
                if (mMode.showHeaderLoadingLayout()) {
                    mHeaderLayout!!.setHeight(maximumPullScroll);
                    pTop = -maximumPullScroll;
                } else {
                    pTop = 0;
                }

                if (mMode.showFooterLoadingLayout()) {
                    mFooterLayout!!.setHeight(maximumPullScroll);
                    pBottom = -maximumPullScroll;
                } else {
                    pBottom = 0;
                }
            }
        }

        if (DEBUG) {
            Log.d(LOG_TAG, String.format("Setting Padding. L: %d, T: %d, R: %d, B: %d", pLeft, pTop, pRight, pBottom));
        }
        setPadding(pLeft, pTop, pRight, pBottom);
    }

    fun refreshRefreshableViewSize(width: Int, height: Int) {
        var lp = mRefreshableViewWrapper!!.getLayoutParams() as LinearLayout.LayoutParams;

        when (getPullToRefreshScrollDirection()) {
            Orientation.HORIZONTAL -> {
                if (lp.width != width) {
                    lp.width = width;
                    mRefreshableViewWrapper!!.requestLayout();
                }
            }
            Orientation.VERTICAL -> {
                if (lp.height != height) {
                    lp.height = height;
                    mRefreshableViewWrapper!!.requestLayout();
                }
            }
        }
    }

    fun setHeaderScroll(value: Int) {
        if (DEBUG) {
            Log.d(LOG_TAG, "setHeaderScroll: " + value);
        }

        var maximumPullScroll = getMaximumPullScroll();
        var val1 = Math.min(maximumPullScroll, Math.max(-maximumPullScroll, value))
        if (mLayoutVisibilityChangesEnabled) {
            if (val1 < 0) {
                mHeaderLayout!!.setVisibility(View.VISIBLE);
            } else if (val1 > 0) {
                mFooterLayout!!.setVisibility(View.VISIBLE);
            } else {
                mHeaderLayout!!.setVisibility(View.INVISIBLE);
                mFooterLayout!!.setVisibility(View.INVISIBLE);
            }
        }



        when (getPullToRefreshScrollDirection()) {
            Orientation.VERTICAL ->
                scrollTo(0, value)
            Orientation.HORIZONTAL ->
                scrollTo(value, 0)
        }
    }


    fun smoothScrollTo(scrollValue: Int) {
        //smoothScrollTo(scrollValue);
    }

    fun smoothScrollTo(scrollValue: Int, listener: OnSmoothScrollFinishedListener) {
        smoothScrollTo(scrollValue, getPullToRefreshScrollDuration(), 0, listener);
    }

    fun smoothScrollToLonger(scrollValue: Int) {
        smoothScrollTo(scrollValue, getPullToRefreshScrollDurationLonger());
    }


    open fun updateUIForMode() {
        var lp = getLoadingLayoutLayoutParams();

        if (this == mHeaderLayout!!.getParent()) {
            removeView(mHeaderLayout);
        }
        if (mMode.showHeaderLoadingLayout()) {
            addViewInternal(mHeaderLayout!!, 0, lp);
        }

        if (this == mFooterLayout!!.getParent()) {
            removeView(mFooterLayout);
        }
        if (mMode.showFooterLoadingLayout()) {
            addViewInternal(mFooterLayout!!, lp);
        }

        refreshLoadingViewsSize();

        mCurrentMode = if (mMode != Mode.BOTH) mMode else Mode.PULL_FROM_START;
    }

    fun addRefreshableView(context: Context, refreshableView: T) {
        mRefreshableViewWrapper = FrameLayout(context);
        mRefreshableViewWrapper!!.addView(refreshableView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        addViewInternal(mRefreshableViewWrapper!!, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    fun callRefreshListener() {
        if (null != mOnRefreshListener) {
            mOnRefreshListener!!.onRefresh(this);
        } else if (null != mOnRefreshListener2) {
            if (mCurrentMode == Mode.PULL_FROM_START) {
                mOnRefreshListener2!!.onPullDownToRefresh(this);
            } else if (mCurrentMode == Mode.PULL_FROM_END) {
                mOnRefreshListener2!!.onPullUpToRefresh(this);
            }
        }
    }

    @SuppressWarnings("deprecation")
    fun init(context: Context, attrs: AttributeSet?) {
        when (getPullToRefreshScrollDirection()) {
            Orientation.HORIZONTAL ->
                setOrientation(LinearLayout.HORIZONTAL);
            Orientation.VERTICAL ->
                setOrientation(LinearLayout.VERTICAL);
        }

        setGravity(Gravity.CENTER);

        var config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();

        var a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefresh);

        if (a.hasValue(R.styleable.PullToRefresh_ptrMode)) {
            mMode = Mode.mapIntToValue(a.getInteger(R.styleable.PullToRefresh_ptrMode, 0));
        }

        if (a.hasValue(R.styleable.PullToRefresh_ptrAnimationStyle)) {
            mLoadingAnimationStyle = AnimationStyle.mapIntToValue(a.getInteger(
                    R.styleable.PullToRefresh_ptrAnimationStyle, 0));
        }

        mRefreshableView = createRefreshableView(context, attrs);
        addRefreshableView(context, mRefreshableView!!);

        mHeaderLayout = createLoadingLayout(context, Mode.PULL_FROM_START, a);
        mFooterLayout = createLoadingLayout(context, Mode.PULL_FROM_END, a);

        if (a.hasValue(R.styleable.PullToRefresh_ptrRefreshableViewBackground)) {
            var background = a.getDrawable(R.styleable.PullToRefresh_ptrRefreshableViewBackground);
            if (null != background) {
                mRefreshableView!!.setBackgroundDrawable(background);
            }
        } else if (a.hasValue(R.styleable.PullToRefresh_ptrAdapterViewBackground)) {
            Utils().warnDeprecation("ptrAdapterViewBackground", "ptrRefreshableViewBackground");
            var background = a.getDrawable(R.styleable.PullToRefresh_ptrAdapterViewBackground);
            if (null != background) {
                mRefreshableView!!.setBackgroundDrawable(background);
            }
        }

        if (a.hasValue(R.styleable.PullToRefresh_ptrOverScroll)) {
            mOverScrollEnabled = a.getBoolean(R.styleable.PullToRefresh_ptrOverScroll, true);
        }

        if (a.hasValue(R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled)) {
            mScrollingWhileRefreshingEnabled = a.getBoolean(
                    R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled, false);
        }

        handleStyledAttributes(a);
        a.recycle();

        updateUIForMode();
    }

    fun isReadyForPull(): Boolean {
        when (mMode) {
            Mode.PULL_FROM_START ->
                return isReadyForPullStart();
            Mode.PULL_FROM_END->
                return isReadyForPullEnd();
            Mode.BOTH ->
                return isReadyForPullEnd() || isReadyForPullStart();

        }
        return false;
    }

    fun pullEvent() {

        var newScrollValue: Int=0
        var itemDimension: Int=0

        var lastMotionValue: Float=0f
        var value:Float=0f
        when (getPullToRefreshScrollDirection()) {
            Orientation.HORIZONTAL -> {
                value = mInitialMotionX;
                lastMotionValue = mLastMotionX;
            }
            Orientation.VERTICAL -> {
                value = mInitialMotionY;
                lastMotionValue = mLastMotionY;
            }
        }

        when (mCurrentMode) {
            Mode.PULL_FROM_END -> {
                newScrollValue = Math.round(Math.max((value - lastMotionValue).toInt(), 0) / FRICTION).toInt();
                itemDimension = getFooterSize();
            }
            Mode.PULL_FROM_START-> {
                newScrollValue = Math.round(Math.min((value - lastMotionValue).toInt(), 0) / FRICTION).toInt();
                itemDimension = getHeaderSize();
            }

        }

        setHeaderScroll(newScrollValue);

        if (newScrollValue != 0 && !isRefreshing()) {
            var scale = Math . abs (newScrollValue) / (itemDimension.toFloat());
            when(mCurrentMode) {
                Mode.PULL_FROM_END->
                mFooterLayout!!.onPull(scale);
                Mode.PULL_FROM_START->
                mHeaderLayout!!.onPull(scale);
            }

            if (mState != State.PULL_TO_REFRESH && itemDimension >= Math.abs(newScrollValue)) {
                setState(State.PULL_TO_REFRESH,false);
            } else if (mState == State.PULL_TO_REFRESH && itemDimension < Math.abs(newScrollValue)) {
                setState(State.RELEASE_TO_REFRESH,false);
            }
        }
    }

    fun getLoadingLayoutLayoutParams(): LinearLayout.LayoutParams {
        when (getPullToRefreshScrollDirection()) {
            Orientation.HORIZONTAL ->
                return LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
            Orientation.VERTICAL ->
                return LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    fun getMaximumPullScroll(): Int {
        when (getPullToRefreshScrollDirection()) {
            Orientation.HORIZONTAL ->
                return Math.round(getWidth() / FRICTION);
            Orientation.VERTICAL ->
                return Math.round(getHeight() / FRICTION);
        }
    }

    fun smoothScrollTo(scrollValue: Int, duration: Long) {
        smoothScrollTo(scrollValue, duration, 0, null);
    }

    fun smoothScrollTo(newScrollValue: Int, duration: Long, delayMillis: Long,
                       listener: OnSmoothScrollFinishedListener?) {
        if (null != mCurrentSmoothScrollRunnable) {
            mCurrentSmoothScrollRunnable!!.stop();
        }

        var oldScrollValue: Int;
        when (getPullToRefreshScrollDirection()) {
            Orientation.HORIZONTAL ->
                oldScrollValue = getScrollX().toInt();
            Orientation.VERTICAL ->
                oldScrollValue = getScrollY().toInt();
        }

        if (oldScrollValue != newScrollValue) {
            if (null == mScrollAnimationInterpolator) {
                // Default interpolator is a Decelerate Interpolator
                mScrollAnimationInterpolator = DecelerateInterpolator();
            }
            mCurrentSmoothScrollRunnable = SmoothScrollRunnable(oldScrollValue, newScrollValue, duration, listener);

            if (delayMillis > 0) {
                postDelayed(mCurrentSmoothScrollRunnable, delayMillis);
            } else {
                post(mCurrentSmoothScrollRunnable);
            }
        }
    }

    fun smoothScrollToAndBack(y: Int) {
        var listener = object : OnSmoothScrollFinishedListener {
            override fun onSmoothScrollFinished() {
                smoothScrollTo(0, SMOOTH_SCROLL_DURATION_MS, DEMO_SCROLL_INTERVAL, null)
            }
        }
        smoothScrollTo(y, SMOOTH_SCROLL_DURATION_MS, 0, listener);
    }

    enum class AnimationStyle {

        ROTATE,

        FLIP;

        companion object {
            fun getDefault(): AnimationStyle {
                return ROTATE;
            }

            fun mapIntToValue(modeInt: Int): AnimationStyle {
                when (modeInt) {
                    0x0 ->
                        return ROTATE;
                    0x1 ->
                        return FLIP;
                }
                return ROTATE;
            }

        }


        fun createLoadingLayout( context:Context,  mode:Mode,  scrollDirection:Orientation,  attrs:TypedArray):LoadingLayout
        {
            when (this) {
                ROTATE->
                return  RotateLoadingLayout (context, mode, scrollDirection, attrs);
                 FLIP->
                     FlipLoadingLayout (context, mode, scrollDirection, attrs);
            }
            return  RotateLoadingLayout (context, mode, scrollDirection, attrs)
        }
    }

     enum class Mode()
    {

        DISABLED(0x0),


        PULL_FROM_START(0x1),


        PULL_FROM_END(0x2),

        BOTH(0x3),


        MANUAL_REFRESH_ONLY(0x4);
        var index:Int=0;
        constructor(index: Int){
            this.index=index
        }
        companion object{
            fun mapIntToValue( modeInt:Int):Mode{

                for (value in values()) {
                    if (modeInt==value.getIntValue()){
                      return value
                    }
                }
                return PULL_FROM_START;
            }
                fun getDefault():Mode {
                    return PULL_FROM_START;
                }





        }
        fun permitsPullToRefresh ():Boolean {
            return !( this=== DISABLED || this === MANUAL_REFRESH_ONLY);
        }

        fun showHeaderLoadingLayout():Boolean{
            return this == PULL_FROM_START || this == BOTH;
        }


        fun showFooterLoadingLayout():Boolean {
            return this == PULL_FROM_END || this == BOTH || this == MANUAL_REFRESH_ONLY;
        }

       fun getIntValue ():Int {
            return index
        }

    }


    interface OnLastItemVisibleListener {

        fun onLastItemVisible();

    }

    interface OnPullEventListener<V : View> {


        fun onPullEvent(refreshView: PullToRefreshBase<V>, state: State, direction: Mode);

    }


    interface OnRefreshListener<V : View> {


        fun onRefresh(refreshView: PullToRefreshBase<V>);

    }


      interface OnRefreshListener2<V : View>
    {
        fun onPullDownToRefresh(  refreshView:PullToRefreshBase < V >);


        fun onPullUpToRefresh(  refreshView:PullToRefreshBase < V >);

    }

     enum class Orientation
    {
        VERTICAL, HORIZONTAL;
    }

     enum class State
    {


        RESET(0x0),


        PULL_TO_REFRESH(0x1),


        RELEASE_TO_REFRESH(0x2),


        REFRESHING(0x8),


        MANUAL_REFRESHING(0x9),


        OVERSCROLLING(0x10);

        var index:Int=0;

        constructor(index: Int){
             this.index=index;
        }

        companion object{
            fun mapIntToValue(index: Int):State{
                for (value in State.values()){
                    if (index==value.getIntValue()){
                        return  value
                    }
                }
                return  RESET
            }
        }




        fun getIntValue ():Int {
            return index;
        }
    }

   inner   class SmoothScrollRunnable : Runnable {
        var mInterpolator: Interpolator? = null
        var mScrollToY: Int? = null
        var mScrollFromY: Int? = null
        var mDuration: Long? = null
        var mListener: OnSmoothScrollFinishedListener? = null

        var mContinueRunning: Boolean? = true
        var mStartTime: Long? = -1
        var mCurrentY: Int? = -1;

        constructor(fromY: Int, toY: Int, duration: Long, listener: OnSmoothScrollFinishedListener?) {
            mScrollFromY = fromY;
            mScrollToY = toY;
            mInterpolator = mScrollAnimationInterpolator;
            mDuration = duration;
            mListener = listener;
        }

        override fun run() {


            if (mStartTime == -1L) {
                mStartTime = System.currentTimeMillis();
            } else {


                var normalizedTime = (1000 * (System.currentTimeMillis() - mStartTime!!)) / mDuration!!;
                normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

                var deltaY = Math.round((mScrollFromY!! - mScrollToY!!)
                        * mInterpolator!!.getInterpolation(normalizedTime / 1000f)) ;
                mCurrentY = (mScrollFromY!! - deltaY).toInt();

                setHeaderScroll(mCurrentY!!);
            }

            if (mContinueRunning!! && mScrollToY != mCurrentY!!) {
                ViewCompat.postOnAnimation(this@PullToRefreshBase, this);
            } else {
                if (null != mListener) {
                    mListener!!.onSmoothScrollFinished();
                }
            }
        }

        fun stop() {
            mContinueRunning = false;

              removeCallbacks(this);

        }
    }

    interface OnSmoothScrollFinishedListener {
        fun onSmoothScrollFinished();
    }

}