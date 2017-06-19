package com.okl.wechathelper.pulldown

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView.ScaleType
import com.okl.wechathelper.R


/**
 * Created by lenovo on 2017/5/30.
 */
class RotateLoadingLayout :LoadingLayout {
    val ROTATION_ANIMATION_DURATION = 1200

    private var mRotateAnimation: Animation?=null
    private var mHeaderImageMatrix: Matrix?=null

    private var mRotationPivotX: Float = 0.toFloat()
    private var mRotationPivotY:Float = 0.toFloat()

    private var mRotateDrawableWhilePulling: Boolean?=null

    constructor(context: Context, mode: PullToRefreshBase.Mode, scrollDirection: PullToRefreshBase.Orientation, attrs: TypedArray):         super(context, mode, scrollDirection, attrs) {


        mRotateDrawableWhilePulling = attrs.getBoolean(R.styleable.PullToRefresh_ptrRotateDrawableWhilePulling, true)

        mHeaderImage!!.scaleType = ScaleType.MATRIX
        mHeaderImageMatrix = Matrix()
        mHeaderImage!!.imageMatrix = mHeaderImageMatrix

        mRotateAnimation = RotateAnimation(0f, 720f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f)
        mRotateAnimation!!.interpolator = ANIMATION_INTERPOLATOR
        mRotateAnimation!!.duration = ROTATION_ANIMATION_DURATION.toLong()
        mRotateAnimation!!.repeatCount = Animation.INFINITE
        mRotateAnimation!!.repeatMode = Animation.RESTART
    }

    public override fun onLoadingDrawableSet(imageDrawable: Drawable) {
        if (null != imageDrawable) {
            mRotationPivotX = Math.round(imageDrawable.intrinsicWidth / 2f).toFloat()
            mRotationPivotY = Math.round(imageDrawable.intrinsicHeight / 2f).toFloat()
        }
    }

    override fun onPullImpl(scaleOfLayout: Float) {
        val angle: Float
        if (mRotateDrawableWhilePulling!!) {
            angle = scaleOfLayout * 90f
        } else {
            angle = Math.max(0f, Math.min(180f, scaleOfLayout * 360f - 180f))
        }

        mHeaderImageMatrix!!.setRotate(angle, mRotationPivotX, mRotationPivotY)
        mHeaderImage!!.imageMatrix = mHeaderImageMatrix
    }

    override fun refreshingImpl() {
        mHeaderImage!!.startAnimation(mRotateAnimation)
    }

    override fun resetImpl() {
        mHeaderImage!!.clearAnimation()
        resetImageRotation()
    }

    private fun resetImageRotation() {
        if (null != mHeaderImageMatrix) {
            mHeaderImageMatrix!!.reset()
            mHeaderImage!!.imageMatrix = mHeaderImageMatrix
        }
    }

    override fun pullToRefreshImpl() {
        // NO-OP
    }

    override fun releaseToRefreshImpl() {
        // NO-OP
    }

    override fun getDefaultDrawableResId(): Int {
        return R.drawable.default_ptr_rotate
    }

}