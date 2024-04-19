package com.example.mvvm.utils.finger

import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.builder.shortToast
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.logWTF


/**
 * 指纹帮助类
 * 添加权限
 * <uses-permission android:name="android.permission.USE_FINGERPRINT" />
 *
 * 用途
 * 纯本地使用。即用户在本地完成指纹识别后，不需要将指纹的相关信息给后台
 * 与后台交互。用户在本地完成指纹识别后，需要将指纹相关的信息传给后台
 *
 * 加密参考
 * https://blog.csdn.net/qq36246172/article/details/119002182
 * https://blog.csdn.net/birthmarkqiqi/article/details/79321324
 */
@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
@RequiresApi(Build.VERSION_CODES.M)
class FingerHelper(private val mActivity: FragmentActivity, private val isGenerator: Boolean = false) : LifecycleEventObserver {
    private val generator by lazy { FingerGenerator() }
    private var fingerprintManager: FingerprintManager? = null
    private var cancellationSignal: CancellationSignal? = null//用于取消指纹识别
    private var listener: OnFingerListener? = null

    companion object {
        /**
         * 指纹识别错误码，会关闭指纹传感器
         */
        const val ERROR_CLOSE = 101

        /**
         * 指纹识别错误码，不会关闭指纹传感器
         */
        const val ERROR_NOT_CLOSE = 102
    }

    init {
        mActivity.lifecycle.addObserver(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = mActivity.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager
            cancellationSignal = CancellationSignal()
        }
    }

    /**
     * 生命周期监听
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> stopAuthenticate()
            Lifecycle.Event.ON_DESTROY -> {
                stopAuthenticate()
                mActivity.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }

    /**
     * 开始认证
     */
    fun startAuthenticate(): Boolean {
        return if (isFingerAvailable()) {
            /**
             * 第一个参数是一个加密对象。还记得之前我们大费周章地创建和初始化的Cipher对象吗？这里的 CryptoObject 对象就是使用 Cipher 对象创建创建出来的：new FingerprintManager.CryptoObject(cipher)。
             * 第二个参数是一个 CancellationSignal 对象，该对象提供了取消操作的能力。创建该对象也很简单，使用 new CancellationSignal() 就可以了。
             * 第三个参数是一个标志，默认为0。
             * 第四个参数是 AuthenticationCallback 对象，它本身是 FingerprintManager 类里面的一个抽象类。该类提供了指纹识别的几个回调方法，包括指纹识别成功、失败等。需要我们重写。
             * 最后一个 Handler，可以用于处理回调事件，可以传null。
             */
            fingerprintManager?.authenticate(generator.getCryptoObject(isGenerator), cancellationSignal, 0, object : FingerprintManager.AuthenticationCallback() {
                /**
                 * 指纹识别成功，会关闭指纹传感器。
                 * @param result
                 */
                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    listener?.onAuthenticated(result)
                }

                /**
                 * 指纹识别失败，不会关闭指纹传感器，可再次识别。
                 */
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    listener?.onError(ERROR_NOT_CLOSE, "识别失败，再试一次")
                }

                /**
                 * 发生不可恢复的错误，会关闭指纹传感器。
                 * @param errorCode
                 * @param errString
                 */
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    listener?.onError(ERROR_CLOSE, errString.toString())
                }

                /**
                 * 发生可恢复的错误，不会关闭指纹传感器，比如手指移动太快。
                 * @param helpCode
                 * @param helpString
                 */
                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                    super.onAuthenticationHelp(helpCode, helpString)
                    listener?.onError(helpCode, helpString.toString())
                }
            }, null)
            true
        } else {
            false
        }
    }

    /**
     * 使用指纹之前的一系列检查
     * 应用是否添加指纹权限
     * 设备是否支持指纹功能
     * 设备是否开启锁屏密码
     * 设备是否录入指纹
     * @return 如果任一项检查失败，返回false
     */
    private fun isFingerAvailable(isToast: Boolean = true): Boolean {
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.USE_FINGERPRINT)) {
            if (isToast) "应用未添加指纹权限".shortToast()
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val keyguardManager = mActivity.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            if (!fingerprintManager?.isHardwareDetected().orFalse) {
                if (isToast) "设备不支持指纹功能".shortToast()
                return false
            }
            if (!fingerprintManager?.hasEnrolledFingerprints().orFalse) {
                if (isToast) "设备未录入指纹".shortToast()
                return false
            }
            if (!keyguardManager?.isKeyguardSecure.orFalse) {
                if (isToast) "设备未开启锁屏密码".shortToast()
                return false
            }
        } else {
            if (isToast) "设备不支持指纹功能".shortToast()
            return false
        }
        return true
    }

    /**
     * 关闭指纹识别
     */
    fun stopAuthenticate() {
        if (cancellationSignal != null) {
            cancellationSignal?.cancel()
            cancellationSignal = null
        }
    }

    /**
     * 获取手机中存储的指纹列表
     */
    fun getFingerprintInfo() {
        try {
            val method = FingerprintManager::class.java.getDeclaredMethod("getEnrolledFingerprints")
            val obj = method.invoke(fingerprintManager)
            if (obj != null) {
                val clazz = Class.forName("android.hardware.fingerprint.Fingerprint")
                val getFingerId = clazz.getDeclaredMethod("getFingerId")
                for (i in (obj as List<*>).indices) {
                    val item = obj[i] ?: continue
                    "fingerId: ${getFingerId.invoke(item)}".logWTF
                }
            }
        } catch (e: Exception) {
//            e.printStackTrace()
        }
    }

    /**
     * 设置监听
     */
    fun setOnFingerListener(listener: OnFingerListener) {
        this.listener = listener
    }

    /**
     * 指纹识别回调
     */
    interface OnFingerListener {
        /**
         * 识别成功回调
         * @param result 系统指纹类
         */
        fun onAuthenticated(result: FingerprintManager.AuthenticationResult?)

        /**
         * 识别失败回调
         * @param code 错误代码
         * @param message 错误信息
         */
        fun onError(code: Int, message: String?)
    }

}