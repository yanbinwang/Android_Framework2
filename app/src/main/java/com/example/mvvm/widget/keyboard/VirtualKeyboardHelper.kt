package com.example.mvvm.widget.keyboard

import android.view.animation.AnimationUtils
import android.widget.EditText
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.fade
import com.example.framework.utils.function.view.isVisible
import com.example.framework.utils.function.view.visible
import com.example.mvvm.R

/**
 * @description 虚拟键盘帮助类
 * @author yan
 * //建立对应的绑定关系，让edittext不再弹出系统的输入框
 * activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
 *
 * <FrameLayout
 * android:layout_width="48pt"
 * android:layout_height="54pt"
 * android:background="@drawable/bg_verification">
 *
 * <EditText
 * android:id="@+id/etPassword1"
 * android:layout_width="match_parent"
 * android:layout_height="match_parent"
 * android:background="@null"
 * android:enabled="false"
 * android:gravity="center"
 * android:inputType="number"
 * android:lines="1"
 * android:maxLength="1"
 * android:singleLine="true"
 * android:text=""
 * android:textColor="@color/textBlack"
 * android:textSize="@dimen/textSize24"
 * android:textStyle="bold" />
 *
 * </FrameLayout>
 */
class VirtualKeyboardHelper(private val keyboard: VirtualKeyboard) {
    private var currentIndex = -1 //用于记录当前输入密码格位置
    private val animatorShow by lazy { AnimationUtils.loadAnimation(keyboard.context, R.anim.set_translate_bottom_in) }
    private var etList: List<EditText>? = null
    var onConvert: ((convert: Boolean) -> Unit)? = null

    /**
     * 传入一组输出框
     * 禁止其弹出输入法
     */
    fun bind(etList: List<EditText>) {
        this.etList = etList
        try {
            val setShowSoftInputOnFocus = EditText::class.java.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
            setShowSoftInputOnFocus.isAccessible = true
            etList.forEach { setShowSoftInputOnFocus.invoke(it, false) }
        } catch (_: Exception) {
        }
        keyboard.getAdapter()?.onItemClick = { _: Map<String, String>?, position: Int ->
            //点击0~9按钮
            if (position < 11 && position != 9) {
                //判断输入位置————要小心数组越界
                if (currentIndex >= -1 && currentIndex < 5) {
                    ++currentIndex
                    etList.safeGet(currentIndex)?.setText(keyboard.getValueList()[position]["name"])
                    if (getPassword().length == 6) onConvert?.invoke(true) else onConvert?.invoke(false)
                }
            } else {
                //点击退格键
                if (position == 11) {
                    //判断是否删除完毕————要小心数组越界
                    if (currentIndex - 1 >= -1) {
                        etList.safeGet(currentIndex)?.setText("")
                        currentIndex--
                    }
                }
            }
        }
    }

    fun getPassword(): String {
        val password = StringBuilder()
        for (i in 0..5) {
            password.append(etList.safeGet(i)?.text.toString().trim { it <= ' ' })
        }
        return password.toString().trim()
    }

    fun show() {
        if (!keyboard.isVisible()) {
            keyboard.visible()
            keyboard.startAnimation(animatorShow)
        }
    }

    fun hidden() {
        if (keyboard.isVisible()) {
            keyboard.fade(300)
        }
    }

}