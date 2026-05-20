package com.example.mvvm.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.common.config.Constants
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.doOnReceiver
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.logE
import com.example.framework.utils.logI
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException

/**
 *  <!-- 基础USB权限 -->
 *     <uses-permission android:name="android.permission.USB_ACCESSORY" />
 *     <uses-permission android:name="android.permission.MANAGE_USB"
 *         tools:ignore="ProtectedPermissions" />
 *     <!-- 监听USB插拔广播必备 -->
 *     <uses-permission android:name="android.permission.BROADCAST_STICKY" />
 *     <!--
 *         设备特性声明
 *         声明设备支持 USB 主机模式，无此硬件设备直接不安装
 *     -->
 *     <uses-feature
 *         android:name="android.hardware.usb.host"
 *         android:required="true" />
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
class USBTransfer(private val mActivity: FragmentActivity) {
    // 全局唯一的拼接流
    private val receiveBuffer = ByteArrayOutputStream()
    // 顺序： manager - availableDrivers（所有可用设备） - UsbSerialDriver（目标设备对象） - UsbDeviceConnection（设备连接对象） - UsbSerialPort（设备的端口，一般只有1个）
    private var availableDrivers = mutableListOf<UsbSerialDriver>() // 所有可用设备
    private var usbSerialDriver: UsbSerialDriver? = null // 当前连接的设备
    private var usbDeviceConnection: UsbDeviceConnection? = null // 连接对象
    private var usbSerialPort: UsbSerialPort? = null // 设备端口对象，通过这个读写数据
    private var inputOutputManager: SerialInputOutputManager? = null // 数据输入输出流管理器
    // 连接参数，按需求自行修改，一般情况下改变的参数只有波特率，数据位、停止位、奇偶校验都是固定的8/1/none ---------------------
    private var baudRate = 115200 // 波特率
    private val dataBits = 8 // 数据位
    private val stopBits = UsbSerialPort.STOPBITS_1 // 停止位
    private val parity = UsbSerialPort.PARITY_NONE // 奇偶校验
    // 发送数据的job
    private var sendJob: Job? = null
    // 回调接口
    private var listener: OnUSBDateReceiveListener? = null
    // USB管理类
    private val manager by lazy { mActivity.getSystemService(Context.USB_SERVICE) as? UsbManager }
    // 广播监听：判断usb设备授权操作
    private val receiver by lazy { object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            "onReceive: ${intent.action}".logE(TAG)
            if (ACTION_GRANT_USB == intent.action) {
                // 授权操作完成，连接
//                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) // 不知为何获取到的永远都是 false 因此无法判断授权还是拒绝
                connectDevice()
            }
        }
    } }
    // 是否具备USB权限
    private val hasPermission get() = manager?.hasPermission(usbSerialDriver?.device).orFalse

    companion object {
        // usb权限请求标识
        private val ACTION_GRANT_USB = "${Constants.APPLICATION_ID}.ACTION_GRANT_USB"
        // 目标设备标识
        private const val IDENTIFICATION = " USB-Serial Controller D"
        // 日志输出标识
        private const val TAG = "USBTransfer"
    }

    init {
        // 页面销毁断开连接
        mActivity.doOnDestroy {
            disconnect()
            receiveBuffer.close()
            sendJob?.cancel()
        }
        // 注册usb授权监听广播
        mActivity.doOnReceiver(receiver, IntentFilter(ACTION_GRANT_USB), isExported = false)
    }

    /**
     * 开始建立连接
     */
    fun connect() {
        disconnect()
        findAllDrivers()
    }

    /**
     * 刷新当前可用 usb设备，拿到已连接的usb设备列表
     */
    private fun findAllDrivers() {
        availableDrivers.clear()
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        "当前可用 usb 设备数量: ${availableDrivers.safeSize}".logE(TAG)
        // 有设备可以连接
        if (!availableDrivers.isEmpty()) {
            // 开发用的定制平板电脑有2个及以上的usb口，会搜索到多个
            if (availableDrivers.safeSize > 1) {
                for (i in availableDrivers.indices) {
                    val availableDriver = availableDrivers.safeGet(i)
                    val productName = availableDriver?.device?.productName
                    "productName: $productName".logE(TAG)
                    // 通过 ProductName 参数来识别要连接的设备
                    if (productName == IDENTIFICATION) {
                        usbSerialDriver = availableDriver
                    }
                }
            } else {
                usbSerialDriver = availableDrivers.safeGet(0)
            }
            // 一般设备的端口都只有一个，具体要参考设备的说明文档
            usbSerialPort = usbSerialDriver?.ports.safeGet(0)
            // 同时申请设备权限
            if (!hasPermission) {
                val usbPermissionIntent = PendingIntent.getBroadcast(mActivity, 0, Intent(ACTION_GRANT_USB), PendingIntent.FLAG_IMMUTABLE)
                manager?.requestPermission(usbSerialDriver?.device, usbPermissionIntent)
            } else {
                connectDevice()
            }
        } else {
            listener?.onConnect(false, "请先接入设备")
        }
    }

    /**
     * 连接设备
     */
    private fun connectDevice() {
        if (usbSerialDriver == null || inputOutputManager != null || usbSerialPort == null) return
        // 判断是否拥有权限
        if (hasPermission) {
            try {
                // 拿到连接对象
                usbDeviceConnection = manager?.openDevice(usbSerialDriver?.device)
                // 打开串口
                usbSerialPort?.open(usbDeviceConnection)
                // 设置串口参数：波特率 - 115200 ， 数据位 - 8 ， 停止位 - 1 ， 奇偶校验 - 无
                usbSerialPort?.setParameters(baudRate, dataBits, stopBits, parity)
                // 开启数据监听
                startReceiveData()
//                // 下发初始化指令
//                initDevice()
            } catch (_: IOException) {
            }
        } else {
            listener?.onConnect(false, "请先授予权限再连接")
        }
    }

    private fun startReceiveData() {
        if (usbSerialPort == null || !usbSerialPort?.isOpen.orFalse) return
        var isConnect = false
        inputOutputManager = SerialInputOutputManager(usbSerialPort, object : SerialInputOutputManager.Listener {
            override fun onNewData(data: ByteArray) {
                try {
                    // 直接往全局buffer写
                    receiveBuffer.write(data)
                    val fullData = receiveBuffer.toByteArray()
                    // 判断是否结束
                    if (fullData.endWithCRLF()) {
                        val reason = bytes2string(fullData)
                        "收到 usb 完整数据: $reason".logI(TAG)
                        mActivity.runOnUiThread {
                            listener?.onReceive(reason)
                        }
                        // 清空，准备下一包
                        receiveBuffer.reset()
                    }
                    // 连接成功只回调一次
                    if (!isConnect) {
                        isConnect = true
                        mActivity.runOnUiThread {
                            listener?.onConnect(true, "连接成功")
                        }
                    }
                } catch (e: Exception) {
                    "接受失败：${e.message}".logE(TAG)
                }
            }

            override fun onRunError(e: Exception) {
                "usb 断开了：${e.message}".logE(TAG)
                disconnect()
            }
        })
        inputOutputManager?.start()
    }

    private fun ByteArray.endWithCRLF(): Boolean {
        return size >= 2 && this[size-2] == '\r'.toByte() && this[size-1] == '\n'.toByte()
    }

    private fun bytes2string(bytes: ByteArray?): String? {
        if (bytes == null) {
            return ""
        }
        var newStr: String? = null
        try {
            newStr = String(bytes, charset("GB18030")).trim { it <= ' ' }
        } catch (_: UnsupportedEncodingException) {
        }
        return newStr
    }

