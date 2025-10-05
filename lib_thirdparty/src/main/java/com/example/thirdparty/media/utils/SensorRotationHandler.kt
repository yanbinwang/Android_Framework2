package com.example.thirdparty.media.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager

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
 */
class SensorRotationHandler(context: Context) {
    // 初始化传感器管理器
    private val sensorManager by lazy { context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager }
    // 获取旋转矢量传感器
    private val rotationSensor by lazy { sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    // 获取窗口管理器用于获取显示旋转信息
    private val windowManager by lazy { context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager }
    // 照片旋转角度变量
    var photoRotation = 0

    // 传感器监听器
    private val sensorEventListener by lazy { object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                // 将旋转矢量转换为旋转矩阵
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                // 计算设备相对于自然方向的旋转角度
                val rotation = windowManager?.defaultDisplay?.rotation
                photoRotation = when (rotation) {
                    Surface.ROTATION_0 -> 0    // 竖屏（自然方向）
                    Surface.ROTATION_90 -> 90   // 横向（右侧横屏）
                    Surface.ROTATION_180 -> 180 // 倒置竖屏
                    Surface.ROTATION_270 -> 270 // 横向（左侧横屏）
                    else -> 0
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // 精度变化时的处理（通常无需特别操作）
        }
    }}

    // 注册传感器监听
    fun registerSensorListener() {
        rotationSensor?.let {
            sensorManager?.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // 取消传感器监听（重要：避免内存泄漏）
    fun unregisterSensorListener() {
        sensorManager?.unregisterListener(sensorEventListener)
    }

}