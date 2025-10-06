package com.example.thirdparty.media.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 手势处理帮助类
 * // 注册传感器监听
 * override fun onResume() {
 *    super.onResume()
 *    rotationHandler.registerSensorListener()
 * }
 * // 取消传感器监听
 * override fun onPause() {
 *    super.onPause()
 *    rotationHandler.unregisterSensorListener()
 * }
 * // 在拍摄照片时获取旋转角度
 * private fun takePhoto() {
 *    val rotation = rotationHandler.photoRotation
 *    // 使用这个角度处理照片...
 * }
 * <!-- 在AndroidManifest.xml中添加 -->
 * <uses-permission android:name="android.permission.INTERNET" />
 * <!-- 传感器基础权限（非危险权限，无需动态申请） -->
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-feature android:name="android.hardware.sensor.rotation_vector" android:required="false" />
 * android:required="false"表示应用可以在没有该传感器的设备上运行。
 * 从 Android 10 (API 29) 开始，对后台传感器使用有严格限制：
 * 当应用处于后台时，传感器更新频率会降低或停止
 * 如果需要在后台使用，需在AndroidManifest.xml中添加：
 * xml
 * <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
 * 并在代码中动态申请（虽然这是定位权限，但传感器在后台使用时可能需要）：
 * kotlin
 * // 在Activity中申请
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
 *     requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1001)
 * }
 */
class SensorRotationObserver(private val mActivity: FragmentActivity) : LifecycleEventObserver {
    // 初始化传感器管理器
    private val sensorManager by lazy { mActivity.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    // 获取旋转矢量传感器
    private val rotationSensor by lazy { sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    // 获取窗口管理器用于获取显示旋转信息
    private val windowManager by lazy { mActivity.getSystemService(Context.WINDOW_SERVICE) as? WindowManager }
    // 照片旋转角度变量
    var photoRotation = 0

    init {
        mActivity.lifecycle.addObserver(this)
    }

    // 传感器监听器
    private val sensorEventListener by lazy { object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                // 实际使用传感器数据计算旋转角度
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                // 获取设备旋转角度
                val orientations = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientations)
                // 转换为角度（0-360度）
                val degrees = (Math.toDegrees(orientations[0].toDouble()) + 360) % 360
                photoRotation = degrees.toInt()
                // 结合屏幕旋转进行修正
                val displayRotation = windowManager?.defaultDisplay?.rotation ?: 0
                photoRotation += when (displayRotation) {
                    Surface.ROTATION_0 -> 0    // 竖屏（自然方向）
                    Surface.ROTATION_90 -> 90   // 横向（右侧横屏）
                    Surface.ROTATION_180 -> 180 // 倒置竖屏
                    Surface.ROTATION_270 -> 270 // 横向（左侧横屏）
                    else -> 0
                }
                photoRotation %= 360
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // 精度变化时的处理（通常无需特别操作）
        }
    }}

    /**
     * 注册传感器监听
     */
    fun registerSensorListener() {
        if (rotationSensor == null) {
            // 设备不支持旋转传感器
            return
        }
        // 先取消再注册，避免重复注册
        unregisterSensorListener()
        rotationSensor?.let {
            /**
             * SENSOR_DELAY_NORMAL
             * 延迟：约 200ms（最低频率）
             * 适用场景：普通 UI 刷新、非实时性场景
             * 特点：功耗最低，适合对实时性要求不高的场景
             *
             * SENSOR_DELAY_UI
             * 延迟：约 60ms
             * 适用场景：需要响应较快的 UI 交互（如旋转屏幕更新 UI）
             * 特点：平衡了功耗和响应速度
             *
             * SENSOR_DELAY_GAME
             * 延迟：约 20ms
             * 适用场景：游戏、实时交互场景
             * 特点：高频更新，响应迅速，功耗较高
             *
             * SENSOR_DELAY_FASTEST
             * 延迟：约 5ms（最高频率，取决于硬件）
             * 适用场景：需要最高精度的场景（如 AR 应用）
             * 特点：更新频率最高，功耗最大，可能导致性能问题
             */
            sensorManager?.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * 取消传感器监听
     */
    fun unregisterSensorListener() {
        sensorManager?.unregisterListener(sensorEventListener)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> registerSensorListener()
            Lifecycle.Event.ON_PAUSE -> unregisterSensorListener()
            Lifecycle.Event.ON_DESTROY -> {
                unregisterSensorListener()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}