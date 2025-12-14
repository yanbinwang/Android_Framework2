package com.example.thirdparty.media.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.os.Build
import android.os.Parcelable
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.logWTF
import java.lang.ref.WeakReference

/**
 * USB 设备连接状态广播接收器
 * 支持：USB 设备插拔、USB 存储连接状态监听
 * fun getUsbIntentFilter(): IntentFilter {
 * return IntentFilter().apply {
 *     addAction(ACTION_USB_STATE)
 *     addAction(ACTION_USB_ATTACHED)
 *     addAction(ACTION_USB_DETACHED)
 *     // 可选：添加 USB 存储相关广播（针对需要监听 U 盘的场景）
 *     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
 *         addAction(Intent.ACTION_MEDIA_MOUNTED)
 *         addAction(Intent.ACTION_MEDIA_UNMOUNTED)
 *         addDataScheme("file") // 必须添加，否则无法接收存储广播
 *     }
 * }
 * }
 */
class USBBroadCastReceiver : BroadcastReceiver() {
    // 回调接口实例（弱引用避免内存泄漏）
    private var stateChangeListener: WeakReference<OnUSBStateChangeListener>? = null

    companion object {
        /**
         * 日志
         */
        private const val TAG = "USBBroadCastReceiver"

        /**
         * USB 设备信息的 Extra Key（硬编码 "device"，全版本兼容）
         */
        private const val EXTRA_USB_DEVICE = "device"

        /**
         * usb线和外设的广播
         */
        private const val RECEIVER_USB_STATE = "android.hardware.usb.action.USB_STATE"
        private const val RECEIVER_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
        private const val RECEIVER_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"

        /**
         * USB 设备连接状态（volatile 保证多线程可见性）
         * true：有 USB 设备连接（包括外设、USB 存储）
         */
        @Volatile
        var isUSBDeviceConnected = false
            private set

        /**
         * 获取 USB 广播过滤器（简化注册流程）
         */
        fun getUsbIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(RECEIVER_USB_STATE)
                addAction(RECEIVER_USB_ATTACHED)
                addAction(RECEIVER_USB_DETACHED)
                // 可选：USB 存储挂载/卸载广播（U 盘场景）
                addAction(Intent.ACTION_MEDIA_MOUNTED)
                addAction(Intent.ACTION_MEDIA_UNMOUNTED)
                // 必需，否则无法接收存储广播
                addDataScheme("file")
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action ?: return
        when (action) {
            // USB 设备物理连接/移除
            RECEIVER_USB_ATTACHED -> {
                // 用硬编码 "device" 获取 UsbDevice，全版本兼容
                val usbDevice = getUsbDeviceFromIntent(intent)
                updateConnectedState(true, usbDevice)
            }
            RECEIVER_USB_DETACHED -> {
                updateConnectedState(false, null)
            }
            // USB 状态变化（存储连接、充电模式切换等）
            RECEIVER_USB_STATE -> {
                val isConnected = intent.extras?.getBoolean("connected") ?: false
                if (isConnected != isUSBDeviceConnected) {
                    updateConnectedState(isConnected, null)
                }
            }
            // USB 存储挂载/卸载
            Intent.ACTION_MEDIA_MOUNTED -> {
                updateConnectedState(true, null)
            }
            Intent.ACTION_MEDIA_UNMOUNTED -> {
                updateConnectedState(false, null)
            }
        }
    }

    /**
     * 从 Intent 中获取 UsbDevice（兼容所有 Android 版本）
     */
    private fun getUsbDeviceFromIntent(intent: Intent): UsbDevice? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+：直接用泛型方法（Extra Key 仍为 "device"）
                intent.getParcelableExtra(EXTRA_USB_DEVICE, UsbDevice::class.java)
            } else {
                // Android 12-：用旧版方法，强制类型转换
                intent.getParcelableExtra<Parcelable>(EXTRA_USB_DEVICE) as? UsbDevice
            }
        } catch (e: Exception) {
            "获取 UsbDevice 失败：${e.message}".logWTF(TAG)
            null
        }
    }

    /**
     * 更新连接状态并触发回调
     */
    private fun updateConnectedState(isConnected: Boolean, usbDevice: UsbDevice?) {
        isUSBDeviceConnected = isConnected
        stateChangeListener?.get()?.let { listener ->
            if (isConnected) {
                listener.onUSBConnected(usbDevice)
            } else {
                listener.onUSBDisconnected()
            }
        }
        // 打印设备详情（调试用）
        val deviceInfo = usbDevice?.run {
            "名称：$deviceName，厂商ID：$vendorId，产品ID：$productId"
        } ?: "无设备信息"
        "USB 状态：${if (isConnected) "✅ 已连接" else "❌ 已断开"}，设备：$deviceInfo".logWTF(TAG)
    }

    /**
     * 设置点击
     */
    fun setOnUSBStateChangeListener(listener: OnUSBStateChangeListener?) {
        stateChangeListener = listener?.let { WeakReference(it) }
    }

    /**
     * 注销广播时清除回调（避免内存泄漏）
     */
    fun clearListener() {
        isUSBDeviceConnected = false
        stateChangeListener?.clear()
        stateChangeListener = null
    }

    /**
     * 状态回调接口（替代静态变量，更灵活）
     */
    interface OnUSBStateChangeListener {
        /**
         * 连接成功（device 为具体设备信息）
         */
        fun onUSBConnected(device: UsbDevice? = null)

        /**
         * 断开连接
         */
        fun onUSBDisconnected()
    }

}