package com.example.thirdparty.media.service.observer

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyCallback.CallStateListener
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 来电监听
 */
@RequiresApi(Build.VERSION_CODES.S)
class TelephonyObserver(private val mActivity: FragmentActivity) : LifecycleEventObserver {
    private val manager by lazy { mActivity.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager }
    private val phoneState by lazy { MyPhoneStateListener() }
    private val callState by lazy { MyCallStateListener() }
    private val highVersion get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    private var listener: OnTelephonyListener? = null

    init {
        mActivity.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> register()
            Lifecycle.Event.ON_PAUSE -> unregister()
            Lifecycle.Event.ON_DESTROY -> {
                unregister()
                listener = null
                mActivity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

    private fun register() {
        unregister()
        if (highVersion) {
            manager?.registerTelephonyCallback(mActivity.mainExecutor, callState)
        } else {
            manager?.listen(phoneState, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun unregister() {
        if (highVersion) {
            manager?.unregisterTelephonyCallback(callState)
        } else {
            manager?.listen(phoneState, PhoneStateListener.LISTEN_NONE)
        }
    }

    /**
     * 设置回调监听
     */
    fun setOnTelephonyListener(mListener: OnTelephonyListener) {
        listener = mListener
    }

    /**
     * 回调监听
     */
    interface OnTelephonyListener {
        /**
         * 空闲状态
         */
        fun onIdle()

        /**
         * 来电话状态
         */
        fun onRinging()

        /**
         * 接电话状态
         */
        fun onOffHook()
    }

    /**
     * inner class 是一种特殊的内部类，它可以访问外部类的成员（包括私有成员），并且持有一个对外部类实例的引用。
     * 这使得内部类能够与外部类进行更紧密的交互，方便实现一些需要访问外部类状态的功能，比如事件监听器、迭代器等。
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private inner class MyCallStateListener : TelephonyCallback(), CallStateListener {
        override fun onCallStateChanged(state: Int) {
            onChanged(state)
        }
    }

    private inner class MyPhoneStateListener : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onCallStateChanged(state: Int, phoneNumber: String) {
            super.onCallStateChanged(state, phoneNumber)
            onChanged(state)
        }
    }

    /**
     * 全局回调处理
     */
    private fun onChanged(state: Int) {
        when (state) {
            // 手机状态：空闲状态
            TelephonyManager.CALL_STATE_IDLE -> listener?.onIdle()
            // 手机状态：来电话状态
            TelephonyManager.CALL_STATE_RINGING -> listener?.onRinging()
            // 手机状态：接电话状态
            TelephonyManager.CALL_STATE_OFFHOOK -> listener?.onOffHook()
        }
    }

}