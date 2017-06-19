package com.okl.wechathelper.pulldown

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import java.util.*

/**
 * Created by lenovo on 2017/6/12.
 */
 class LoadingLayoutProxy: ILoadingLayout {

    var mLoadingLayouts: HashSet<LoadingLayout>? = null;

    constructor() {
        mLoadingLayouts = HashSet<LoadingLayout>();
    }

    fun addLayout(layout: LoadingLayout) {
        if (null != layout) {
            mLoadingLayouts!!.add(layout);
        }
    }

    override fun setLastUpdatedLabel(label: CharSequence) {
        for (layout in mLoadingLayouts!!) {
            layout.setLastUpdatedLabel(label);
        }
    }

    override fun setLoadingDrawable(drawable: Drawable) {
        for (layout in mLoadingLayouts!!) {
            layout.setLoadingDrawable(drawable);
        }
    }

    override fun setRefreshingLabel(refreshingLabel: CharSequence) {
        for (layout in mLoadingLayouts!!) {
            layout.setRefreshingLabel(refreshingLabel);
        }
    }


    override fun setPullLabel(pullLabel: CharSequence) {
        for (layout in mLoadingLayouts!!) {
            layout.setPullLabel(pullLabel)
        }

    }

    override fun setReleaseLabel(releaseLabel: CharSequence) {
        for (layout in mLoadingLayouts!!) {
            layout.setReleaseLabel(releaseLabel)
        }
    }

    override fun setTextTypeface(tf: Typeface) {
        for (layout in mLoadingLayouts!! ){
            layout.setTextTypeface(tf)
        }
    }
}