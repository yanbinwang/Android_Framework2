package com.example.mvvm.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Looper
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.config.Constants
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.logE
import com.example.framework.utils.logI
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException

@SuppressLint("UnspecifiedRegisterReceiverFlag")
class USBTransfer(private val mActivity: FragmentActivity) : LifecycleEventObserver {
    private var isConnectUSB = false
    private var readBuffer = ByteArray(1024 * 2) // 缓冲区
    private var receiver: BroadcastReceiver? = null // 广播监听：判断usb设备授权操作
    private var listener: OnUSBDateReceiveListener? = null//接口
    // 顺序： manager - availableDrivers（所有可用设备） - UsbSerialDriver（目标设备对象） - UsbDeviceConnection（设备连接对象） - UsbSerialPort（设备的端口，一般只有1个）
    private var availableDrivers: MutableList<UsbSerialDriver> = ArrayList() // 所有可用设备
    private var usbSerialDriver: UsbSerialDriver? = null // 当前连接的设备
    private var usbDeviceConnection: UsbDeviceConnection? = null // 连接对象
    private var usbSerialPort: UsbSerialPort? = null // 设备端口对象，通过这个读写数据
    private var inputOutputManager: SerialInputOutputManager? = null // 数据输入输出流管理器
    // 连接参数，按需求自行修改，一般情况下改变的参数只有波特率，数据位、停止位、奇偶校验都是固定的8/1/none ---------------------
    private var baudRate = 115200 // 波特率
    private val dataBits = 8 // 数据位
    private val stopBits = UsbSerialPort.STOPBITS_1 // 停止位
    private val parity = UsbSerialPort.PARITY_NONE // 奇偶校验
    private val baos by lazy { ByteArrayOutputStream() }
    //静态参数
    private val manager by lazy { mActivity.getSystemService(Context.USB_SERVICE) as? UsbManager }
    private val handler by lazy { WeakHandler(Looper.getMainLooper()) }
    private val INTENT_ACTION_GRANT_USB = "${Constants.APPLICATION_ID}.INTENT_ACTION_GRANT_USB" // usb权限请求标识
    private val IDENTIFICATION = " USB-Serial Controller D" // 目标设备标识
    private val TAG = "USBTransfer"

    init {
        mActivity.lifecycle.addObserver(this)
    }

    fun connect() {
        if (!isConnectUSB) {
            registerReceiver() // 注册广播监听
            refreshDevice() // 拿到已连接的usb设备列表
            connectDevice() // 建立连接
        }
    }

