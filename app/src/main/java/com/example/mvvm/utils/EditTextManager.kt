package com.example.mvvm.utils

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.lifecycle.LifecycleOwner
import com.example.common.utils.function.color
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.common.widget.textview.edittext.ClearEditText
import com.example.common.widget.textview.edittext.PasswordEditText
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.textColor
import com.example.framework.utils.function.view.visible
import com.example.mvvm.R

/**
 * 页面生命周期/父类布局（最外层）
 * 针对界面中部分模拟web端选中橙色，报错红色，正常灰色书写的输入框管理类
 * 注意：
 * 1.页面输入框校验只适用于本地校验，本地会逐行检测，拿取到检测不通过的数据的view下标，准确的在其下方显示红色的报错信息
 * 2.如果是服务器检测，则是本地校验通过后提交的整体数据，只会告诉客户端整体报错信息，不会也不能告知对应view下标，手机端此时只能直接给出提示，不能准确在对应view下显示红色报错信息
 * 3.此类样式一旦使用，app内部大范围都得格式统一，加大了手机端本地逻辑判断的工作量，页面view绘制也增多了，不是很推荐使用此类做法
 * 举例：
 * 1.注册账号，注册密码，邀请码，城市信息4个输入框，手机端校验了长度，格式等数据，错误的情况下不做接口提交，错误信息准确的显示在对应view下方
 * 2.手机端4个输入框的数据通过了校验，提交给了服务器，服务器检测到提交的数据体中某个值错误，比如密码不正确，数据库里对不上，邀请码查不到，并没有生成，统一给了一个错误提示，并没有给出对应view的下标
 * 3.手机端此时只能把错误信息toast出来，不能准确在某个view下面展示红色报错信息
 */
class EditTextManager(observer: LifecycleOwner) {
    //first->所有输入框 second->所有异常原因
    private var list: ArrayList<Pair<View?, TextView?>>? = null
    //焦点监听
    private var onFocusChange: ((v: View?, hasFocus: Boolean?, index: Int?) -> Unit)? = null
    //正常/选中/报错
    private val colorRes by lazy { Triple(drawable(R.color.inputNormal), drawable(R.color.inputFocused), drawable(R.color.inputError)) }

    init {
        observer.doOnDestroy {
            list?.clear()
        }
    }

    /**
     * 外层输入框线条绘制
     */
    private fun drawable(@ColorRes res: Int): Drawable {
        return GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(1.pt, color(res))
            cornerRadius = 4.ptFloat
        }
    }

    /**
     * 绑定一整个页面所有的输入框/报错view
     */
    fun bind(vararg views: Pair<View?, TextView?>): EditTextManager {
        list = arrayListOf(*views)
        return this
    }

    /**
     * 构建
     */
    fun build(): EditTextManager {
        init()
        return this
    }

    private fun init() {
        list?.forEachIndexed { index, pair ->
            val view = pair.first
            //针对控件赋值
            view?.background = colorRes.first
            //如果对应输入框没有快捷输入
            when (view) {
                is ClearEditText -> view.editText
                is PasswordEditText -> view.editText
                else -> view as? EditText
            }?.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                normal()
                if (hasFocus) view?.background = colorRes.second
                onFocusChange?.invoke(v, hasFocus, index)
            }
            val textView = pair.second
            textView.textColor(R.color.textRed)
            textView.gone()
        }
    }

    /**
     * 常态
     */
    fun normal() {
        list?.forEach {
            //隐藏所有reason错误
            it.first?.gone()
            //所有选中都初始化
            it.second?.background = colorRes.first
        }
    }

    /**
     * 传入对应下标，告知失败原因
     */
    fun reason(index: Int, reason: String? = null) {
        list?.forEach {
            it.second.gone()
        }
        val pair = list.safeGet(index)
        val view = pair?.first
        view?.background = colorRes.third
        val textView = pair?.second
        textView.visible()
        textView?.text = reason.orEmpty()
    }

    /**
     * 整体套用的校验需要重写焦点监听，一旦重写，页面上需要用到的地方会出问题（不响应）故而提出一个整体焦点监听，统一管理
     */
    fun setOnFocusChangeListener(onFocusChange: ((v: View?, hasFocus: Boolean?, index: Int?) -> Unit)): EditTextManager {
        this.onFocusChange = onFocusChange
        return this
    }

}