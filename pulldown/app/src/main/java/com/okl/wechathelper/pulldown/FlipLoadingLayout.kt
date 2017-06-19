package com.okl.wechathelper.pulldown

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView.ScaleType
import com.okl.wechathelper.R


/**
 * Created by lenovo on 2017/5/30.
 */
class FlipLoadingLayout :LoadingLayout {

    val FLIP_ANIMATION_DURATION = 150

    private var mRotateAnimation: Animation?=null
    private var mResetRotateAnimation: Animation?=null

    constructor(context: Context, mode: PullToRefreshBase.Mode, scrollDirection: PullToRefreshBase.Orientation, attrs: TypedArray): super(context, mode, scrollDirection, attrs) {


        val rotateAngle = if (mode === PullToRefreshBase.Mode.PULL_FROM_START) -180 else 180

        mRotateAnimation = RotateAnimation(0f, rotateAngle.toFloat(), Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f)
        mRotateAnimation!!.interpolator = ANIMATION_INTERPOLATOR
        mRotateAnimation!!.duration = FLIP_ANIMATION_DURATION.toLong()
        mRotateAnimation!!.fillAfter = true

        mResetRotateAnimation = RotateAnimation(rotateAngle.toFloat(), 0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f)
        mResetRotateAnimation!!.interpolator = ANIMATION_INTERPOLATOR
        mResetRotateAnimation!!.duration = FLIP_ANIMATION_DURATION.toLong()
        mResetRotateAnimation!!.fillAfter = true
    }

    override fun onLoadingDrawableSet(imageDrawable: Drawable) {
        if (null != imageDrawable) {
            val dHeight = imageDrawable.intrinsicHeight
            val dWidth = imageDrawable.intrinsicWidth


            val lp = mHeaderImage!!.layoutParams
            lp.height = Math.max(dHeight, dWidth)
            lp.width = lp.height
            mHeaderImage!!.requestLayout()


            mHeaderImage!!.scaleType = ScaleType.MATRIX
            val matrix = Matrix()
            matrix.postTranslate((lp.width - dWidth) / 2f, (lp.height - dHeight) / 2f)
            matrix.postRotate(getDrawableRotationAngle(), lp.width / 2f, lp.height / 2f)
            mHeaderImage!!.imageMatrix = matrix
        }
    }

    override fun onPullImpl(scaleOfLayout: Float) {
        // NO-OP
    }

    override fun pullToRefreshImpl() {

        if (mRotateAnimation === mHeaderImage!!.animation) {
            mHeaderImage!!.startAnimation(mResetRotateAnimation)
        }
    }

    override fun refreshingImpl() {
        mHeaderImage!!.clearAnimation()
        mHeaderImage!!.visibility = View.INVISIBLE
        mHeaderProgress!!.visibility = View.VISIBLE
    }

    override fun releaseToRefreshImpl() {
        mHeaderImage!!.startAnimation(mRotateAnimation)
    }

    override fun resetImpl() {
        mHeaderImage!!.clearAnimation()
        mHeaderProgress!!.visibility = View.GONE
        mHeaderImage!!.visibility = View.VISIBLE
    }

    override fun getDefaultDrawableResId(): Int {
        return R.drawable.default_ptr_flip
    }

    private fun getDrawableRotationAngle(): Float {
        var angle = 0f
        when (mMode) {
            PullToRefreshBase.Mode.PULL_FROM_START -> {if (mScrollDirection === PullToRefreshBase.Orientation.HORIZONTAL) {
                angle = 90f
            } else {
                angle = 180f
            }
            }

            PullToRefreshBase.Mode.PULL_FROM_START -> {
                if (mScrollDirection === PullToRefreshBase.Orientation.HORIZONTAL) {
                    angle = 270f
                }
            }
        }

        return angle
    }
}