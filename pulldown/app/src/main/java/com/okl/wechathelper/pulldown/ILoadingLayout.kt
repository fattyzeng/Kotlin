package com.okl.wechathelper.pulldown

import android.graphics.Typeface
import android.graphics.drawable.Drawable



/**
 * Created by lenovo on 2017/5/29.
 */
interface ILoadingLayout {


    fun setLastUpdatedLabel(label: CharSequence)


    fun setLoadingDrawable(drawable: Drawable)


    fun setPullLabel(pullLabel: CharSequence)


    fun setRefreshingLabel(refreshingLabel: CharSequence)


    fun setReleaseLabel(releaseLabel: CharSequence)


    fun setTextTypeface(tf: Typeface)
}

