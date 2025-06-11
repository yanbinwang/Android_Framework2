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
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
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
    private var readBuffer = ByteArray(1024 * 2) //缓冲区
    private var listener: OnUSBDateReceiveListener? = null//接口
    private var availableDrivers: MutableList<UsbSerialDriver> = ArrayList() // 所有可用设备
    private var usbSerialDriver: UsbSerialDriver? = null//当前连接的设备（目标设备对象）
    private var usbDeviceConnection: UsbDeviceConnection? = null//设备连接对象
    private var usbSerialPort: UsbSerialPort? = null//设备端口对象，通过这个读写数据，一般只有1个
    private var inputOutputManager: SerialInputOutputManager? = null//数据输入输出流管理器
    private var IDENTIFICATION = " USB-Serial Controller D"//目标设备标识
    private var baudRate = 115200//波特率
    private val dataBits = 8//数据位
    private val stopBits = UsbSerialPort.STOPBITS_1//停止位
    private val parity = UsbSerialPort.PARITY_NONE//奇偶校验（固定的8/1/none）
    private val manager by lazy { mActivity.getSystemService(Context.USB_SERVICE) as? UsbManager }
    private val handler by lazy { WeakHandler(Looper.getMainLooper()) }
    private val TAG = "USBTransfer"
    //广播监听：判断usb设备授权操作
    private val receiver by lazy { object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            "onReceive: ${intent.action}".logE(TAG)
            if (INTENT_ACTION_GRANT_USB == intent.action) {
                // 授权操作完成，连接
//                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);  // 不知为何获取到的永远都是 false 因此无法判断授权还是拒绝
                connectDevice()
            }
        }
    }}
    private val hasPermission get() = manager?.hasPermission(usbSerialDriver?.device).orFalse

    companion object {
        @Volatile
        private var isConnect = false
        //usb权限请求标识->广播
        private val INTENT_ACTION_GRANT_USB = "${Constants.APPLICATION_ID}.INTENT_ACTION_GRANT_USB"
    }

    init {
        mActivity.lifecycle.addObserver(this)
    }

    /**
     * 开始建立连接
     */
    fun connect() {
        if (!isConnect) {
            registerReceiver()
            refreshDevice()
            connectDevice()
        }
    }

    /**
     * 注册usb授权监听广播
     */
    private fun registerReceiver() {
        mActivity.registerReceiver(receiver, IntentFilter(INTENT_ACTION_GRANT_USB))
    }

    /**
     * 刷新当前可用 usb设备，拿到已连接的usb设备列表
     */
    private fun refreshDevice() {
        availableDrivers.clear()
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        "当前可用 usb 设备数量: ${availableDrivers.safeSize}".logE(TAG)
        //有设备可以连接
        if (availableDrivers.safeSize != 0) {
            //开发用的定制平板电脑有2个及以上的usb口，会搜索到多个
            if (availableDrivers.safeSize > 1) {
                for (i in availableDrivers.indices) {
                    val availableDriver = availableDrivers.safeGet(i)
                    val productName = availableDriver?.device?.productName
                    "productName: $productName".logE(TAG)
                    //通过 ProductName 参数来识别要连接的设备
                    if (productName == IDENTIFICATION) {
                        usbSerialDriver = availableDriver
                    }
                }
            } else {
                usbSerialDriver = availableDrivers.safeGet(0)
            }
            //一般设备的端口都只有一个，具体要参考设备的说明文档
            usbSerialPort = usbSerialDriver?.ports.safeGet(0)
            //同时申请设备权限
            if (!hasPermission) {
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                val usbPermissionIntent = PendingIntent.getBroadcast(mActivity, 0, Intent(INTENT_ACTION_GRANT_USB), flags)
                manager?.requestPermission(usbSerialDriver?.device, usbPermissionIntent)
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
        //判断是否拥有权限
        if (hasPermission) {
            //拿到连接对象
            usbDeviceConnection = manager?.openDevice(usbSerialDriver?.device)
            try {
                //打开串口
                usbSerialPort?.open(usbDeviceConnection)
                //设置串口参数：波特率 - 115200 ， 数据位 - 8 ， 停止位 - 1 ， 奇偶校验 - 无
                usbSerialPort?.setParameters(baudRate, dataBits, stopBits, parity)
                startReceiveData()//开启数据监听
                initDevice()//下发初始化指令
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            listener?.onConnect(false, "请先授予权限再连接")
        }
    }

    private fun startReceiveData() {
        if (usbSerialPort == null || !usbSerialPort?.isOpen.orFalse) return
        inputOutputManager = SerialInputOutputManager(usbSerialPort, object : SerialInputOutputManager.Listener {
            private val baos = ByteArrayOutputStream()
            override fun onNewData(data: ByteArray) {
                //在这里处理接收到的 usb 数据 -------------------------------
                //按照结尾标识符处理
                baos.write(data, 0, data.size)
                readBuffer = baos.toByteArray()
                if (readBuffer.size >= 2 && readBuffer[readBuffer.size - 2] == '\r'.code.toByte() && readBuffer[readBuffer.size - 1] == '\n'.code.toByte()) {
                    val reason = bytes2string(readBuffer)
                    "收到 usb 数据: $reason".logI(TAG)
                    if (listener != null) {
                        handler.post { listener?.onReceive(reason) }
                    }
                    //重置
                    baos.reset()
                }
                baos.close()
//                //直接处理
//               String data_str = bytes2string(data);
//               Log.i(TAG, "收到 usb 数据: " + data_str);
            }

            override fun onRunError(e: Exception) {
                "usb 断开了".logE(TAG)
                baos.close()
                disconnect()
            }
        })
        inputOutputManager?.start()
        //修改连接标识
        isConnect = true
        listener?.onConnect(true, "连接成功")
    }

    private fun bytes2string(bytes: ByteArray?): String? {
        if (bytes == null) {
            return ""
        }
        var newStr: String? = null
        try {
            newStr = String(bytes, charset("GB18030")).trim()
        } catch (_: UnsupportedEncodingException) {
        }
        return newStr
    }

    private fun initDevice() {
        try {
            Thread.sleep(500)
        } catch (_: InterruptedException) {
        }
        //查询 IC 信息
        write("6861686168610D0A")
    }

    /**
     * 下发数据：建议使用线程池
     */
    fun write(data: String?) {
        data ?: return
        if (usbSerialPort != null) {
            "当前usb状态: isOpen-${usbSerialPort?.isOpen}".logE(TAG)
            // 当串口打开时再下发
            if (usbSerialPort?.isOpen.orFalse) {
                //将字符数据转化为 byte[]
                val dataBytes = hex2bytes(data)
                if (dataBytes == null || dataBytes.isEmpty()) return
                try {
                    //写入数据，延迟设置太大的话如果下发间隔太小可能报错
                    usbSerialPort?.write(dataBytes, 0)
                } catch (_: IOException) {
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
            //停止数据接收监听
            if (inputOutputManager != null) {
                inputOutputManager?.stop()
                inputOutputManager = null
            }
            //关闭端口
            if (usbSerialPort != null) {
                usbSerialPort?.close()
                usbSerialPort = null
            }
            //关闭连接
            if (usbDeviceConnection != null) {
                usbDeviceConnection?.close()
                usbDeviceConnection = null
            }
            //清除设备
            if (usbSerialDriver != null) {
                usbSerialDriver = null
            }
            //清空设备列表
            availableDrivers.clear()
            //注销广播监听
            mActivity.unregisterReceiver(receiver)
            //修改标识
            if (isConnect) {
                isConnect = false
            }
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