package com.example.common.widget.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.example.common.R
import com.example.common.utils.function.orNoData
import com.example.common.utils.function.string
import com.example.framework.utils.function.value.orFalse

/**
 * author: wyb
 * date: 2017/8/25.
 * 安卓原生提示框
 */
class AndDialog(context: Context) : AlertDialog.Builder(context, R.style.AndDialogStyle) {
    private var dialog: AlertDialog? = null
    private var onPositive: (() -> Unit)? = null
    private var onNegative: (() -> Unit)? = null

    fun setPositive(title: String? = string(R.string.hint), message: String? = null, positiveText: String? = string(R.string.sure), hasTitle: Boolean = true): AndDialog {
        return setParams(title, message, positiveText, "", hasTitle)
    }

    fun setParams(title: String? = string(R.string.hint), message: String? = null, positiveText: String? = string(R.string.sure), negativeText: String? = string(R.string.cancel), hasTitle: Boolean = true): AndDialog {
        if (hasTitle) setTitle(title.orNoData())
        setMessage(message.orNoData())
        setPositiveButton(positiveText.orNoData()) { _: DialogInterface?, _: Int ->
            onPositive?.invoke()
        }
        if (!negativeText.isNullOrEmpty()) {
            setNegativeButton(negativeText) { _: DialogInterface?, _: Int ->
                onNegative?.invoke()
            }
        }
        return this
    }

    fun setDialogListener(onPositive: () -> Unit = {}, onNegative: () -> Unit = {}): AndDialog {
        this.onPositive = onPositive
        this.onNegative = onNegative
        return this
    }

    override fun show(): AlertDialog {
        if (dialog == null) {
            dialog = super.show()
        } else if (!dialog?.isShowing.orFalse) {
            dialog?.show()
        }
        return dialog ?: super.show()
    }

    override fun create(): AlertDialog {
        if (dialog == null) {
            dialog = super.create()
            setType() // 如果是构建模式,此时是可以直接添加窗口后台显示的类型的
        }
        return dialog ?: super.create()
    }

    /**
     * 必须在 show()/create() 之后调用才有效
     * WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY：
     * 从 Android 8.0（API 级别 26）开始引入，用于在其他应用之上显示窗口。使用该类型需要在 AndroidManifest.xml 里声明 SYSTEM_ALERT_WINDOW 权限，并且用户需要在系统设置里手动授予此权限(<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>)
     * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&!Settings.canDrawOverlays(context)) {
     *     val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
     *     context.startActivity(intent)
     * }
     * WindowManager.LayoutParams.TYPE_SYSTEM_ALERT：
     * 在 Android 8.0 之前使用，同样用于在其他应用之上显示窗口，也需要 SYSTEM_ALERT_WINDOW 权限。不过从 Android 8.0 开始，此类型已被弃用，建议使用 TYPE_APPLICATION_OVERLAY 替代。
     */
    fun setType() {
        this.dialog?.window?.setType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
    }

}