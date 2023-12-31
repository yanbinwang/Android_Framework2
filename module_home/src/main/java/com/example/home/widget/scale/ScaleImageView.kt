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
import android.widget.ImageView
import android.widget.OverScroller
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeDouble
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.value.toSafeInt
import kotlin.math.abs

/**
 * 能伸缩放大的图片
 * @author wyb
 */
@SuppressLint("AppCompatCustomView", "ClickableViewAccessibility")
class ScaleImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ImageView(context, attrs, defStyleAttr) {
    private var onDrawReady = false
    private var imageRenderedAtLeastOnce = false
    private var prevViewWidth = 0
    private var prevViewHeight = 0
    private var superMinScale = 0f
    private var superMaxScale = 0f
    private var matchViewWidth = 0f
    private var matchViewHeight = 0f
    private var prevMatchViewWidth = 0f
    private var prevMatchViewHeight = 0f
    private var m = FloatArray(9)
    private var prevMatrix = Matrix()
    private var mScaleType = ScaleType.FIT_CENTER
    private var state: State? = null
    private var delayedZoomVariables: ZoomVariables? = null
    private val SUPER_MIN_MULTIPLIER = 0.75f
    private val SUPER_MAX_MULTIPLIER = 1.25f

    companion object {
        var matrixs = Matrix()
        var viewWidth = 0
        var viewHeight = 0
        var normalizedScale = 0f
        var minScale = 0f
        var maxScale = 0f
        var fling: Fling? = null
        var mScaleDetector: ScaleGestureDetector? = null
        var mGestureDetector: GestureDetector? = null
        var userTouchListener: OnTouchListener? = null
        var touchImageViewListener: OnTouchImageViewListener? = null
        var doubleTapListener: GestureDetector.OnDoubleTapListener? = null

        fun compatPostOnAnimation(view: ScaleImageView, runnable: Runnable?) {
            view.postOnAnimation(runnable)
        }

        fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
            return if (contentSize <= viewSize) 0f else delta
        }
    }

    init {
        super.setClickable(true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener(this))
        mGestureDetector = GestureDetector(context, GestureListener(this))
        normalizedScale = 1f
        minScale = 1f
        maxScale = 3f
        superMinScale = SUPER_MIN_MULTIPLIER * minScale
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale
        imageMatrix = matrixs
        scaleType = ScaleType.MATRIX
        state = State.NONE
        onDrawReady = false
        super.setOnTouchListener(PrivateOnTouchListener(this))
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

    override fun setScaleType(type: ScaleType) {
        if (type == ScaleType.FIT_START || type == ScaleType.FIT_END) {
            throw UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END")
        }
        if (type == ScaleType.MATRIX) {
            super.setScaleType(ScaleType.MATRIX)
        } else {
            mScaleType = type
            if (onDrawReady) setZoom(this)
        }
    }

    override fun getScaleType(): ScaleType {
        return mScaleType
    }

    fun isZoomed(): Boolean {
        return normalizedScale != 1f
    }

    fun getZoomedRect(): RectF {
        if (mScaleType == ScaleType.FIT_XY) throw UnsupportedOperationException("getZoomedRect() not supported with FIT_XY")
        val topLeft = transformCoordTouchToBitmap(0f, 0f, true)
        val bottomRight = transformCoordTouchToBitmap(viewWidth.toSafeFloat(), viewHeight.toSafeFloat(), true)
        val w = drawable.intrinsicWidth.toSafeFloat()
        val h = drawable.intrinsicHeight.toSafeFloat()
        return RectF(topLeft.x / w, topLeft.y / h, bottomRight.x / w, bottomRight.y / h)
    }

    private fun savePreviousImageValues() {
        if (viewHeight != 0 && viewWidth != 0) {
            matrixs.getValues(m)
            prevMatrix.setValues(m)
            prevMatchViewHeight = matchViewHeight
            prevMatchViewWidth = matchViewWidth
            prevViewHeight = viewHeight
            prevViewWidth = viewWidth
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        bundle.putFloat("saveScale", normalizedScale)
        bundle.putFloat("matchViewHeight", matchViewHeight)
        bundle.putFloat("matchViewWidth", matchViewWidth)
        bundle.putInt("viewWidth", viewWidth)
        bundle.putInt("viewHeight", viewHeight)
        matrixs.getValues(m)
        bundle.putFloatArray("matrix", m)
        bundle.putBoolean("imageRendered", imageRenderedAtLeastOnce)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val bundle = state
            normalizedScale = bundle.getFloat("saveScale")
            m = bundle.getFloatArray("matrix") ?: FloatArray(9)
            prevMatrix.setValues(m)
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
            setZoom(delayedZoomVariables?.scale.orZero, delayedZoomVariables?.focusX.orZero, delayedZoomVariables?.focusY.orZero, delayedZoomVariables?.scaleType ?: ScaleType.FIT_CENTER)
            delayedZoomVariables = null
        }
        super.onDraw(canvas)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        savePreviousImageValues()
    }

    fun getMaxZoom(): Float {
        return maxScale
    }

    fun setMaxZoom(max: Float) {
        maxScale = max
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale
    }

    fun getMinZoom(): Float {
        return minScale
    }

    fun getCurrentZoom(): Float {
        return normalizedScale
    }

    fun setMinZoom(min: Float) {
        minScale = min
        superMinScale = SUPER_MIN_MULTIPLIER * minScale
    }

    fun resetZoom() {
        normalizedScale = 1f
        fitImageToView()
    }

    fun setZoom(scale: Float, focusX: Float = 0.5f, focusY: Float = 0.5f, scaleType: ScaleType = mScaleType) {
        if (!onDrawReady) {
            delayedZoomVariables = ZoomVariables(scale, focusX, focusY, scaleType)
            return
        }
        if (scaleType != mScaleType) setScaleType(scaleType)
        resetZoom()
        scaleImage(scale.toSafeDouble(), viewWidth / 2f, viewHeight / 2f, true)
        matrixs.getValues(m)
        m[Matrix.MTRANS_X] = -(focusX * getImageWidth() - viewWidth * 0.5f)
        m[Matrix.MTRANS_Y] = -(focusY * getImageHeight() - viewHeight * 0.5f)
        matrixs.setValues(m)
        fixTrans()
        imageMatrix = matrixs
    }

    fun setZoom(img: ScaleImageView) {
        val center = img.getScrollPosition()
        setZoom(img.getCurrentZoom(), center?.x.orZero, center?.y.orZero, img.scaleType)
    }

    fun getScrollPosition(): PointF? {
        val mDrawable = drawable ?: return null
        val drawableWidth = mDrawable.intrinsicWidth
        val drawableHeight = mDrawable.intrinsicHeight
        val point = transformCoordTouchToBitmap(viewWidth / 2f, viewHeight / 2f, true)
        point.x /= drawableWidth.toSafeFloat()
        point.y /= drawableHeight.toSafeFloat()
        return point
    }

    fun setScrollPosition(focusX: Float, focusY: Float) {
        setZoom(normalizedScale, focusX, focusY)
    }

    fun fixTrans() {
        matrixs.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]
        val fixTransX = getFixTrans(transX, viewWidth.toSafeFloat(), getImageWidth())
        val fixTransY = getFixTrans(transY, viewHeight.toSafeFloat(), getImageHeight())
        if (fixTransX != 0f || fixTransY != 0f) matrixs.postTranslate(fixTransX, fixTransY)
    }

    private fun fixScaleTrans() {
        fixTrans()
        matrixs.getValues(m)
        if (getImageWidth() < viewWidth) m[Matrix.MTRANS_X] = (viewWidth - getImageWidth()) / 2
        if (getImageHeight() < viewHeight) m[Matrix.MTRANS_Y] = (viewHeight - getImageHeight()) / 2
        matrixs.setValues(m)
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }
        if (trans < minTrans) return -trans + minTrans
        return if (trans > maxTrans) -trans + maxTrans else 0f
    }

    private fun getImageWidth() = matchViewWidth * normalizedScale

    private fun getImageHeight() = matchViewHeight * normalizedScale

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mDrawable = drawable
        if (mDrawable == null || mDrawable.intrinsicWidth == 0 || mDrawable.intrinsicHeight == 0) {
            setMeasuredDimension(0, 0)
            return
        }
        val drawableWidth = mDrawable.intrinsicWidth
        val drawableHeight = mDrawable.intrinsicHeight
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        viewWidth = setViewSize(widthMode, widthSize, drawableWidth)
        viewHeight = setViewSize(heightMode, heightSize, drawableHeight)
        setMeasuredDimension(viewWidth, viewHeight)
        fitImageToView()
    }

    private fun fitImageToView() {
        val mDrawable = drawable
        if (mDrawable == null || mDrawable.intrinsicWidth == 0 || mDrawable.intrinsicHeight == 0) return
        val drawableWidth = mDrawable.intrinsicWidth
        val drawableHeight = mDrawable.intrinsicHeight
        var scaleX = viewWidth.toSafeFloat() / drawableWidth
        var scaleY: Float = viewHeight.toSafeFloat() / drawableHeight
        when (mScaleType) {
            ScaleType.CENTER -> {
                scaleY = 1f
                scaleX = scaleY
            }
            ScaleType.CENTER_CROP -> {
                scaleY = scaleX.coerceAtLeast(scaleY)
                scaleX = scaleY
            }
            ScaleType.CENTER_INSIDE -> {
                run {
                    scaleY = 1f.coerceAtMost(scaleX.coerceAtMost(scaleY))
                    scaleX = scaleY
                }
                run {
                    scaleY = scaleX.coerceAtMost(scaleY)
                    scaleX = scaleY
                }
            }
            ScaleType.FIT_CENTER -> {
                scaleY = scaleX.coerceAtMost(scaleY)
                scaleX = scaleY
            }
            ScaleType.FIT_XY -> {}
            else -> throw UnsupportedOperationException("TouchImageView does not support FIT_START or FIT_END")
        }
        val redundantXSpace = viewWidth - scaleX * drawableWidth
        val redundantYSpace: Float = viewHeight - scaleY * drawableHeight
        matchViewWidth = viewWidth - redundantXSpace
        matchViewHeight = viewHeight - redundantYSpace
        if (!isZoomed() && !imageRenderedAtLeastOnce) {
            matrixs.setScale(scaleX, scaleY)
            matrixs.postTranslate(redundantXSpace / 2, redundantYSpace / 2)
            normalizedScale = 1f
        } else {
            if (prevMatchViewWidth == 0f || prevMatchViewHeight == 0f) savePreviousImageValues()
            prevMatrix.getValues(m)
            m[Matrix.MSCALE_X] = matchViewWidth / drawableWidth * normalizedScale
            m[Matrix.MSCALE_Y] = matchViewHeight / drawableHeight * normalizedScale
            val transX = m[Matrix.MTRANS_X]
            val transY = m[Matrix.MTRANS_Y]
            val prevActualWidth: Float = prevMatchViewWidth * normalizedScale
            val actualWidth = getImageWidth()
            translateMatrixAfterRotate(Matrix.MTRANS_X, transX, prevActualWidth, actualWidth, prevViewWidth, viewWidth, drawableWidth)
            val prevActualHeight: Float = prevMatchViewHeight * normalizedScale
            val actualHeight = getImageHeight()
            translateMatrixAfterRotate(Matrix.MTRANS_Y, transY, prevActualHeight, actualHeight, prevViewHeight, viewHeight, drawableHeight)
            matrixs.setValues(m)
        }
        fixTrans()
        imageMatrix = matrixs
    }

    private fun setViewSize(mode: Int, size: Int, drawableWidth: Int): Int {
        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> drawableWidth.coerceAtMost(size)
            MeasureSpec.UNSPECIFIED -> drawableWidth
            else -> size
        }
    }

    private fun translateMatrixAfterRotate(axis: Int, trans: Float, prevImageSize: Float, imageSize: Float, prevViewSize: Int, viewSize: Int, drawableSize: Int) {
        if (imageSize < viewSize) {
            m[axis] = (viewSize - drawableSize * m[Matrix.MSCALE_X]) * 0.5f
        } else if (trans > 0) {
            m[axis] = -((imageSize - viewSize) * 0.5f)
        } else {
            val percentage = (abs(trans) + 0.5f * prevViewSize) / prevImageSize
            m[axis] = -(percentage * imageSize - viewSize * 0.5f)
        }
    }

    fun canScrollHorizontallyFroyo(direction: Int): Boolean {
        return canScrollHorizontally(direction)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        matrixs.getValues(m)
        val x = m[Matrix.MTRANS_X]
        return if (getImageWidth() < viewWidth) {
            false
        } else if (x >= -1 && direction < 0) {
            false
        } else {
            abs(x) + viewWidth + 1 < getImageWidth() || direction <= 0
        }
    }

    private class GestureListener(var view: ScaleImageView) : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return if (doubleTapListener != null) {
                doubleTapListener?.onSingleTapConfirmed(e).orFalse
            } else {
                view.performClick()
            }
        }

        override fun onLongPress(e: MotionEvent) {
            view.performLongClick()
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (fling != null) fling?.cancelFling()
            fling = Fling(view, velocityX.toSafeInt(), velocityY.toSafeInt())
            compatPostOnAnimation(view, fling)
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            var consumed = false
            if (doubleTapListener != null) {
                consumed = doubleTapListener?.onDoubleTap(e).orFalse
            }
            if (view.state == State.NONE) {
                val targetZoom = if (normalizedScale == minScale) maxScale else minScale
                val doubleTap = DoubleTapZoom(view, targetZoom, e.x, e.y, false)
                compatPostOnAnimation(view, doubleTap)
                consumed = true
            }
            return consumed
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            return if (doubleTapListener != null) doubleTapListener?.onDoubleTapEvent(e).orFalse else false
        }
    }

    interface OnTouchImageViewListener {
        fun onMove()
    }

    private class PrivateOnTouchListener(var view: ScaleImageView) : OnTouchListener {
        private val last = PointF()

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            mScaleDetector?.onTouchEvent(event)
            mGestureDetector?.onTouchEvent(event)
            val curr = PointF(event.x, event.y)
            if (view.state == State.NONE || view.state == State.DRAG || view.state == State.FLING) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        last.set(curr)
                        if (fling != null) fling?.cancelFling()
                        view.state = State.DRAG
                    }
                    MotionEvent.ACTION_MOVE -> if (view.state == State.DRAG) {
                        val deltaX = curr.x - last.x
                        val deltaY = curr.y - last.y
                        val fixTransX = getFixDragTrans(deltaX, viewWidth.toSafeFloat(), view.getImageWidth())
                        val fixTransY = getFixDragTrans(deltaY, viewHeight.toSafeFloat(), view.getImageHeight())
                        matrixs.postTranslate(fixTransX, fixTransY)
                        view.fixTrans()
                        last[curr.x] = curr.y
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> view.state = State.NONE
                }
            }
            view.imageMatrix = matrixs
            if (userTouchListener != null) userTouchListener?.onTouch(v, event)
            if (touchImageViewListener != null) touchImageViewListener?.onMove()
            return true
        }
    }

    private class ScaleListener(var view: ScaleImageView) : SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            view.state = State.ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            view.scaleImage(detector.scaleFactor.toSafeDouble(), detector.focusX, detector.focusY, true)
            if (touchImageViewListener != null) {
                touchImageViewListener?.onMove()
            }
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            view.state = State.NONE
            var animateToZoomBoundary = false
            var targetZoom: Float = normalizedScale
            if (normalizedScale > maxScale) {
                targetZoom = maxScale
                animateToZoomBoundary = true
            } else if (normalizedScale < minScale) {
                targetZoom = minScale
                animateToZoomBoundary = true
            }
            if (animateToZoomBoundary) {
                val doubleTap = DoubleTapZoom(view, targetZoom, viewWidth / 2f, viewHeight / 2f, true)
                compatPostOnAnimation(view, doubleTap)
            }
        }
    }

    fun scaleImage(scale: Double, focusX: Float, focusY: Float, stretchImageToSuper: Boolean) {
        var deltaScale = scale
        val lowerScale: Float
        val upperScale: Float
        if (stretchImageToSuper) {
            lowerScale = superMinScale
            upperScale = superMaxScale
        } else {
            lowerScale = minScale
            upperScale = maxScale
        }
        val origScale = normalizedScale
        normalizedScale *= deltaScale.toSafeFloat()
        if (normalizedScale > upperScale) {
            normalizedScale = upperScale
            deltaScale = (upperScale / origScale).toSafeDouble()
        } else if (normalizedScale < lowerScale) {
            normalizedScale = lowerScale
            deltaScale = (lowerScale / origScale).toSafeDouble()
        }
        matrixs.postScale(deltaScale.toSafeFloat(), deltaScale.toSafeFloat(), focusX, focusY)
        fixScaleTrans()
    }

    private class DoubleTapZoom(var view: ScaleImageView, targetZoom: Float, focusX: Float, focusY: Float, stretchImageToSuper: Boolean) : Runnable {
        private val startZoom: Float
        private val targetZoom: Float
        private val bitmapX: Float
        private val bitmapY: Float
        private val startTime: Long
        private val stretchImageToSuper: Boolean
        private val startTouch: PointF
        private val endTouch: PointF
        private val interpolator = AccelerateDecelerateInterpolator()

        companion object {
            private const val ZOOM_TIME = 500f
        }

        init {
            view.state = State.ANIMATE_ZOOM
            startTime = System.currentTimeMillis()
            startZoom = normalizedScale
            this.targetZoom = targetZoom
            this.stretchImageToSuper = stretchImageToSuper
            val bitmapPoint: PointF = view.transformCoordTouchToBitmap(focusX, focusY, false)
            bitmapX = bitmapPoint.x
            bitmapY = bitmapPoint.y
            startTouch = view.transformCoordBitmapToTouch(bitmapX, bitmapY)
            endTouch = PointF(viewWidth / 2f, viewHeight / 2f)
        }

        override fun run() {
            val t = interpolate()
            val deltaScale = calculateDeltaScale(t)
            view.scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper)
            translateImageToCenterTouchPosition(t)
            view.fixScaleTrans()
            view.imageMatrix = matrixs
            if (touchImageViewListener != null) {
                touchImageViewListener?.onMove()
            }
            if (t < 1f) {
                compatPostOnAnimation(view, this)
            } else {
                view.state = State.NONE
            }
        }

        private fun translateImageToCenterTouchPosition(t: Float) {
            val targetX = startTouch.x + t * (endTouch.x - startTouch.x)
            val targetY = startTouch.y + t * (endTouch.y - startTouch.y)
            val curr = view.transformCoordBitmapToTouch(bitmapX, bitmapY)
            matrixs.postTranslate(targetX - curr.x, targetY - curr.y)
        }

        private fun interpolate(): Float {
            val currTime = System.currentTimeMillis()
            var elapsed = (currTime - startTime) / ZOOM_TIME
            elapsed = 1f.coerceAtMost(elapsed)
            return interpolator.getInterpolation(elapsed)
        }

        private fun calculateDeltaScale(t: Float): Double {
            val zoom = (startZoom + t * (targetZoom - startZoom)).toSafeDouble()
            return zoom / normalizedScale
        }
    }

    private fun transformCoordTouchToBitmap(x: Float, y: Float, clipToBitmap: Boolean): PointF {
        matrixs.getValues(m)
        val origW = drawable.intrinsicWidth.toSafeFloat()
        val origH = drawable.intrinsicHeight.toSafeFloat()
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]
        var finalX = (x - transX) * origW / getImageWidth()
        var finalY = (y - transY) * origH / getImageHeight()
        if (clipToBitmap) {
            finalX = finalX.coerceAtLeast(0f).coerceAtMost(origW)
            finalY = finalY.coerceAtLeast(0f).coerceAtMost(origH)
        }
        return PointF(finalX, finalY)
    }

    private fun transformCoordBitmapToTouch(bx: Float, by: Float): PointF {
        matrixs.getValues(m)
        val origW = drawable.intrinsicWidth.toSafeFloat()
        val origH = drawable.intrinsicHeight.toSafeFloat()
        val px = bx / origW
        val py = by / origH
        val finalX = m[Matrix.MTRANS_X] + getImageWidth() * px
        val finalY = m[Matrix.MTRANS_Y] + getImageHeight() * py
        return PointF(finalX, finalY)
    }

    class Fling(var view: ScaleImageView, velocityX: Int, velocityY: Int) : Runnable {
        private var scroller: CompatScroller?
        private var currX = 0
        private var currY = 0

        init {
            view.state = State.FLING
            scroller = CompatScroller(view.context)
            matrixs.getValues(view.m)
            val startX = view.m[Matrix.MTRANS_X].toSafeInt()
            val startY = view.m[Matrix.MTRANS_Y].toSafeInt()
            val minX: Int
            val maxX: Int
            val minY: Int
            val maxY: Int
            if (view.getImageWidth() > viewWidth) {
                minX = viewWidth - view.getImageWidth().toSafeInt()
                maxX = 0
            } else {
                maxX = startX.toSafeInt()
                minX = maxX
            }
            if (view.getImageHeight() > viewHeight) {
                minY = viewHeight - view.getImageHeight().toSafeInt()
                maxY = 0
            } else {
                maxY = startY.toSafeInt()
                minY = maxY
            }
            scroller?.fling(startX.toSafeInt(), startY.toSafeInt(), velocityX, velocityY, minX, maxX, minY, maxY)
            currX = startX.toSafeInt()
            currY = startY.toSafeInt()
        }

        fun cancelFling() {
            if (scroller != null) {
                view.state = State.NONE
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
                val transX = newX - currX
                val transY = newY - currY
                currX = newX
                currY = newY
                matrixs.postTranslate(transX.toSafeFloat(), transY.toSafeFloat())
                view.fixTrans()
                view.imageMatrix = matrixs
                compatPostOnAnimation(view, this)
            }
        }
    }

    private class CompatScroller(context: Context) {
        private val overScroller = OverScroller(context)

        fun fling(startX: Int, startY: Int, velocityX: Int, velocityY: Int, minX: Int, maxX: Int, minY: Int, maxY: Int) {
            overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
        }

        fun forceFinished(finished: Boolean) {
            overScroller.forceFinished(finished)
        }

        fun isFinished(): Boolean {
            return overScroller.isFinished
        }

        fun computeScrollOffset(): Boolean {
            overScroller.computeScrollOffset()
            return overScroller.computeScrollOffset()
        }

        fun getCurrX(): Int {
            return overScroller.currX
        }

        fun getCurrY(): Int {
            return overScroller.currY
        }
    }

    private data class ZoomVariables(var scale: Float?, var focusX: Float?, var focusY: Float?, var scaleType: ScaleType?)

    private enum class State {
        NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM
    }

}