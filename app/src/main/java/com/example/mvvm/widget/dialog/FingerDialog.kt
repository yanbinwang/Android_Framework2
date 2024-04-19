package com.example.mvvm.widget.dialog

import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.example.common.base.BaseDialog
import com.example.common.utils.builder.shortToast
import com.example.common.utils.function.setArguments
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.click
import com.example.mvvm.R
import com.example.mvvm.databinding.ViewDialogFingerBinding
import com.example.mvvm.utils.finger.FingerHelper

/**
 * 指纹弹框
 */
@RequiresApi(Build.VERSION_CODES.M)
class FingerDialog(private val mActivity: FragmentActivity) : BaseDialog<ViewDialogFingerBinding>(mActivity) {
    private val helper by lazy { FingerHelper(mActivity) }
    private var listener: ((result: FingerprintManager.AuthenticationResult?) -> Unit)? = null

    init {
        helper.setOnFingerListener(object : FingerHelper.OnFingerListener {
            override fun onAuthenticated(result: FingerprintManager.AuthenticationResult?) {
                "指纹识别成功".shortToast()
                listener?.invoke(result)
                dismiss()
            }

            override fun onError(code: Int, message: String?) {
                if(code == FingerHelper.ERROR_CLOSE) {
                    message.shortToast()
                    helper.stopAuthenticate()
                    dismiss()
                } else {
                    mBinding?.ivFinger.background(R.drawable.ic_fingerprint_error)
                    mBinding?.tvHint.setArguments(message.orEmpty(), R.color.textRed)
                }
            }
        })
        mBinding?.btnCancel.click {
            helper.stopAuthenticate()
            dismiss()
        }
    }

    /**
     * 开启扫描
     */
    fun startAuthenticate() {
        helper.startAuthenticate()
    }

    /**
     * 设置监听
     */
    fun setOnFingerListener(listener: ((result: FingerprintManager.AuthenticationResult?) -> Unit)) {
        this.listener = listener
    }

}