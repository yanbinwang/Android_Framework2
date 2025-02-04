package com.example.thirdparty.media.service

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

    companion object {
        private var listener: OnTelephonyListener? = null

        @JvmStatic
        @Synchronized
        private fun onChanged(state: Int) {
            when (state) {
                //手机状态：空闲状态
                TelephonyManager.CALL_STATE_IDLE -> listener?.onIdle()
                //手机状态：来电话状态
                TelephonyManager.CALL_STATE_RINGING -> listener?.onRinging()
                //手机状态：接电话状态
                TelephonyManager.CALL_STATE_OFFHOOK -> listener?.onOffHook()
            }
        }
    }

    init {
        mActivity.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> register()
            Lifecycle.Event.ON_DESTROY -> {
                unregister()
                mActivity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

    private fun register() {
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

    @RequiresApi(api = Build.VERSION_CODES.S)
    private class MyCallStateListener : TelephonyCallback(), CallStateListener {
        override fun onCallStateChanged(state: Int) {
            onChanged(state)
        }
    }

    private class MyPhoneStateListener : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onCallStateChanged(state: Int, phoneNumber: String) {
            super.onCallStateChanged(state, phoneNumber)
            onChanged(state)
        }
    }

}