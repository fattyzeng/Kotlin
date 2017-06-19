package com.okl.wechathelper.pulldown

import android.graphics.Typeface
import android.graphics.drawable.Drawable

/**
 * Created by lenovo on 2017/6/16.
 */
interface ILoadingLayout1 {

    fun  onLoadingDrawable(drawable: Drawable);

    fun  setLastUpdateLabel(label:CharSequence)

    fun setOnPullLabel(label: CharSequence)

    fun setOnRefreshLabel(label: CharSequence)

    fun  setOnReleaseLabel(label: CharSequence)

    fun  setTypeFace(typeface: Typeface)


}