package com.example.qiniu.widget

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.logI
import kotlin.math.max
import kotlin.math.min

class CameraPreviewFrameView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null
    private val TAG = "CameraPreviewFrameView"

    companion object {
        private var mListener: Listener? = null

        private val mScaleListener = object : SimpleOnScaleGestureListener() {
            private var mScaleFactor = 1.0f

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                // factor > 1, zoom
                // factor < 1, pinch
                mScaleFactor *= detector.getScaleFactor()

                // Don't let the object get too small or too large.
                mScaleFactor = max(0.01, min(mScaleFactor.toDouble(), 1.0)).toFloat()
                return mListener != null && mListener?.onZoomValueChanged(mScaleFactor).orFalse
            }
        }

        private val mGestureListener = object : SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                mListener?.onSingleTapUp(e)
                return false
            }
        }
    }

    init {
        "initialize".logI(TAG)
        mScaleDetector = ScaleGestureDetector(context, mScaleListener)
        mGestureDetector = GestureDetector(context, mGestureListener)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        return if (!mGestureDetector?.onTouchEvent(event).orFalse) {
            mScaleDetector?.onTouchEvent(event).orFalse
        } else {
            false
        }
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun onSingleTapUp(e: MotionEvent?): Boolean

        fun onZoomValueChanged(factor: Float): Boolean
    }

}