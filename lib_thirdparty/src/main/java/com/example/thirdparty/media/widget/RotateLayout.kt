package com.example.thirdparty.media.widget

import android.content.Context
import android.util.AttributeSet
import android.view.OrientationEventListener
import android.widget.RelativeLayout
import com.example.framework.utils.function.value.toSafeFloat
import com.example.framework.utils.function.view.doOnceAfterLayout

/**
 * 记录对应的宽高，坐标轴
 * // 页面可见时启用监听
 * override fun onResume() {
 *    super.onResume()
 *    rotateLayout.enable()
 * }
 *
 * // 页面不可见时禁用监听（避免后台耗电）
 * override fun onPause() {
 *    super.onPause()
 *    rotateLayout.disable()
 * }
 */
class RotateLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {
    // 原始布局宽度（未旋转时）
    private var mOriginalWidth = 0
    // 原始布局高度（未旋转时）
    private var mOriginalHeight = 0
    // 旋转中心X（原始左下角X=0）
    private var mOriginalPivotX = 0f
    // 旋转中心Y（原始左下角Y=原始高度）
    private var mOriginalPivotY = 0f
    // 视图布局旋转角度（0/90/180/270）
    private var mRotate = 0
    // 设备物理方向（0/90/180/270）
    private var currentOrientation = 0
    // 布局是否完成初始化
    private var isLaidOut = false
    // 传感器角度判定误差范围（可通过 attrs 配置，增强灵活性）
    private val sensorAngleThreshold = 10
    // 角度监听器
    private val orientationListener = object : OrientationEventListener(getContext()) {
        override fun onOrientationChanged(orientation: Int) {
            // orientation的范围是0-359，-1表示未就绪
            if (orientation == ORIENTATION_UNKNOWN) {
                return
            }
            // 更新设备物理方向（准确角度记录）
            val newOrientation = getFixedDeviceOrientation(orientation)
            if (newOrientation != currentOrientation) {
                currentOrientation = newOrientation
            }
            // 计算视图需要的旋转角度（布局适配）
            val newLayoutRotate = getLayoutRotateAngle(orientation)
            // 仅当旋转角度变化时才更新
            if (newLayoutRotate != mRotate) {
                setRotateLayout(newLayoutRotate)
            }
        }
    }

    init {
        doOnceAfterLayout {
            isLaidOut = true
            mOriginalWidth = measuredWidth
            mOriginalHeight = measuredHeight
            mOriginalPivotX = pivotX
            mOriginalPivotY = pivotY
        }
    }

    /**
     * 设备物理方向判定（准确记录，按45°分界）
     * 0：正常竖屏 | 90：向左横屏 | 180：上下颠倒 | 270：向右横屏
     */
    private fun getFixedDeviceOrientation(orientation: Int): Int {
        return when (orientation) {
            !in 45..<315 -> 0
            in 45 until 135 -> 90
            in 135 until 225 -> 180
            else -> 270
        }
    }

    /**
     * 视图布局旋转角度判定（带误差容忍，适配布局）
     * 0：正常 | 90/180/270：对应旋转方向（与物理方向适配）
     */
    private fun getLayoutRotateAngle(orientation: Int): Int {
        return when {
            orientation > 360 - sensorAngleThreshold || orientation < sensorAngleThreshold -> 0
            orientation in (90 - sensorAngleThreshold)..(90 + sensorAngleThreshold) -> 270
            orientation in (180 - sensorAngleThreshold)..(180 + sensorAngleThreshold) -> 180
            orientation in (270 - sensorAngleThreshold)..(270 + sensorAngleThreshold) -> 90
            // 未达到判定阈值，保持当前旋转角度
            else -> mRotate
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 释放传感器资源
        isLaidOut = false
        orientationListener.disable()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (isLaidOut) {
            // 根据当前旋转角度，交换宽高测量值
            val isPortrait = mRotate == 0 || mRotate == 180
            val targetWidth = if (isPortrait) mOriginalWidth else mOriginalHeight
            val targetHeight = if (isPortrait) mOriginalHeight else mOriginalWidth
            setMeasuredDimension(targetWidth, targetHeight)
            val targetPivotX = if (mRotate == 90 || mRotate == 180) mOriginalPivotX else mOriginalPivotY
            val targetPivotY = if (mRotate == 180 || mRotate == 270) mOriginalPivotY else mOriginalPivotX
            pivotX = targetPivotX
            pivotY = targetPivotY
        }
    }

    /**
     * 旋转时候宽高替换
     *
     * @param rotate
     */
    fun setRotateLayout(rotate: Int) {
        mRotate = rotate
        rotation = rotate.toSafeFloat()
        val isPortrait = rotate == 0 || rotate == 180
        val newWidth = if (isPortrait) mOriginalWidth else mOriginalHeight
        val newHeight = if (isPortrait) mOriginalHeight else mOriginalWidth
        (layoutParams as? LayoutParams)?.apply {
            if (width != newWidth || height != newHeight) {
                width = newWidth
                height = newHeight
                setLayoutParams(this)
            }
        }
        requestLayout()
    }

    /**
     * 获取设备当前物理方向（对外暴露准确方向）
     */
    fun getCurrentDeviceOrientation(): Int {
        return currentOrientation
    }

    /**
     * 获取视图当前旋转角度（对外暴露布局旋转状态）
     */
    fun getLayoutRotate(): Int {
        return mRotate
    }

    /**
     * 启动方向监听（添加传感器可用性检查）
     */
    fun enableListener() {
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }
    }

