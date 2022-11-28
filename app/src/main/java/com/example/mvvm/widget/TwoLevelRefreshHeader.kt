package com.example.mvvm.widget

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentActivity
import com.example.base.utils.function.value.orZero
import com.example.mvvm.R
import com.scwang.smart.drawable.ProgressDrawable
import com.scwang.smart.refresh.classics.ArrowDrawable
import com.scwang.smart.refresh.classics.ClassicsAbstract
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.scwang.smart.refresh.layout.util.SmartUtil
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * @description 自定义头部
 * @author yan
 */
class TwoLevelRefreshHeader @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ClassicsAbstract<TwoLevelRefreshHeader>(context, attrs, defStyleAttr), RefreshHeader {
    protected var KEY_LAST_UPDATE_TIME = "LAST_UPDATE_TIME"
    protected var mLastTime: Date? = null
    protected var mLastUpdateText: TextView? = null
    protected var mShared: SharedPreferences? = null
    protected var mLastUpdateFormat: DateFormat? = null
    protected var mEnableLastTime = true
    protected var mTextPulling: String? = null//"下拉可以刷新";

    protected var mTextRefreshing: String? = null//"正在刷新...";
    protected var mTextLoading: String? = null//"正在加载...";
    protected var mTextRelease: String? = null//"释放立即刷新";
    protected var mTextFinish: String? = null//"刷新完成";
    protected var mTextFailed: String? = null//"刷新失败";
    protected var mTextUpdate: String? = null//"上次更新 M-d HH:mm";
    protected var mTextSecondary: String? = null//"释放进入二楼";

