package com.example.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.example.common.R
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.view.alpha
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.byHardwareAccelerate
import com.example.framework.utils.function.view.cancelAnim
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.dimen
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.layoutGravity
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.move
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.visible

/**
 * 仿ios开关
 * wangyanbin
 */
class SwitchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, ) : FrameLayout(context, attrs, defStyleAttr) {
    private val viewBg: View
    private val viewBgSelected: View
    private val viewCircle: View
    private var animTime = 200L
    private var onCheckChangeListener = { _: Boolean, _: Boolean, _: Long -> }
    private var beforeCheckChangeListener: ((nowChecked: Boolean, willChecked: Boolean) -> Boolean)? =
        null
    var isChecked = false
        private set

    init {
        val layout = FrameLayout(context)
        addView(layout)
        //插入后设置位置
        layout.layoutGravity = Gravity.CENTER
        //按钮背景
        viewBg = View(context)
        layout.addView(viewBg)
        viewBg.background("#E5E4E4", 11.ptFloat)
        //选中背景
        viewBgSelected = View(context)
        layout.addView(viewBgSelected)
        viewBgSelected.background("#5ebe77", 11.ptFloat)
        viewBgSelected.gone()
        //圆球
        viewCircle = View(context)
        layout.addView(viewCircle)
        viewCircle.background("#ffffff", 9.ptFloat)
        //布局属性绘制
        if (isInEditMode) {
            val pt1 = dimen(R.dimen.textSize20) / 20
            layout.size((38 * pt1).toInt(), (21 * pt1).toInt())
            viewBg.size((38 * pt1).toInt(), (21 * pt1).toInt())
            viewBgSelected.size((38 * pt1).toInt(), (21 * pt1).toInt())
            viewCircle.size((17 * pt1).toInt(), (17 * pt1).toInt())
            viewCircle.margin(start = (2 * pt1).toInt(), end = (2 * pt1).toInt())
        } else {
            layout.size(38.pt, 21.pt)
            viewBg.size(38.pt, 21.pt)
            viewBgSelected.size(38.pt, 21.pt)
            viewCircle.size(17.pt, 17.pt)
            viewCircle.margin(start = 2.pt, end = 2.pt)
        }
        //        context.inflate(R.layout.view_deal_check, this).apply {
        //            viewCircle = findViewById(R.id.viewCircle)
        //            viewBg = findViewById(R.id.viewBg)
        //            viewBgSelected = findViewById(R.id.viewBgSelected)
        //        }
        viewCircle.layoutGravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        //点击设置
        click(animTime) {
            if (beforeCheckChangeListener?.invoke(isChecked, !isChecked) == true) return@click
            checkChange(!isChecked, needAnim = true, callListener = true)
        }
        //加速渲染
        byHardwareAccelerate()
        viewCircle.byHardwareAccelerate()
        viewBgSelected.byHardwareAccelerate()
    }

    /**
     * 设置是否选中
     */
    fun setChecked(isChecked: Boolean, needAnim: Boolean, callListener: Boolean = true) {
        checkChange(isChecked, needAnim, callListener)
    }

    /**
     * 判断是否选中
     */
    private fun checkChange(checked: Boolean, needAnim: Boolean, callListener: Boolean) {
        if (isChecked == checked) return
        isChecked = checked
        viewCircle.cancelAnim()
        viewBgSelected.cancelAnim()
        viewBg.cancelAnim()
        when (checked) {
            true -> {
                viewCircle.layoutGravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                if (callListener) onCheckChangeListener(checked, needAnim, animTime)
                if (needAnim) {
                    viewCircle.move(-1f, 0f, 0f, 0f, animTime, false)
                    viewBgSelected.alpha(0f, 1f, animTime)
                    viewBg.alpha(1f, 0f, animTime)
                } else {
                    viewBgSelected.visible()
                    viewBg.gone()
                }
            }
            false -> {
                viewCircle.layoutGravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                if (callListener) onCheckChangeListener(checked, needAnim, animTime)
                if (needAnim) {
                    viewBg.visible()
                    viewCircle.move(1f, 0f, 0f, 0f, animTime, false)
                    viewBgSelected.alpha(1f, 0f, animTime)
                    viewBg.alpha(0f, 1f, animTime)
                } else {
                    viewBgSelected.gone()
                    viewBg.visible()
                }
            }
        }
    }

    /**
     * 绑定某个view（某个view点击后，当前按钮处于点击状态）
     */
    fun bindClickView(view: View) {
        view.click(animTime) {
            this.performClick()
        }
    }

    /**
     * 设置监听回调
     */
    fun setOnCheckChangeListener(listener: (isChecked: Boolean, needAnim: Boolean, animTime: Long) -> Unit) {
        this.onCheckChangeListener = listener
    }

    /**
     * 在选择器发生改变时调用
     * @param nowChecked 目前的选中状态
     * @param willChecked 将要改变的选中状态
     * @return 是否拦截这次变化
     * */
    fun setBeforeCheckChangeListener(listener: ((nowChecked: Boolean, willChecked: Boolean) -> Boolean)?) {
        this.beforeCheckChangeListener = listener
    }

}