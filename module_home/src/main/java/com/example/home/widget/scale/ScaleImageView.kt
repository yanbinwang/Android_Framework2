package com.example.home.widget.scale

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.OverScroller
import android.widget.Scroller
import androidx.appcompat.widget.AppCompatImageView
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeDouble
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 能伸缩放大的图片
 * 支持 CENTER/CENTER_CROP/CENTER_INSIDE/FIT_CENTER/FIT_XY（明确禁用 FIT_START/FIT_END）
 * 双击缩放（切换最小 / 最大缩放值）、双指缩放、单指拖动、惯性滑动（Fling）
 */
@SuppressLint("ObsoleteSdkInt", "ClickableViewAccessibility")
class ScaleImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var viewWidth: Int? = null
    private var viewHeight: Int? = null
    private var prevViewWidth: Int? = null
    private var prevViewHeight: Int? = null
    private var normalizedScale: Float? = null
    private var minScale: Float? = null
    private var maxScale: Float? = null
    private var superMinScale: Float? = null
    private var superMaxScale: Float? = null
    private var matchViewWidth: Float? = null
    private var matchViewHeight: Float? = null
    private var prevMatchViewWidth: Float? = null
    private var prevMatchViewHeight: Float? = null
    private var onDrawReady: Boolean? = null
    private var imageRenderedAtLeastOnce: Boolean? = null
    private var m: FloatArray? = null
    private var context: Context? = null
    private var matrix: Matrix? = null
    private var prevMatrix: Matrix? = null
    private var mScaleType: ScaleType? = null
    private var state: State? = null
    private var fling: Fling? = null
    private var delayedZoomVariables: ZoomVariables? = null
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null
    private var doubleTapListener: GestureDetector.OnDoubleTapListener? = null
    private var userTouchListener: OnTouchListener? = null
    private var touchImageViewListener: OnTouchImageViewListener? = null

    companion object {
        private const val ZOOM_TIME = 500f
        private const val SUPER_MIN_MULTIPLIER = 0.75f
        private const val SUPER_MAX_MULTIPLIER = 1.25f
    }

    init {
        super.setClickable(true)
        this.context = context
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mGestureDetector = GestureDetector(context, GestureListener())
        matrix = Matrix()
        prevMatrix = Matrix()
        m = FloatArray(9)
        normalizedScale = 1f
        if (mScaleType == null) {
            mScaleType = ScaleType.FIT_CENTER
        }
        minScale = 1f
        maxScale = 3f
        superMinScale = SUPER_MIN_MULTIPLIER * minScale.orZero
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale.orZero
        setImageMatrix(matrix)
        setScaleType(ScaleType.MATRIX)
        setState(State.NONE)
        onDrawReady = false
        super.setOnTouchListener(PrivateOnTouchListener())
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        userTouchListener = l
    }

    fun setOnTouchImageViewListener(l: OnTouchImageViewListener?) {
        touchImageViewListener = l
    }

    fun setOnDoubleTapListener(l: GestureDetector.OnDoubleTapListener?) {
        doubleTapListener = l
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setScaleType(scaleType: ScaleType?) {
        if (scaleType == ScaleType.FIT_START || scaleType == ScaleType.FIT_END) {
            throw UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END")
        }
        if (scaleType == ScaleType.MATRIX) {
            super.setScaleType(ScaleType.MATRIX)
        } else {
            mScaleType = scaleType
            if (onDrawReady.orFalse) {
                setZoom(this)
            }
        }
    }

    override fun getScaleType(): ScaleType? {
        return mScaleType
    }

    fun isZoomed(): Boolean {
        return normalizedScale != 1f
    }

    fun getZoomedRect(): RectF {
        if (mScaleType == ScaleType.FIT_XY) {
            throw UnsupportedOperationException("getZoomedRect() not supported with FIT_XY")
        }
        val topLeft = transformCoordTouchToBitmap(0f, 0f, true)
        val bottomRight = transformCoordTouchToBitmap(viewWidth?.toSafeFloat(), viewHeight?.toSafeFloat(), true)
        val w = getDrawable().intrinsicWidth.toSafeFloat()
        val h = getDrawable().intrinsicHeight.toSafeFloat()
        return RectF(topLeft.x / w, topLeft.y / h, bottomRight.x / w, bottomRight.y / h)
    }

    private fun savePreviousImageValues() {
        if (matrix != null && viewHeight != 0 && viewWidth != 0) {
            matrix?.getValues(m)
            prevMatrix?.setValues(m)
            prevMatchViewHeight = matchViewHeight
            prevMatchViewWidth = matchViewWidth
            prevViewHeight = viewHeight
            prevViewWidth = viewWidth
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        bundle.putFloat("saveScale", normalizedScale.orZero)
        bundle.putFloat("matchViewHeight", matchViewHeight.orZero)
        bundle.putFloat("matchViewWidth", matchViewWidth.orZero)
        bundle.putInt("viewWidth", viewWidth.orZero)
        bundle.putInt("viewHeight", viewHeight.orZero)
        matrix?.getValues(m)
        bundle.putFloatArray("matrix", m)
        bundle.putBoolean("imageRendered", imageRenderedAtLeastOnce.orFalse)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val bundle = state
            normalizedScale = bundle.getFloat("saveScale")
            m = bundle.getFloatArray("matrix")
            prevMatrix?.setValues(m)
            prevMatchViewHeight = bundle.getFloat("matchViewHeight")
            prevMatchViewWidth = bundle.getFloat("matchViewWidth")
            prevViewHeight = bundle.getInt("viewHeight")
            prevViewWidth = bundle.getInt("viewWidth")
            imageRenderedAtLeastOnce = bundle.getBoolean("imageRendered")
            super.onRestoreInstanceState(bundle.getParcelable("instanceState"))
            return
        }
        super.onRestoreInstanceState(state)
    }

    override fun onDraw(canvas: Canvas) {
        onDrawReady = true
        imageRenderedAtLeastOnce = true
        if (delayedZoomVariables != null) {
            setZoom(delayedZoomVariables?.scale, delayedZoomVariables?.focusX, delayedZoomVariables?.focusY, delayedZoomVariables?.scaleType)
            delayedZoomVariables = null
        }
        super.onDraw(canvas)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        savePreviousImageValues()
    }

    fun getMaxZoom(): Float? {
        return maxScale
    }

    fun setMaxZoom(max: Float) {
        maxScale = max
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale.orZero
    }

    fun getMinZoom(): Float? {
        return minScale
    }

    fun getCurrentZoom(): Float? {
        return normalizedScale
    }

    fun setMinZoom(min: Float) {
        minScale = min
        superMinScale = SUPER_MIN_MULTIPLIER * minScale.orZero
    }

    fun resetZoom() {
        normalizedScale = 1f
        fitImageToView()
    }

    fun setZoom(scale: Float?) {
        setZoom(scale, 0.5f, 0.5f)
    }

    fun setZoom(scale: Float?, focusX: Float?, focusY: Float?) {
        setZoom(scale, focusX, focusY, mScaleType)
    }

    fun setZoom(scale: Float?, focusX: Float?, focusY: Float?, scaleType: ScaleType?) {
        if (!onDrawReady.orFalse) {
            delayedZoomVariables = ZoomVariables(scale, focusX, focusY, scaleType)
            return
        }
        if (scaleType != mScaleType) {
            setScaleType(scaleType)
        }
        resetZoom()
        scaleImage(scale?.toSafeDouble(), (viewWidth.orZero / 2).toSafeFloat(), (viewHeight.orZero / 2).toSafeFloat(), true)
        matrix?.getValues(m)
        m?.set(Matrix.MTRANS_X, -((focusX.orZero * getImageWidth().orZero) - (viewWidth.orZero * 0.5f)))
        m?.set(Matrix.MTRANS_Y, -((focusY.orZero * getImageHeight().orZero) - (viewHeight.orZero * 0.5f)))
        matrix?.setValues(m)
        fixTrans()
        setImageMatrix(matrix)
    }

    fun setZoom(img: ScaleImageView) {
        val center = img.getScrollPosition()
        setZoom(img.getCurrentZoom(), center?.x, center?.y, img.scaleType)
    }

    fun getScrollPosition(): PointF? {
        val drawable = getDrawable()
        if (drawable == null) {
            return null
        }
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        val point = transformCoordTouchToBitmap((viewWidth.orZero / 2).toSafeFloat(), (viewHeight.orZero / 2).toSafeFloat(), true)
        point.x /= drawableWidth.toSafeFloat()
        point.y /= drawableHeight.toSafeFloat()
        return point
    }

    fun setScrollPosition(focusX: Float, focusY: Float) {
        setZoom(normalizedScale, focusX, focusY)
    }

    private fun fixTrans() {
        matrix?.getValues(m)
        val transX = m?.get(Matrix.MTRANS_X)
        val transY = m?.get(Matrix.MTRANS_Y)
        val fixTransX = getFixTrans(transX, viewWidth?.toSafeFloat(), getImageWidth())
        val fixTransY = getFixTrans(transY, viewHeight?.toSafeFloat(), getImageHeight())
        if (fixTransX != 0f || fixTransY != 0f) {
            matrix?.postTranslate(fixTransX, fixTransY)
        }
    }

    private fun fixScaleTrans() {
        fixTrans()
        matrix?.getValues(m)
        if (getImageWidth().orZero < viewWidth.orZero) {
            m?.set(Matrix.MTRANS_X, (viewWidth.orZero - getImageWidth().orZero) / 2)
        }
        if (getImageHeight().orZero < viewHeight.orZero) {
            m?.set(Matrix.MTRANS_Y, (viewHeight.orZero - getImageHeight().orZero) / 2)
        }
        matrix?.setValues(m)
    }

    private fun getFixTrans(trans: Float?, viewSize: Float?, contentSize: Float?): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize.orZero <= viewSize.orZero) {
            minTrans = 0f
            maxTrans = viewSize.orZero - contentSize.orZero
        } else {
            minTrans = viewSize.orZero - contentSize.orZero
            maxTrans = 0f
        }
        if (trans.orZero < minTrans.orZero) return -trans.orZero + minTrans
        if (trans.orZero > maxTrans.orZero) return -trans.orZero + maxTrans
        return 0f
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        if (contentSize <= viewSize) {
            return 0f
        }
        return delta
    }

    private fun getImageWidth(): Float? {
        return matchViewWidth.orZero * normalizedScale.orZero
    }

    private fun getImageHeight(): Float? {
        return matchViewHeight.orZero * normalizedScale.orZero
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val drawable = getDrawable()
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            setMeasuredDimension(0, 0)
            return
        }
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        viewWidth = setViewSize(widthMode, widthSize, drawableWidth)
        viewHeight = setViewSize(heightMode, heightSize, drawableHeight)
        setMeasuredDimension(viewWidth.orZero, viewHeight.orZero)
        fitImageToView()
    }

    private fun fitImageToView() {
        val drawable = getDrawable()
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            return
        }
        if (matrix == null || prevMatrix == null) {
            return
        }
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        var scaleX = viewWidth?.toSafeFloat().orZero / drawableWidth
        var scaleY = viewHeight?.toSafeFloat().orZero / drawableHeight
        when (mScaleType) {
            ScaleType.CENTER -> {
                scaleY = 1f
                scaleX = scaleY
            }
            ScaleType.CENTER_CROP -> {
                scaleY = max(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.CENTER_INSIDE -> {
                scaleX = min(1f, min(scaleX, scaleY)).also { scaleY = it }
            }
            ScaleType.FIT_CENTER -> {
                scaleY = min(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.FIT_XY -> {}
            else -> {
                throw UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END")
            }
        }
        val redundantXSpace = viewWidth.orZero - (scaleX * drawableWidth)
        val redundantYSpace = viewHeight.orZero - (scaleY * drawableHeight)
        matchViewWidth = viewWidth.orZero - redundantXSpace
        matchViewHeight = viewHeight.orZero - redundantYSpace
        if (!isZoomed() && !imageRenderedAtLeastOnce.orFalse) {
            matrix?.setScale(scaleX, scaleY)
            matrix?.postTranslate(redundantXSpace / 2, redundantYSpace / 2)
            normalizedScale = 1f
        } else {
            if (prevMatchViewWidth == 0f || prevMatchViewHeight == 0f) {
                savePreviousImageValues()
            }
            prevMatrix?.getValues(m)
            m?.set(Matrix.MSCALE_X, matchViewWidth.orZero / drawableWidth * normalizedScale.orZero)
            m?.set(Matrix.MSCALE_Y, matchViewHeight.orZero / drawableHeight * normalizedScale.orZero)
            val transX = m?.get(Matrix.MTRANS_X)
            val transY = m?.get(Matrix.MTRANS_Y)
            val prevActualWidth = prevMatchViewWidth.orZero * normalizedScale.orZero
            val actualWidth = getImageWidth()
            translateMatrixAfterRotate(Matrix.MTRANS_X, transX, prevActualWidth, actualWidth, prevViewWidth, viewWidth, drawableWidth)
            val prevActualHeight = prevMatchViewHeight.orZero * normalizedScale.orZero
            val actualHeight = getImageHeight()
            translateMatrixAfterRotate(Matrix.MTRANS_Y, transY, prevActualHeight, actualHeight, prevViewHeight, viewHeight, drawableHeight)
            matrix?.setValues(m)
        }
        fixTrans()
        setImageMatrix(matrix)
    }

    private fun setViewSize(mode: Int, size: Int, drawableWidth: Int): Int {
        val viewSize = when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> min(drawableWidth, size)
            MeasureSpec.UNSPECIFIED -> drawableWidth
            else -> size
        }
        return viewSize
    }

    private fun translateMatrixAfterRotate(axis: Int?, trans: Float?, prevImageSize: Float?, imageSize: Float?, prevViewSize: Int?, viewSize: Int?, drawableSize: Int?) {
        if (imageSize.orZero < viewSize.orZero) {
            m?.set(axis.orZero, (viewSize.orZero - (drawableSize.orZero * m?.get(Matrix.MSCALE_X).orZero)) * 0.5f)
        } else if (trans.orZero > 0) {
            m?.set(axis.orZero, -((imageSize.orZero - viewSize.orZero) * 0.5f))
        } else {
            val percentage = (abs(trans.orZero) + (0.5f * prevViewSize.orZero)) / prevImageSize.orZero
            m?.set(axis.orZero, -((percentage * imageSize.orZero) - (viewSize.orZero * 0.5f)))
        }
    }

    private fun setState(state: State?) {
        this.state = state
    }

    fun canScrollHorizontallyFroyo(direction: Int): Boolean {
        return canScrollHorizontally(direction)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        matrix?.getValues(m)
        val x = m?.get(Matrix.MTRANS_X).orZero
        return if (getImageWidth().orZero < viewWidth.orZero) {
            false
        } else if (x >= -1 && direction < 0) {
            false
        } else {
            !(abs(x) + viewWidth.orZero + 1 >= getImageWidth().orZero) || direction <= 0
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (doubleTapListener != null) {
                return doubleTapListener?.onSingleTapConfirmed(e).orFalse
            }
            return performClick()
        }

        override fun onLongPress(e: MotionEvent) {
            performLongClick()
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (fling != null) {
                fling?.cancelFling()
            }
            fling = Fling(velocityX.toInt(), velocityY.toInt())
            compatPostOnAnimation(fling)
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            var consumed = false
            if (doubleTapListener != null) {
                consumed = doubleTapListener?.onDoubleTap(e).orFalse
            }
            if (state == State.NONE) {
                val targetZoom = if (normalizedScale == minScale) maxScale else minScale
                val doubleTap = DoubleTapZoom(targetZoom, e.x, e.y, false)
                compatPostOnAnimation(doubleTap)
                consumed = true
            }
            return consumed
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            if (doubleTapListener != null) {
                return doubleTapListener?.onDoubleTapEvent(e).orFalse
            }
            return false
        }

    }

    private inner class PrivateOnTouchListener() : OnTouchListener {
        private val last = PointF()

        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            mScaleDetector?.onTouchEvent(event)
            mGestureDetector?.onTouchEvent(event)
            val curr = PointF(event.x, event.y)
            if (state == State.NONE || state == State.DRAG || state == State.FLING) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        last.set(curr)
                        if (fling != null) fling?.cancelFling()
                        setState(State.DRAG)
                    }
                    MotionEvent.ACTION_MOVE -> if (state == State.DRAG) {
                        val deltaX = curr.x - last.x
                        val deltaY = curr.y - last.y
                        val fixTransX = getFixDragTrans(deltaX, viewWidth?.toSafeFloat().orZero, getImageWidth().orZero)
                        val fixTransY = getFixDragTrans(deltaY, viewHeight?.toSafeFloat().orZero, getImageHeight().orZero)
                        matrix?.postTranslate(fixTransX, fixTransY)
                        fixTrans()
                        last.set(curr.x, curr.y)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                        setState(State.NONE)
                    }
                }
            }
            setImageMatrix(matrix)
            if (userTouchListener != null) {
                userTouchListener?.onTouch(v, event)
            }
            if (touchImageViewListener != null) {
                touchImageViewListener?.onMove()
            }
            return true
        }

    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            setState(State.ZOOM)
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleImage(detector.getScaleFactor().toDouble(), detector.focusX, detector.focusY, true)
            if (touchImageViewListener != null) {
                touchImageViewListener?.onMove()
            }
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            setState(State.NONE)
            var animateToZoomBoundary = false
            var targetZoom = normalizedScale
            if (normalizedScale.orZero > maxScale.orZero) {
                targetZoom = maxScale
                animateToZoomBoundary = true
            } else if (normalizedScale.orZero < minScale.orZero) {
                targetZoom = minScale
                animateToZoomBoundary = true
            }
            if (animateToZoomBoundary) {
                val doubleTap = DoubleTapZoom(targetZoom, (viewWidth.orZero / 2).toSafeFloat(), (viewHeight.orZero / 2).toSafeFloat(), true)
                compatPostOnAnimation(doubleTap)
            }
        }

    }

    private fun scaleImage(deltaScale: Double?, focusX: Float?, focusY: Float?, stretchImageToSuper: Boolean?) {
        var mDeltaScale = deltaScale
        val lowerScale: Float
        val upperScale: Float
        if (stretchImageToSuper.orFalse) {
            lowerScale = superMinScale.orZero
            upperScale = superMaxScale.orZero
        } else {
            lowerScale = minScale.orZero
            upperScale = maxScale.orZero
        }
        val origScale = normalizedScale
        normalizedScale = normalizedScale.orZero * mDeltaScale.toSafeFloat().orZero
        if (normalizedScale.orZero > upperScale) {
            normalizedScale = upperScale
            mDeltaScale = (upperScale / origScale.orZero).toSafeDouble()
        } else if (normalizedScale.orZero < lowerScale) {
            normalizedScale = lowerScale
            mDeltaScale = (lowerScale / origScale.orZero).toSafeDouble()
        }
        matrix?.postScale(mDeltaScale?.toSafeFloat().orZero, mDeltaScale?.toSafeFloat().orZero, focusX.orZero, focusY.orZero)
        fixScaleTrans()
    }

    private inner class DoubleTapZoom(private val targetZoom: Float?, focusX: Float, focusY: Float, private val stretchImageToSuper: Boolean) : Runnable {
        private var startTime: Long? = null
        private var startZoom: Float? = null
        private var bitmapX: Float? = null
        private var bitmapY: Float? = null
        private var startTouch: PointF? = null
        private var endTouch: PointF? = null
        private val interpolator = AccelerateDecelerateInterpolator()

        init {
            setState(State.ANIMATE_ZOOM)
            startTime = System.currentTimeMillis()
            startZoom = normalizedScale
            val bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, false)
            bitmapX = bitmapPoint.x
            bitmapY = bitmapPoint.y
            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY)
            endTouch = PointF((viewWidth.orZero / 2).toSafeFloat(), (viewHeight.orZero / 2).toSafeFloat())
        }

        override fun run() {
            val t = interpolate()
            val deltaScale = calculateDeltaScale(t)
            scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper)
            translateImageToCenterTouchPosition(t)
            fixScaleTrans()
            setImageMatrix(matrix)
            if (touchImageViewListener != null) {
                touchImageViewListener?.onMove()
            }
            if (t < 1f) {
                compatPostOnAnimation(this)
            } else {
                setState(State.NONE)
            }
        }

        private fun translateImageToCenterTouchPosition(t: Float) {
            val targetX = startTouch?.x.orZero + t * (endTouch?.x.orZero - startTouch?.x.orZero)
            val targetY = startTouch?.y.orZero + t * (endTouch?.y.orZero - startTouch?.y.orZero)
            val curr = transformCoordBitmapToTouch(bitmapX, bitmapY)
            matrix?.postTranslate(targetX - curr.x, targetY - curr.y)
        }

        private fun interpolate(): Float {
            val currTime = System.currentTimeMillis()
            var elapsed = (currTime - startTime.orZero) / ZOOM_TIME
            elapsed = min(1f, elapsed)
            return interpolator.getInterpolation(elapsed)
        }

        private fun calculateDeltaScale(t: Float): Double {
            val zoom = (startZoom.orZero + t * (targetZoom.orZero - startZoom.orZero)).toSafeDouble()
            return zoom / normalizedScale.orZero
        }
    }

    private fun transformCoordTouchToBitmap(x: Float?, y: Float?, clipToBitmap: Boolean?): PointF {
        matrix?.getValues(m)
        val origW = getDrawable().intrinsicWidth.toSafeFloat()
        val origH = getDrawable().intrinsicHeight.toSafeFloat()
        val transX = m?.get(Matrix.MTRANS_X).orZero
        val transY = m?.get(Matrix.MTRANS_Y).orZero
        var finalX = ((x.orZero - transX) * origW) / getImageWidth().orZero
        var finalY = ((y.orZero - transY) * origH) / getImageHeight().orZero
        if (clipToBitmap.orFalse) {
            finalX = min(max(finalX, 0f), origW)
            finalY = min(max(finalY, 0f), origH)
        }
        return PointF(finalX, finalY)
    }

    private fun transformCoordBitmapToTouch(bx: Float?, by: Float?): PointF {
        matrix?.getValues(m)
        val origW = getDrawable().intrinsicWidth.toSafeFloat()
        val origH = getDrawable().intrinsicHeight.toSafeFloat()
        val px = bx.orZero / origW
        val py = by.orZero / origH
        val finalX = m?.get(Matrix.MTRANS_X).orZero + getImageWidth().orZero * px
        val finalY = m?.get(Matrix.MTRANS_Y).orZero + getImageHeight().orZero * py
        return PointF(finalX, finalY)
    }

    private inner class Fling(velocityX: Int?, velocityY: Int?) : Runnable {
        var scroller: CompatScroller? = null
        var currX: Int? = null
        var currY: Int? = null

        init {
            setState(State.FLING)
            scroller = CompatScroller(context)
            matrix?.getValues(m)
            val startX = m?.get(Matrix.MTRANS_X).toSafeInt()
            val startY = m?.get(Matrix.MTRANS_Y).toSafeInt()
            val minX: Int
            val maxX: Int
            val minY: Int
            val maxY: Int
            if (getImageWidth().orZero > viewWidth.orZero) {
                minX = viewWidth.orZero - getImageWidth().toSafeInt()
                maxX = 0
            } else {
                maxX = startX
                minX = maxX
            }
            if (getImageHeight().orZero > viewHeight.orZero) {
                minY = viewHeight.orZero - getImageHeight().toSafeInt()
                maxY = 0
            } else {
                maxY = startY
                minY = maxY
            }
            scroller?.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
            currX = startX
            currY = startY
        }

        fun cancelFling() {
            if (scroller != null) {
                setState(State.NONE)
                scroller?.forceFinished(true)
            }
        }

        override fun run() {
            if (touchImageViewListener != null) {
                touchImageViewListener?.onMove()
            }
            if (scroller?.isFinished().orFalse) {
                scroller = null
                return
            }
            if (scroller?.computeScrollOffset().orFalse) {
                val newX = scroller?.getCurrX().orZero
                val newY = scroller?.getCurrY().orZero
                val transX = newX - currX.orZero
                val transY = newY - currY.orZero
                currX = newX
                currY = newY
                matrix?.postTranslate(transX.toSafeFloat(), transY.toSafeFloat())
                fixTrans()
                setImageMatrix(matrix)
                compatPostOnAnimation(this)
            }
        }

    }

    private fun compatPostOnAnimation(runnable: Runnable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postOnAnimation(runnable)
        } else {
            postDelayed(runnable, (1000 / 60).toLong())
        }
    }

    private fun printMatrixInfo() {
        val n = FloatArray(9)
        matrix?.getValues(n)
    }

    private class CompatScroller {
        var scroller: Scroller? = null
        var overScroller: OverScroller? = null
        var isPreGingerbread: Boolean = false

        constructor(context: Context?) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                isPreGingerbread = true
                scroller = Scroller(context)
            } else {
                isPreGingerbread = false
                overScroller = OverScroller(context)
            }
        }

        fun fling(startX: Int?, startY: Int?, velocityX: Int?, velocityY: Int?, minX: Int?, maxX: Int?, minY: Int?, maxY: Int?) {
            if (isPreGingerbread) {
                scroller?.fling(startX.orZero, startY.orZero, velocityX.orZero, velocityY.orZero, minX.orZero, maxX.orZero, minY.orZero, maxY.orZero)
            } else {
                overScroller?.fling(startX.orZero, startY.orZero, velocityX.orZero, velocityY.orZero, minX.orZero, maxX.orZero, minY.orZero, maxY.orZero)
            }
        }

        fun forceFinished(finished: Boolean) {
            if (isPreGingerbread) {
                scroller?.forceFinished(finished)
            } else {
                overScroller?.forceFinished(finished)
            }
        }

        fun isFinished(): Boolean {
            return if (isPreGingerbread) {
                scroller?.isFinished
            } else {
                overScroller?.isFinished
            }.orFalse
        }

        fun computeScrollOffset(): Boolean {
            if (isPreGingerbread) {
                return scroller?.computeScrollOffset().orFalse
            } else {
                overScroller?.computeScrollOffset()
                return overScroller?.computeScrollOffset().orFalse
            }
        }

        fun getCurrX(): Int {
            return if (isPreGingerbread) {
                scroller?.currX
            } else {
                overScroller?.currX
            }.orZero
        }

        fun getCurrY(): Int {
            return if (isPreGingerbread) {
                scroller?.currY
            } else {
                overScroller?.currY
            }.orZero
        }
    }

    private enum class State {
        NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM
    }

    private data class ZoomVariables(
        var scale: Float? = null,
        var focusX: Float? = null,
        var focusY: Float? = null,
        var scaleType: ScaleType? = null
    )

    interface OnTouchImageViewListener {
        fun onMove()
    }

}