    /**
     * 停止方向监听
     */
    fun disableListener() {
        orientationListener.disable()
    }

}
//class RotateLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {
//    private var width = 0
//    private var height = 0
//    private var rotate = 0
//    private var x = 0f
//    private var y = 0f
//    private var inflateLock = false
//    private val eventListener: OrientationEventListener = object : OrientationEventListener(getContext()) {
//        override fun onOrientationChanged(orientation: Int) {
//            if (orientation == ORIENTATION_UNKNOWN) return
//            //下面是手机旋转准确角度与四个方向角度（0 90 180 270）的转换
//            val SENSOR_ANGLE = 10
//            val rotate = if (orientation > 360 - SENSOR_ANGLE || orientation < SENSOR_ANGLE) {
//                0
//            } else if (orientation > 90 - SENSOR_ANGLE && orientation < 90 + SENSOR_ANGLE) {
//                270
//            } else if (orientation > 180 - SENSOR_ANGLE && orientation < 180 + SENSOR_ANGLE) {
//                180
//            } else if (orientation > 270 - SENSOR_ANGLE && orientation < 270 + SENSOR_ANGLE) {
//                90
//            } else {
//                return
//            }
//            setRotate(rotate)
//        }
//    }
//
//    /**
//     * 设置post后后续到的当前控件的横屏宽高
//     */
//    override fun onFinishInflate() {
//        super.onFinishInflate()
//        post {
//            this.inflateLock = true
//            this.width = measuredWidth
//            this.height = measuredHeight
//            this.x = pivotX
//            this.y = pivotY
//        }
//    }
//
//    /**
//     * 销毁时解除锁定
//     */
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        inflateLock = false
//    }
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        if (inflateLock) {
//            // 根据记录的原生比例调整坐标轴
//            val portrait = rotate == 0 || rotate == 180
//            setMeasuredDimension(if (portrait) width else height, if (portrait) height else width)
//            if (rotate == 90) {
//                pivotX = x
//                pivotY = x
//            }
//            if (rotate == 180) {
//                pivotX = x
//                pivotY = y
//            }
//            if (rotate == 270) {
//                pivotX = y
//                pivotY = y
//            }
//        }
//    }
//
//    /**
//     * 旋转时候宽高替换
//     *
//     * @param rotate
//     */
//    fun setRotate(rotate: Int) {
//        if (inflateLock) {
//            this.rotate = rotate
//            this.rotation = rotate.toSafeFloat()
//            val portrait = rotate == 0 || rotate == 180
//            val params = layoutParams as? LayoutParams
//            params?.width = if (portrait) width else height
//            params?.height = if (portrait) height else width
//            this.setLayoutParams(params)
//            this.requestLayout()
//        }
//    }
//
//    /**
//     * 获取当前方向角
//     */
//    fun getRotate(): Int {
//        return rotate
//    }
//
//    /**
//     * 页面注册监听
//     */
//    fun enable() {
//        eventListener.enable()
//    }
//
//    /**
//     * 页面销毁监听
//     */
//    fun disable() {
//        eventListener.disable()
//    }
//
//}