//    /**
//     * 串口打开成功后，自动发一条「初始化指令」给设备， demo 示例代码
//     * 用的是 USB 串口设备（比如单片机、读卡器、传感器、门禁、工业设备）
//     * 这类设备有个规则：
//     * 发什么指令 → 设备返回什么数据
//     * 比如：
//     * 发 6861686168610D0A → 设备返回 设备型号 / 版本 / IC 信息
//     * 发别的 → 设备做别的动作
//     */
//    private fun initDevice() {
//        try {
//            Thread.sleep(500)
//        } catch (_: InterruptedException) {
//        }
//        // 查询 IC 信息
//        send("6861686168610D0A")
//    }

    /**
     * 下发数据：建议使用线程池
     */
    fun send(data: String?) {
        data ?: return
        if (usbSerialPort != null) {
            "当前usb状态: isOpen-${usbSerialPort?.isOpen}".logE(TAG)
            // 当串口打开时再下发
            if (usbSerialPort?.isOpen.orFalse) {
                sendJob?.cancel()
                sendJob = mActivity.lifecycleScope.launch(IO) {
                    try {
                        // 将字符数据转化为 byte[]
                        val dataBytes = hex2bytes(data)
                        if (dataBytes == null || dataBytes.isEmpty()) return@launch
                        // 写入数据，延迟设置太大的话如果下发间隔太小可能报错
                        usbSerialPort?.write(dataBytes, 0)
                    } catch (e: IOException) {
                        "发送失败：${e.message}".logE(TAG)
                    }
                }
            } else {
                "write: usb 未连接".logE(TAG)
            }
        }
    }

    private fun hex2bytes(hex: String?): ByteArray? {
        var mHex = hex
        if (mHex.isNullOrEmpty()) {
            return null
        }
        // 如果长度不是偶数，则前面补0
        if (mHex.length % 2 != 0) {
            mHex = "0$mHex"
        }
        val bytes = ByteArray((mHex.length + 1) / 2)
        try {
            var i = 0
            var j = 0
            while (i < mHex.length) {
                val hight = ((mHex[i].digitToIntOrNull(16) ?: (-1 and 0xff))).toByte()
                val low = ((mHex[i + 1].digitToIntOrNull(16) ?: (-1 and 0xff))).toByte()
                bytes[j++] = (hight.toInt() shl 4 or low.toInt()).toByte()
                i += 2
            }
        } catch (e: Exception) {
            return null
        }
        return bytes
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        try {
            // 停止数据接收监听
            inputOutputManager?.stop()
            inputOutputManager = null
            // 关闭端口
            usbSerialPort?.close()
            usbSerialPort = null
            // 关闭连接
            usbDeviceConnection?.close()
            usbDeviceConnection = null
            // 清除设备
            usbSerialDriver = null
            // 清空设备列表
            availableDrivers.clear()
            listener?.onDisconnect()
            "断开连接".logE(TAG)
        } catch (_: Exception) {
        }
    }

    /**
     * 设置回调接口
     */
    fun setOnUSBDateReceiveListener(listener: OnUSBDateReceiveListener?) {
        this.listener = listener
    }

    /**
     * 回调接口
     */
    interface OnUSBDateReceiveListener {
        /**
         * 连接信息
         */
        fun onConnect(flag: Boolean, reason: String?)

        /**
         * 断开连接
         */
        fun onDisconnect()

        /**
         * 返回信息
         */
        fun onReceive(reason: String?)
    }

}