package com.example.mvvm.activity

import android.view.View
import android.view.View.OnClickListener
import com.example.common.base.BaseActivity
import com.example.common.config.RouterPath
import com.example.framework.utils.function.view.clicks
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.utils.USBTransfer
import com.therouter.router.Route

/**
 * 首页
 * <activity
 *     android:name="..."
 *     android:exported="true"
 *     ...>
 *     <intent-filter>
 *         <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
 *     </intent-filter>
 *     <meta-data
 *         android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
 *         android:resource="@xml/device_filter" />
 * </activity>
 *
 * android:exported="true"
 * 1) 安卓系统 发现 USB 设备插入时
 * 2) 系统要主动唤醒、打开你的这个 MainActivity
 * 3) 系统 = 外部调用者
 */
@Route(path = RouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), OnClickListener {
    private val usbTransfer by lazy { USBTransfer(this) }

    override fun initEvent() {
        super.initEvent()
        clicks(mBinding?.tvConnect, mBinding?.tvSend, mBinding?.tvDisconnect)
        usbTransfer.setOnUSBDateReceiveListener(object : USBTransfer.OnUSBReceiveListener {
            override fun onConnected() {
                mBinding?.tvState?.text = "连接成功"
            }

            override fun onConnectionFailed(message: String?) {
                mBinding?.tvState?.text = "连接失败"
            }

            override fun onDisconnected() {
                mBinding?.tvState?.text = "未连接"
            }

            override fun onReceive(reason: String?) {
                mBinding?.tvReceive?.append("receive: $reason\r\n")
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // 连接
            R.id.tv_connect -> {
                mBinding?.tvState?.text = "连接中"
                usbTransfer.connect()
            }
            // 下发数据
            R.id.tv_send -> {
                val content_str = mBinding?.etContent?.text.toString()
                usbTransfer.send(content_str)
                mBinding?.tvReceive?.append("send: $content_str\r\n")
            }
            // 断开
            R.id.tv_disconnect -> usbTransfer.disconnect()
        }
    }

}