    // 注册usb授权监听广播
    fun registerReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                "onReceive: ${intent.action}".logE(TAG)
                if (INTENT_ACTION_GRANT_USB == intent.action) {
                    // 授权操作完成，连接
//                    boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);  // 不知为何获取到的永远都是 false 因此无法判断授权还是拒绝
                    connectDevice()
                }
            }
        }
        mActivity.registerReceiver(receiver, IntentFilter(INTENT_ACTION_GRANT_USB))
    }

    // 刷新当前可用 usb设备
    fun refreshDevice() {
        availableDrivers.clear()
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        "当前可用 usb 设备数量: ${availableDrivers.size}".logE(TAG)
        // 有设备可以连接
        if (availableDrivers.size != 0) {
            // 当时开发用的是定制平板电脑有 2 个usb口，所以搜索到两个
            if (availableDrivers.size > 1) {
                for (i in availableDrivers.indices) {
                    val availableDriver = availableDrivers[i]
                    val productName = availableDriver.device.productName
                    "productName: $productName".logE(TAG)
                    // 我是通过 ProductName 这个参数来识别我要连接的设备
                    if (productName == IDENTIFICATION) {
                        usbSerialDriver = availableDriver
                    }
                }
            } else {
                usbSerialDriver = availableDrivers[0]
            }
            usbSerialPort = usbSerialDriver?.ports.safeGet(0) // 一般设备的端口都只有一个，具体要参考设备的说明文档
            // 同时申请设备权限
            if (!manager?.hasPermission(usbSerialDriver?.device).orFalse) {
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                val usbPermissionIntent = PendingIntent.getBroadcast(mActivity, 0, Intent(INTENT_ACTION_GRANT_USB), flags)
                manager?.requestPermission(usbSerialDriver?.device, usbPermissionIntent)
            }
        } else {
            "请先接入设备".shortToast()
        }
    }

    // 连接设备
    fun connectDevice() {
        if (usbSerialDriver == null || inputOutputManager != null) {
            return
        }
        // 判断是否拥有权限
        val hasPermission = manager?.hasPermission(usbSerialDriver?.device).orFalse
        if (hasPermission) {
            usbDeviceConnection = manager?.openDevice(usbSerialDriver?.device) // 拿到连接对象
            if (usbSerialPort == null) {
                return
            }
            try {
                usbSerialPort?.open(usbDeviceConnection) // 打开串口
                usbSerialPort?.setParameters(baudRate, dataBits, stopBits, parity) // 设置串口参数：波特率 - 115200 ， 数据位 - 8 ， 停止位 - 1 ， 奇偶校验 - 无
                startReceiveData() // 开启数据监听
                init_device() // 下发初始化指令
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            "请先授予权限再连接".shortToast()
        }
    }

    // 开启数据接收监听
    fun startReceiveData() {
        if (usbSerialPort == null || !usbSerialPort?.isOpen.orFalse) {
            return
        }
        inputOutputManager = SerialInputOutputManager(usbSerialPort, object : SerialInputOutputManager.Listener {
            override fun onNewData(data: ByteArray) {
                // 在这里处理接收到的 usb 数据 -------------------------------
                // 按照结尾标识符处理
                baos.write(data, 0, data.size)
                readBuffer = baos.toByteArray()
                if (readBuffer.size >= 2 && readBuffer[readBuffer.size - 2] == '\r'.code.toByte() && readBuffer[readBuffer.size - 1] == '\n'.code.toByte()) {
                    val data_str = bytes2string(readBuffer)
                    "收到 usb 数据: $data_str".logI(TAG)
                    if (listener != null) {
                        handler.post { listener?.onReceive(data_str) }
                    }
                    baos.reset() // 重置
                }
                    // 直接处理
//               String data_str = bytes2string(data);
//               Log.i(TAG, "收到 usb 数据: " + data_str);
            }

            override fun onRunError(e: Exception) {
                "usb 断开了".logE(TAG)
                disconnect()
                e.printStackTrace()
            }
        })
        inputOutputManager?.start()
        isConnectUSB = true // 修改连接标识
        "连接成功".shortToast()
    }

    // 下发数据：建议使用线程池
    fun write(data_hex: String?) {
        data_hex ?: return
        if (usbSerialPort != null) {
            "当前usb状态: isOpen-${usbSerialPort?.isOpen}".logE(TAG)
            // 当串口打开时再下发
            if (usbSerialPort?.isOpen.orFalse) {
                val data_bytes = hex2bytes(data_hex) // 将字符数据转化为 byte[]
                if (data_bytes == null || data_bytes.isEmpty()) return
                try {
                    usbSerialPort?.write(data_bytes, 0) // 写入数据，延迟设置太大的话如果下发间隔太小可能报错
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                "write: usb 未连接".logE(TAG)
            }
        }
    }

    // 断开连接
    fun disconnect() {
        try {
            // 停止数据接收监听
            if (inputOutputManager != null) {
                inputOutputManager?.stop()
                inputOutputManager = null
            }
            // 关闭端口
            if (usbSerialPort != null) {
                usbSerialPort?.close()
                usbSerialPort = null
            }
            // 关闭连接
            if (usbDeviceConnection != null) {
                usbDeviceConnection?.close()
                usbDeviceConnection = null
            }
            // 清除设备
            if (usbSerialDriver != null) {
                usbSerialDriver = null
            }
            // 清空设备列表
            availableDrivers.clear()
            // 注销广播监听
            if (receiver != null) {
                mActivity.unregisterReceiver(receiver)
            }
            if (isConnectUSB) {
                isConnectUSB = false // 修改标识
            }
            "断开连接".logE(TAG)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 下发设备初始化指令
    fun init_device() {
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        write("6861686168610D0A") // 查询 IC 信息
    }

    fun bytes2string(bytes: ByteArray?): String? {
        if (bytes == null) {
            return ""
        }
        var newStr: String? = null
        try {
            newStr = String(bytes, charset("GB18030")).trim { it <= ' ' }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return newStr
    }

    fun hex2bytes(hex: String?): ByteArray? {
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
            e.printStackTrace()
            return null
        }
        return bytes
    }

    fun string2Hex(str: String?): String? {
        val hex: String
        try {
            val bytes = string2bytes(str, "GB18030")
            hex = bytes2Hex(bytes)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return null
        }
        return hex
    }

    @Throws(UnsupportedEncodingException::class)
    fun string2bytes(str: String?, charset: String?): ByteArray? {
        if (str == null) {
            return null
        }
        return str.toByteArray(charset(charset!!))
    }

    fun bytes2Hex(bytes: ByteArray?): String {
        if (bytes == null) return ""
        var hex = ""
        for (i in bytes.indices) {
            val value = bytes[i].toInt() and 0xff
            var hexVaule = Integer.toHexString(value)
            if (hexVaule.length < 2) {
                hexVaule = "0$hexVaule"
            }
            hex += hexVaule
        }
        return hex
    }

    fun setBaudRate(baudRate: Int) {
        this.baudRate = baudRate
    }

    /**
     * 设置回调接口
     */
    fun setOnUSBDateReceiveListener(listener: OnUSBDateReceiveListener?) {
        this.listener = listener
    }

    /**
     * 回信接口
     */
    interface OnUSBDateReceiveListener {
        fun onReceive(data_str: String?)
    }

    /**
     * 生命周期管控
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            // 当系统监测到usb插入动作后跳转到此页面时
            Lifecycle.Event.ON_RESUME -> connect()
            Lifecycle.Event.ON_DESTROY -> {
                disconnect()
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

}