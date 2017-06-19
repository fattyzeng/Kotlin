package com.okl.wechathelper.pulldown

import android.util.Log

/**
 * Created by lenovo on 2017/6/10.
 */
class Utils {
    val LOG_TAG = "PullToRefresh"

     fun warnDeprecation(depreacted: String, replacement: String) {
        Log.w(LOG_TAG, "You're using the deprecated $depreacted attr, please switch over to $replacement")
    }
}