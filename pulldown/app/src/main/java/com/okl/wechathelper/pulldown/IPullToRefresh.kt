package com.okl.wechathelper.pulldown

import android.view.View
import android.view.animation.Interpolator
import com.okl.wechathelper.pulldown.PullToRefreshBase.OnRefreshListener2



/**
 * Created by lenovo on 2017/6/10.
 */
interface IPullToRefresh<T:View> {

    fun demo(): Boolean


    fun getCurrentMode(): PullToRefreshBase.Mode


    fun getFilterTouchEvents(): Boolean


    fun getLoadingLayoutProxy(): ILoadingLayout


    fun getLoadingLayoutProxy(includeStart: Boolean, includeEnd: Boolean): ILoadingLayout


    fun getMode(): PullToRefreshBase.Mode


    fun getShowViewWhileRefreshing(): Boolean


    fun getState(): PullToRefreshBase.State


    fun isPullToRefreshEnabled(): Boolean


    fun isPullToRefreshOverScrollEnabled(): Boolean


    fun isRefreshing(): Boolean


    fun isScrollingWhileRefreshingEnabled(): Boolean


    fun onRefreshComplete()


    fun setFilterTouchEvents(filterEvents: Boolean)


    fun setMode(mode: PullToRefreshBase.Mode)


    fun setOnPullEventListener(listener: PullToRefreshBase.OnPullEventListener<T>)


    fun setOnRefreshListener(listener: PullToRefreshBase.OnRefreshListener<T>)


    fun setOnRefreshListener(listener: OnRefreshListener2<T>)


    fun setPullToRefreshOverScrollEnabled(enabled: Boolean)


    fun setRefreshing()


    fun setRefreshing(doScroll: Boolean)


    fun setScrollAnimationInterpolator(interpolator: Interpolator)


    fun setScrollingWhileRefreshingEnabled(scrollingWhileRefreshingEnabled: Boolean)


    fun setShowViewWhileRefreshing(showView: Boolean)
}