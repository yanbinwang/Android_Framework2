package com.example.qiniu.widget

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.example.framework.utils.function.value.orFalse

@SuppressLint("ClickableViewAccessibility")
class CameraPreviewFrameView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : GLSurfaceView(context, attrs) {
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null

    init {
        mScaleDetector = ScaleGestureDetector(context, mScaleListener)
        mGestureDetector = GestureDetector(context, mGestureListener)
    }

    companion object {
        private var mListener: Listener? = null

        private val mGestureListener by lazy { object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                mListener?.onSingleTapUp(e)
                return false
            }
        }}

        private val mScaleListener by lazy { object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private var mScaleFactor = 1.0f

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                // factor > 1, zoom
                // factor < 1, pinch
                mScaleFactor *= detector.getScaleFactor();
                // Don't let the object get too small or too large.
                mScaleFactor = 0.01f.coerceAtLeast(mScaleFactor.coerceAtMost(1.0f))
                return mListener != null && mListener?.onZoomValueChanged(mScaleFactor).orFalse
            }
        }}
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        if (!mGestureDetector?.onTouchEvent(event).orFalse) {
            return mScaleDetector?.onTouchEvent(event).orFalse
        }
        return false
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    interface Listener {
        fun onSingleTapUp(e: MotionEvent): Boolean

        fun onZoomValueChanged(factor: Float?): Boolean
    }

}