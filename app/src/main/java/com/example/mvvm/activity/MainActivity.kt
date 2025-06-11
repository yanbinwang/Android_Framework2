package com.example.mvvm.activity

import android.view.View
import android.view.View.OnClickListener
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.utils.USBTransfer

/**
 * 首页
 * <activity
 *     android:name="..."
 *     ...>
 *     <intent-filter>
 *         <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
 *     </intent-filter>
 *     <meta-data
 *         android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
 *         android:resource="@xml/device_filter" />
 * </activity>
 * https://github.com/LXTTTTTT/USBtoSerialPortDemo
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>(), OnClickListener {
    private val usbTransfer by lazy { USBTransfer(this) }

    override fun initEvent() {
        super.initEvent()
        usbTransfer.setOnUSBDateReceiveListener(object :
            USBTransfer.OnUSBDateReceiveListener {
            override fun onConnect(flag: Boolean, reason: String?) {
            }

            override fun onDisconnect() {
            }

            override fun onReceive(reason: String?) {
                mBinding?.tvReceive?.append("receive: $reason\r\n")
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // 连接
            R.id.tv_connect -> usbTransfer.connect()
            // 下发数据
            R.id.tv_send -> {
                val content_str = mBinding?.etContent?.text.toString()
                usbTransfer.write(content_str)
                mBinding?.tvReceive?.append("send: $content_str\r\n")
            }
            // 断开
            R.id.tv_disconnect -> usbTransfer.disconnect()
        }
    }

}