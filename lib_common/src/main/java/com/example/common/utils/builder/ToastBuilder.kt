package com.example.common.utils.builder

import android.os.Looper
import android.widget.Toast
import com.example.common.BaseApplication
import java.lang.ref.WeakReference

object ToastBuilder {
    /**
     * 传入引用string格式的toast
     * ToastBuilder.short(R.string.homeRecommendedQuestsReceiveSuccess) { resId, length ->
     * val toast = Toast.makeText(MyApplication.instance, null, length)
     * toast?.setGravity(Gravity.CENTER, 0, 0)
     * toast?.duration = length
     *
     * val view = BaseApplication.instance.inflate(R.layout.toast_home_quest_success)
     * view.imgIcon.setImageResource(R.mipmap.icon_home_quest_dialog_coupon)
     * view.txtTitle.setI18nRes(resId)
     * view.txtAmount.text = "$" + bean.rewardNum
     * toast?.view = view
     * toast
     * }
     */
    private var resToastBuilder: (resId: Int, length: Int) -> Toast = { resId, length ->
        val toast = Toast.makeText(BaseApplication.instance, null, length)
        toast?.setText(resId)
        toast
    }

    /**
     * 传入文字的toast
     */
    private var toastBuilder: (message: String, length: Int) -> Toast = { message, length ->
        val toast = Toast.makeText(BaseApplication.instance, null, length)
        toast?.setText(message)
        toast
    }

    /**
     * 弱引用toast
     */
    private var toast: WeakReference<Toast>? = null

    fun short(resId: Int, toastBuilder: ((resId: Int, length: Int) -> Toast) = resToastBuilder) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        if (resId == -1) return
        cancelToast()
        toastBuilder(resId, Toast.LENGTH_SHORT).apply {
            toast = WeakReference(this)
            show()
        }
    }

    fun short(message: String, toastBuilder: ((message: String, length: Int) -> Toast) = this.toastBuilder) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        if (message.isEmpty()) return
        cancelToast()
        toastBuilder(message, Toast.LENGTH_SHORT).apply {
            toast = WeakReference(this)
            show()
        }
    }

    fun long(resId: Int, toastBuilder: ((resId: Int, length: Int) -> Toast) = resToastBuilder) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        if (resId == -1) return
        cancelToast()
        toastBuilder(resId, Toast.LENGTH_LONG).apply {
            toast = WeakReference(this)
            show()
        }
    }

    fun long(message: String, toastBuilder: ((message: String, length: Int) -> Toast) = this.toastBuilder) {
        if (Looper.getMainLooper() != Looper.myLooper()) return
        cancelToast()
        toastBuilder(message, Toast.LENGTH_LONG).apply {
            toast = WeakReference(this)
            show()
        }
    }

    fun cancelToast() {
        toast?.get()?.cancel()
    }

    /**
     * application中初始化全局的toast
     * 部分手機定制導致顯示不全，樣式不統一，故而再重寫一次，統一樣式
     */
    fun setResToastBuilder(builder: (message: Int, length: Int) -> Toast) {
        resToastBuilder = builder
    }

    fun setStringToastBuilder(builder: (message: String, length: Int) -> Toast) {
        toastBuilder = builder
    }

}

fun Int?.shortToast() {
    this ?: return
    ToastBuilder.short(this)
}

fun String?.shortToast() {
    this ?: return
    ToastBuilder.short(this)
}