    init {
        inflate(context, R.layout.view_two_level_refresh_header, this)

        mArrowView = findViewById(R.id.srl_classics_arrow)
        mLastUpdateText = findViewById(R.id.srl_classics_update)
        mProgressView = findViewById(R.id.srl_classics_progress)
        mTitleText = findViewById(R.id.srl_classics_title)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.ClassicsHeader)
        val lpArrow = mArrowView.layoutParams as LayoutParams
        val lpProgress = mProgressView.layoutParams as LayoutParams
        val lpUpdateText = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lpUpdateText.topMargin = ta.getDimensionPixelSize(R.styleable.ClassicsHeader_srlTextTimeMarginTop, SmartUtil.dp2px(0f))
        lpProgress.rightMargin = ta.getDimensionPixelSize(R.styleable.ClassicsHeader_srlDrawableMarginRight, SmartUtil.dp2px(20f))
        lpArrow.rightMargin = lpProgress.rightMargin
        lpArrow.width = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableArrowSize, lpArrow.width)
        lpArrow.height = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableArrowSize, lpArrow.height)
        lpProgress.width = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableProgressSize, lpProgress.width)
        lpProgress.height = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableProgressSize, lpProgress.height)
        lpArrow.width = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableSize, lpArrow.width)
        lpArrow.height = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableSize, lpArrow.height)
        lpProgress.width = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableSize, lpProgress.width)
        lpProgress.height = ta.getLayoutDimension(R.styleable.ClassicsHeader_srlDrawableSize, lpProgress.height)

        mFinishDuration = ta.getInt(R.styleable.ClassicsHeader_srlFinishDuration, mFinishDuration)
        mEnableLastTime = ta.getBoolean(R.styleable.ClassicsHeader_srlEnableLastTime, mEnableLastTime)
        mSpinnerStyle = SpinnerStyle.values[ta.getInt(R.styleable.ClassicsHeader_srlClassicsSpinnerStyle, mSpinnerStyle.ordinal)]
        if (ta.hasValue(R.styleable.ClassicsHeader_srlDrawableArrow)) {
            mArrowView.setImageDrawable(ta.getDrawable(R.styleable.ClassicsHeader_srlDrawableArrow))
        } else if (mArrowView.drawable == null) {
            mArrowDrawable = ArrowDrawable()
            mArrowDrawable.setColor(-0x99999a)
            mArrowView.setImageDrawable(mArrowDrawable)
        }
        if (ta.hasValue(R.styleable.ClassicsHeader_srlDrawableProgress)) {
            mProgressView.setImageDrawable(ta.getDrawable(R.styleable.ClassicsHeader_srlDrawableProgress))
        } else if (mProgressView.drawable == null) {
            mProgressDrawable = ProgressDrawable()
            mProgressDrawable.setColor(-0x99999a)
            mProgressView.setImageDrawable(mProgressDrawable)
        }
        if (ta.hasValue(R.styleable.ClassicsHeader_srlTextSizeTitle)) {
            mTitleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, ta.getDimensionPixelSize(R.styleable.ClassicsHeader_srlTextSizeTitle, SmartUtil.dp2px(16f)).toFloat())
        }
        if (ta.hasValue(R.styleable.ClassicsHeader_srlTextSizeTime)) {
            mLastUpdateText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, ta.getDimensionPixelSize(R.styleable.ClassicsHeader_srlTextSizeTime, SmartUtil.dp2px(12f)).toFloat())
        }
        if (ta.hasValue(R.styleable.ClassicsHeader_srlPrimaryColor)) {
            super.setPrimaryColor(ta.getColor(R.styleable.ClassicsHeader_srlPrimaryColor, 0))
        }
        if (ta.hasValue(R.styleable.ClassicsHeader_srlAccentColor)) {
            setAccentColor(ta.getColor(R.styleable.ClassicsHeader_srlAccentColor, 0))
        }
        mTextPulling = if (ta.hasValue(R.styleable.ClassicsHeader_srlTextPulling)) {
            ta.getString(R.styleable.ClassicsHeader_srlTextPulling)
        } else {
            context.getString(R.string.srl_header_pulling)
        }
        mTextLoading = if (ta.hasValue(R.styleable.ClassicsHeader_srlTextLoading)) {
            ta.getString(R.styleable.ClassicsHeader_srlTextLoading)
        } else {
            context.getString(R.string.srl_header_loading)
        }
        mTextRelease = if (ta.hasValue(R.styleable.ClassicsHeader_srlTextRelease)) {
            ta.getString(R.styleable.ClassicsHeader_srlTextRelease)
        } else {
            context.getString(R.string.srl_header_release)
        }
        mTextFinish = if (ta.hasValue(R.styleable.ClassicsHeader_srlTextFinish)) {
            ta.getString(R.styleable.ClassicsHeader_srlTextFinish)
        } else {
            context.getString(R.string.srl_header_finish)
        }
        mTextFailed = if (ta.hasValue(R.styleable.ClassicsHeader_srlTextFailed)) {
            ta.getString(R.styleable.ClassicsHeader_srlTextFailed)
        } else {
            context.getString(R.string.srl_header_failed)
        }
        mTextSecondary = if (ta.hasValue(R.styleable.ClassicsHeader_srlTextSecondary)) {
            ta.getString(R.styleable.ClassicsHeader_srlTextSecondary)
        } else {
            context.getString(R.string.srl_header_secondary)
        }
        mTextRefreshing = if (ta.hasValue(R.styleable.ClassicsHeader_srlTextRefreshing)) {
            ta.getString(R.styleable.ClassicsHeader_srlTextRefreshing)
        } else {
            context.getString(R.string.srl_header_refreshing)
        }
        mTextUpdate = if (ta.hasValue(R.styleable.ClassicsHeader_srlTextUpdate)) {
            ta.getString(R.styleable.ClassicsHeader_srlTextUpdate)
        } else {
            context.getString(R.string.srl_header_update)
        }
        mLastUpdateFormat = SimpleDateFormat(mTextUpdate, Locale.getDefault())
        ta.recycle()

        mProgressView.animate().interpolator = null
        mLastUpdateText?.visibility = if (mEnableLastTime) VISIBLE else GONE
        mTitleText.text = if (isInEditMode) mTextRefreshing else mTextPulling
        if (isInEditMode) {
            mArrowView.visibility = GONE
        } else {
            mProgressView.visibility = GONE
        }
        try { //try 不能删除-否则会出现兼容性问题
            if (context is FragmentActivity) {
                if (context.supportFragmentManager.fragments.isNotEmpty()) {
                    setLastUpdateTime(Date())
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        KEY_LAST_UPDATE_TIME += context.javaClass.name
        mShared = context.getSharedPreferences("ClassicsHeader", Context.MODE_PRIVATE)
        setLastUpdateTime(Date(mShared?.getLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis()).orZero))
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        if (success) {
            mTitleText.text = mTextFinish
            if (mLastTime != null) {
                setLastUpdateTime(Date())
            }
        } else {
            mTitleText.text = mTextFailed
        }
        return super.onFinish(refreshLayout, success) //延迟500毫秒之后再弹回
    }

    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
        val arrowView: View = mArrowView
        val updateView: View = mLastUpdateText!!
        when (newState) {
            RefreshState.None -> {
                updateView.visibility = if (mEnableLastTime) VISIBLE else GONE
                mTitleText.text = mTextPulling
                arrowView.visibility = VISIBLE
                arrowView.animate().rotation(0f)
            }
            RefreshState.PullDownToRefresh -> {
                mTitleText.text = mTextPulling
                arrowView.visibility = VISIBLE
                arrowView.animate().rotation(0f)
            }
            RefreshState.Refreshing, RefreshState.RefreshReleased -> {
                mTitleText.text = mTextRefreshing
                arrowView.visibility = GONE
            }
            RefreshState.ReleaseToRefresh -> {
                mTitleText.text = mTextRelease
                arrowView.animate().rotation(180f)
            }
            RefreshState.ReleaseToTwoLevel -> {
                mTitleText.text = mTextSecondary
                arrowView.animate().rotation(0f)
            }
            RefreshState.Loading -> {
                arrowView.visibility = GONE
                updateView.visibility = if (mEnableLastTime) INVISIBLE else GONE
                mTitleText.text = mTextLoading
            }
            else -> {}
        }
    }

    fun setLastUpdateTime(time: Date): TwoLevelRefreshHeader {
        mLastTime = time
        mLastUpdateText?.text = mLastUpdateFormat?.format(time)
        if (mShared != null && !isInEditMode) {
            mShared?.edit()?.putLong(KEY_LAST_UPDATE_TIME, time.time)?.apply()
        }
        return this
    }

    fun setTimeFormat(format: DateFormat): TwoLevelRefreshHeader {
        mLastUpdateFormat = format
        mLastUpdateText?.text = mLastUpdateFormat?.format(mLastTime ?: Date())
        return this
    }

    fun setLastUpdateText(text: CharSequence?): TwoLevelRefreshHeader {
        mLastTime = null
        mLastUpdateText?.text = text
        return this
    }

    override fun setAccentColor(@ColorInt accentColor: Int): TwoLevelRefreshHeader {
        mLastUpdateText?.setTextColor(accentColor and 0x00ffffff or -0x34000000)
        return super.setAccentColor(accentColor)
    }

    fun setEnableLastTime(enable: Boolean): TwoLevelRefreshHeader {
        mEnableLastTime = enable
        mLastUpdateText?.visibility = if (enable) VISIBLE else GONE
        mRefreshKernel?.requestRemeasureHeightFor(this)
        return this
    }

    fun setTextSizeTime(size: Float): TwoLevelRefreshHeader {
        mLastUpdateText?.textSize = size
        mRefreshKernel?.requestRemeasureHeightFor(this)
        return this
    }

    fun setTextSizeTime(unit: Int, size: Float): TwoLevelRefreshHeader {
        mLastUpdateText?.setTextSize(unit, size)
        mRefreshKernel?.requestRemeasureHeightFor(this)
        return this
    }

    fun setTextTimeMarginTop(dp: Float): TwoLevelRefreshHeader {
        val lp = mLastUpdateText?.layoutParams as? MarginLayoutParams
        lp?.topMargin = SmartUtil.dp2px(dp)
        mLastUpdateText?.layoutParams = lp
        return this
    }

    fun setTextTimeMarginTopPx(px: Int): TwoLevelRefreshHeader {
        val lp = mLastUpdateText?.layoutParams as? MarginLayoutParams
        lp?.topMargin = px
        mLastUpdateText?.layoutParams = lp
        return this
    }

}