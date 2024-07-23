package com.example.qiniu.widget.rotate

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.example.framework.utils.function.value.toSafeLong
import com.example.framework.utils.logI
import com.example.qiniu.R
import com.qiniu.pili.droid.streaming.ui.FocusIndicator

// A view that indicates the focus area or the metering area.
@SuppressLint("UseCompatLoadingForDrawables")
class FocusIndicatorRotateLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : RotateLayout(context, attrs), FocusIndicator {
    private val TAG = "FocusIndicatorLayout"
    // Sometimes continuous autofucus starts and stops several times quickly.
    // These states are used to make sure the animation is run for at least some
    // time.
    private var mState = 0
    private val STATE_IDLE = 0
    private val STATE_FOCUSING = 1
    private val STATE_FINISHING = 2
    private val SCALING_UP_TIME = 1000
    private val SCALING_DOWN_TIME = 200
    private val DISAPPEAR_TIMEOUT = 200
    private val mDisappear by lazy { Runnable {
        mChild?.setBackgroundDrawable(null)
        mState = STATE_IDLE
    }}
    private val mEndAction by lazy { Runnable {
        // Keep the focus indicator for some time.
        postDelayed(mDisappear, DISAPPEAR_TIMEOUT.toSafeLong())
    }}

    private fun setDrawable(resid: Int) {
        mChild?.setBackgroundDrawable(resources.getDrawable(resid))
    }

    override fun showStart() {
        "showStart".logI(TAG)
        if (mState == STATE_IDLE) {
            setDrawable(R.drawable.ic_focus_focusing)
            animate()
                .withLayer()
                .setDuration(SCALING_UP_TIME.toSafeLong())
                .scaleX(1.5f)
                .scaleY(1.5f)
            mState = STATE_FOCUSING
        }
    }

    override fun showSuccess(timeout: Boolean) {
        "showSuccess".logI(TAG)
        if (mState == STATE_FOCUSING) {
            setDrawable(R.drawable.ic_focus_focused)
            animate()
                .withLayer()
                .setDuration(SCALING_DOWN_TIME.toSafeLong())
                .scaleX(1f)
                .scaleY(1f).withEndAction(if (timeout) mEndAction else null)
            mState = STATE_FINISHING
        }
    }

    override fun showFail(timeout: Boolean) {
        "showFail".logI(TAG)
        if (mState == STATE_FOCUSING) {
            setDrawable(R.drawable.ic_focus_failed)
            animate()
                .withLayer()
                .setDuration(SCALING_DOWN_TIME.toSafeLong())
                .scaleX(1f)
                .scaleY(1f).withEndAction(if (timeout) mEndAction else null)
            mState = STATE_FINISHING
        }
    }

    override fun clear() {
        "clear".logI(TAG)
        animate().cancel()
        removeCallbacks(mDisappear)
        mDisappear.run()
        scaleX = 1f
        scaleY = 1f
    }

}