package com.okl.wechathelper.pulldown

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.support.v4.view.ViewCompat
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.okl.wechathelper.R


/**
 * Created by lenovo on 2017/5/30.
 */
public abstract class LoadingLayout : FrameLayout, ILoadingLayout {

    val LOG_TAG = "PullToRefresh-LoadingLayout"

    val ANIMATION_INTERPOLATOR: Interpolator = LinearInterpolator()

    private var mInnerLayout: FrameLayout?=null

    protected var mHeaderImage: ImageView?=null
    protected var mHeaderProgress: ProgressBar?=null

    private var mUseIntrinsicAnimation: Boolean = false

    private var mHeaderText: TextView?=null
    private var mSubHeaderText: TextView?=null

    protected var mMode: PullToRefreshBase.Mode?=null
    protected var mScrollDirection: PullToRefreshBase.Orientation?=null


    private var mPullLabel: CharSequence?=null
    private var mRefreshingLabel: CharSequence?=null
    private var mReleaseLabel: CharSequence?=null

    constructor(context: Context, mode: PullToRefreshBase.Mode, scrollDirection: PullToRefreshBase.Orientation, attrs: TypedArray): super(context) {
        mMode = mode
        mScrollDirection = scrollDirection

        when (scrollDirection) {
            PullToRefreshBase.Orientation.HORIZONTAL -> LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header_horizontal, this)
            PullToRefreshBase.Orientation.VERTICAL -> LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header_vertical, this)
        }

        mInnerLayout = findViewById(R.id.fl_inner) as FrameLayout
        mHeaderText = mInnerLayout!!.findViewById(R.id.pull_to_refresh_text) as TextView
        mHeaderProgress = mInnerLayout!!.findViewById(R.id.pull_to_refresh_progress) as ProgressBar
        mSubHeaderText = mInnerLayout!!.findViewById(R.id.pull_to_refresh_sub_text) as TextView
        mHeaderImage = mInnerLayout!!.findViewById(R.id.pull_to_refresh_image) as ImageView

        val lp = mInnerLayout!!.layoutParams as FrameLayout.LayoutParams

        when (mode) {
            PullToRefreshBase.Mode.PULL_FROM_START -> {
                lp.gravity = if (scrollDirection === PullToRefreshBase.Orientation.VERTICAL) Gravity.TOP else Gravity.LEFT

                // Load in labels
                mPullLabel = context.getString(R.string.pull_to_refresh_from_bottom_pull_label)
                mRefreshingLabel = context.getString(R.string.pull_to_refresh_from_bottom_refreshing_label)
                mReleaseLabel = context.getString(R.string.pull_to_refresh_from_bottom_release_label)
            }

            PullToRefreshBase.Mode.PULL_FROM_END -> {
                lp.gravity = if (scrollDirection === PullToRefreshBase.Orientation.VERTICAL) Gravity.BOTTOM else Gravity.RIGHT

                mPullLabel = context.getString(R.string.pull_to_refresh_pull_label)
                mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label)
                mReleaseLabel = context.getString(R.string.pull_to_refresh_release_label)
            }
        }

        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
            val background = attrs.getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground)
            if (null != background) {
                ViewCompat.setBackground(this, background)
            }
        }

        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextAppearance)) {
            val styleID = TypedValue()
            attrs.getValue(R.styleable.PullToRefresh_ptrHeaderTextAppearance, styleID)
            setTextAppearance(styleID.data)
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrSubHeaderTextAppearance)) {
            val styleID = TypedValue()
            attrs.getValue(R.styleable.PullToRefresh_ptrSubHeaderTextAppearance, styleID)
            setSubTextAppearance(styleID.data)
        }

        // Text Color attrs need to be set after TextAppearance attrs
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextColor)) {
            val colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderTextColor)
            if (null != colors) {
                setTextColor(colors)
            }
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderSubTextColor)) {
            val colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderSubTextColor)
            if (null != colors) {
                setSubTextColor(colors)
            }
        }

        var imageDrawable: Drawable?=null
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawable)) {
            imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawable)
        }

        when (mode) {
            PullToRefreshBase.Mode.PULL_FROM_END ->{if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableStart)) {
                imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableStart)
            } else if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableTop)) {
                Utils().warnDeprecation("ptrDrawableTop", "ptrDrawableStart")
                imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableTop)
            }}

            PullToRefreshBase.Mode.PULL_FROM_START -> if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableEnd)) {
                imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableEnd)
            } else if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableBottom)) {
                Utils().warnDeprecation("ptrDrawableBottom", "ptrDrawableEnd")
                imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableBottom)
            }
        }

        if (null == imageDrawable) {
            imageDrawable = context.resources.getDrawable(getDefaultDrawableResId())
        }
        setLoadingDrawable(imageDrawable!!)

        reset()
    }

    fun setHeight(height: Int) {
        layoutParams.height = height
        requestLayout()
    }

    fun setWidth(width: Int) {
        layoutParams.width = width
        requestLayout()
    }

    fun getContentSize(): Int {
        when (mScrollDirection) {
            PullToRefreshBase.Orientation.HORIZONTAL -> return mInnerLayout!!.width
            PullToRefreshBase.Orientation.VERTICAL-> return mInnerLayout!!.height
        }
        return  mInnerLayout!!.height
    }

    fun hideAllViews() {
        if (View.VISIBLE == mHeaderText!!.visibility) {
            mHeaderText!!.visibility = View.INVISIBLE
        }
        if (View.VISIBLE == mHeaderProgress!!.visibility) {
            mHeaderProgress!!.visibility = View.INVISIBLE
        }
        if (View.VISIBLE == mHeaderImage!!.visibility) {
            mHeaderImage!!.visibility = View.INVISIBLE
        }
        if (View.VISIBLE == mSubHeaderText!!.visibility) {
            mSubHeaderText!!.visibility = View.INVISIBLE
        }
    }

    fun onPull(scaleOfLayout: Float) {
        if (!mUseIntrinsicAnimation) {
            onPullImpl(scaleOfLayout)
        }
    }

    fun pullToRefresh() {
        if (null != mHeaderText) {
            mHeaderText!!.text = mPullLabel
        }

        pullToRefreshImpl()
    }

    fun refreshing() {
        if (null != mHeaderText) {
            mHeaderText!!.text = mRefreshingLabel
        }

        if (mUseIntrinsicAnimation) {
            (mHeaderImage!!.drawable as AnimationDrawable).start()
        } else {
            refreshingImpl()
        }

        if (null != mSubHeaderText) {
            mSubHeaderText!!.visibility = View.GONE
        }
    }

    fun releaseToRefresh() {
        if (null != mHeaderText) {
            mHeaderText!!.text = mReleaseLabel
        }

        releaseToRefreshImpl()
    }

    fun reset() {
        if (null != mHeaderText) {
            mHeaderText!!.text = mPullLabel
        }
        mHeaderImage!!.visibility = View.VISIBLE

        if (mUseIntrinsicAnimation) {
            (mHeaderImage!!.drawable as AnimationDrawable).stop()
        } else {
            resetImpl()
        }

        if (null != mSubHeaderText) {
            if (TextUtils.isEmpty(mSubHeaderText!!.text)) {
                mSubHeaderText!!.visibility = View.GONE
            } else {
                mSubHeaderText!!.visibility = View.VISIBLE
            }
        }
    }

   override   fun setLastUpdatedLabel(label: CharSequence) {
        setSubHeaderText(label)
    }

    override fun setLoadingDrawable(imageDrawable: Drawable) {
        // Set Drawable
        mHeaderImage!!.setImageDrawable(imageDrawable)
        mUseIntrinsicAnimation = imageDrawable is AnimationDrawable

        onLoadingDrawableSet(imageDrawable)
    }

    override fun setPullLabel(pullLabel: CharSequence) {
        mPullLabel = pullLabel
    }

    override fun setRefreshingLabel(refreshingLabel: CharSequence) {
        mRefreshingLabel = refreshingLabel
    }

    override  fun setReleaseLabel(releaseLabel: CharSequence) {
        mReleaseLabel = releaseLabel
    }

    override  fun setTextTypeface(tf: Typeface) {
        mHeaderText!!.typeface = tf
    }

    fun showInvisibleViews() {
        if (View.INVISIBLE == mHeaderText!!.visibility) {
            mHeaderText!!.visibility = View.VISIBLE
        }
        if (View.INVISIBLE == mHeaderProgress!!.visibility) {
            mHeaderProgress!!.visibility = View.VISIBLE
        }
        if (View.INVISIBLE == mHeaderImage!!.visibility) {
            mHeaderImage!!.visibility = View.VISIBLE
        }
        if (View.INVISIBLE == mSubHeaderText!!.visibility) {
            mSubHeaderText!!.visibility = View.VISIBLE
        }
    }


    protected abstract fun getDefaultDrawableResId(): Int

    protected abstract fun onLoadingDrawableSet(imageDrawable: Drawable)

    protected abstract fun onPullImpl(scaleOfLayout: Float)

    protected abstract fun pullToRefreshImpl()

    protected abstract fun refreshingImpl()

    protected abstract fun releaseToRefreshImpl()

    protected abstract fun resetImpl()

    private fun setSubHeaderText(label: CharSequence) {
        if (null != mSubHeaderText) {
            if (TextUtils.isEmpty(label)) {
                mSubHeaderText!!.visibility = View.GONE
            } else {
                mSubHeaderText!!.text = label


                if (View.GONE == mSubHeaderText!!.visibility) {
                    mSubHeaderText!!.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setSubTextAppearance(value: Int) {
        if (null != mSubHeaderText) {
            mSubHeaderText!!.setTextAppearance(context, value)
        }
    }

    private fun setSubTextColor(color: ColorStateList) {
        if (null != mSubHeaderText) {
            mSubHeaderText!!.setTextColor(color)
        }
    }

    private fun setTextAppearance(value: Int) {
        if (null != mHeaderText) {
            mHeaderText!!.setTextAppearance(context, value)
        }
        if (null != mSubHeaderText) {
            mSubHeaderText!!.setTextAppearance(context, value)
        }
    }

    private fun setTextColor(color: ColorStateList) {
        if (null != mHeaderText) {
            mHeaderText!!.setTextColor(color)
        }
        if (null != mSubHeaderText) {
            mSubHeaderText!!.setTextColor(color)
        }
    }


}