package com.okl.wechathelper.pulldown

import android.content.Context
import android.graphics.Matrix
import android.view.animation.Animation
import android.widget.FrameLayout
import android.view.View.GONE
import android.view.animation.RotateAnimation
import android.view.animation.LinearInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView.ScaleType
import com.okl.wechathelper.R.drawable.indicator_arrow
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.okl.wechathelper.R


/**
 * Created by lenovo on 2017/6/15.
 */
class IndicatorLayout:FrameLayout,Animation.AnimationListener{
    val DEFAULT_ROTATION_ANIMATION_DURATION = 150

    private var mInAnim: Animation
    private var mOutAnim: Animation
    private var mArrowImageView: ImageView

    private var mRotateAnimation: Animation
    private var mResetRotateAnimation: Animation

    constructor(context: Context, mode: PullToRefreshBase.Mode):  super(context) {

        mArrowImageView = ImageView(context)

        var arrowD = resources.getDrawable(R.drawable.indicator_arrow)
        mArrowImageView.setImageDrawable(arrowD)

        var padding = resources.getDimensionPixelSize(R.dimen.indicator_internal_padding)
        mArrowImageView.setPadding(padding, padding, padding, padding)
        addView(mArrowImageView)

        var inAnimResId: Int=0
        var outAnimResId: Int=0
        when (mode) {
            PullToRefreshBase.Mode.PULL_FROM_END -> {
                inAnimResId = R.anim.slide_in_from_bottom
                outAnimResId = R.anim.slide_out_to_bottom
                setBackgroundResource(R.drawable.indicator_bg_bottom)

                mArrowImageView.setScaleType(ScaleType.MATRIX)
                var matrix = Matrix()
                matrix.setRotate(180f, arrowD.intrinsicWidth / 2f, arrowD.intrinsicHeight / 2f)
                mArrowImageView.setImageMatrix(matrix)
            }
            PullToRefreshBase.Mode.PULL_FROM_START -> {
                inAnimResId = R.anim.slide_in_from_top
                outAnimResId = R.anim.slide_out_to_top
                setBackgroundResource(R.drawable.indicator_bg_top)
            }
        }

        mInAnim = AnimationUtils.loadAnimation(context, inAnimResId)
        mInAnim.setAnimationListener(this)

        mOutAnim = AnimationUtils.loadAnimation(context, outAnimResId)
        mOutAnim.setAnimationListener(this)

        var interpolator = LinearInterpolator()
        mRotateAnimation = RotateAnimation(0f, -180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f)
        mRotateAnimation.interpolator = interpolator
        mRotateAnimation.duration = DEFAULT_ROTATION_ANIMATION_DURATION.toLong()
        mRotateAnimation.fillAfter = true

        mResetRotateAnimation = RotateAnimation(-180f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f)
        mResetRotateAnimation.interpolator = interpolator
        mResetRotateAnimation.duration = DEFAULT_ROTATION_ANIMATION_DURATION.toLong()
        mResetRotateAnimation.fillAfter = true

    }

    fun isVisible(): Boolean {
        var currentAnim = animation
        if (null != currentAnim) {
            return mInAnim === currentAnim
        }

        return visibility == View.VISIBLE
    }

    fun hide() {
        startAnimation(mOutAnim)
    }

    fun show() {
        mArrowImageView.clearAnimation()
        startAnimation(mInAnim)
    }

    override fun onAnimationEnd(animation: Animation) {
        if (animation === mOutAnim) {
            mArrowImageView.clearAnimation()
            visibility = View.GONE
        } else if (animation === mInAnim) {
            visibility = View.VISIBLE
        }

        clearAnimation()
    }

    override fun onAnimationRepeat(animation: Animation) {
        // NO-OP
    }

    override fun onAnimationStart(animation: Animation) {
        visibility = View.VISIBLE
    }

    fun releaseToRefresh() {
        mArrowImageView.startAnimation(mRotateAnimation)
    }

    fun pullToRefresh() {
        mArrowImageView.startAnimation(mResetRotateAnimation)
    }
}