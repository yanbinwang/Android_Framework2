package com.example.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.example.common.R
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.value.createRectangleDrawable
import com.example.framework.utils.function.value.toSafeInt
import com.example.framework.utils.function.view.alpha
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
 * yan
 */
class SwitchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private val viewBg: View
    private val viewBgSelected: View
    private val viewCircle: View
    private var animTime = 200L
    private var onCheckChangeListener: ((isChecked: Boolean, needAnim: Boolean, animTime: Long) -> Unit)? = null
    private var onBeforeCheckChangeListener: ((nowChecked: Boolean, willChecked: Boolean) -> Boolean)? = null
    var isChecked = false
        private set

    init {
        val layout = FrameLayout(context)
        addView(layout)
        // 插入后设置位置
        layout.layoutGravity = Gravity.CENTER
        // 按钮背景
        viewBg = View(context)
        layout.addView(viewBg)
        viewBg.background = createRectangleDrawable("#E5E4E4", 11.ptFloat)
        // 选中背景
        viewBgSelected = View(context)
        layout.addView(viewBgSelected)
        viewBgSelected.background = createRectangleDrawable("#5ebe77", 11.ptFloat)
        viewBgSelected.gone()
        // 圆球
        viewCircle = View(context)
        layout.addView(viewCircle)
        viewCircle.background = createRectangleDrawable("#ffffff", 9.ptFloat)
        // 布局属性绘制
        if (isInEditMode) {
            val ratio = dimen(R.dimen.textSize20) / 20
            size(layout, ratio)
        } else {
            size(layout)
        }
        //        context.inflate(R.layout.view_deal_check, this).apply {
        //            viewCircle = findViewById(R.id.viewCircle)
        //            viewBg = findViewById(R.id.viewBg)
        //            viewBgSelected = findViewById(R.id.viewBgSelected)
        //        }
        viewCircle.layoutGravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        // 点击设置
        click(animTime) {
            if (onBeforeCheckChangeListener?.invoke(isChecked, !isChecked) == true) return@click
            checkChange(!isChecked, needAnim = true, callListener = true)
        }
        // 加速渲染
        byHardwareAccelerate()
        viewCircle.byHardwareAccelerate()
        viewBgSelected.byHardwareAccelerate()
    }

    /**
     * 内部控件设置固定尺寸和间距 (给开关定死大小，预览时按比例缩放，运行时按原始大小显示)
     * @layout 开关的外层容器（FrameLayout），是背景 / 圆球等子控件的父容器
     * @ratio 缩放比例（默认值 1f），用于在「布局编辑器预览」时适配不同的显示比例
     * 76*ratio：开关整体的宽度（设计稿上的基准宽度）；
     * 42*ratio：开关整体的高度（设计稿上的基准高度）；
     * 34*ratio：圆球的直径（要小于开关高度，避免超出）；
     * 4*ratio：圆球的左右边距（让圆球不会贴紧开关边缘）
     */
    private fun size(layout: FrameLayout, ratio: Float = 1f) {
        // 设置外层容器的尺寸：宽76*比例，高42*比例（单位是像素，基于设计稿的尺寸）
        layout.size((76 * ratio).toSafeInt(), (42 * ratio).toSafeInt())
        // 设置开关背景的尺寸（和外层容器一样大，铺满）
        viewBg.size((76 * ratio).toSafeInt(), (42 * ratio).toSafeInt())
        // 设置选中状态背景的尺寸（和外层容器一样大，铺满）
        viewBgSelected.size((76 * ratio).toSafeInt(), (42 * ratio).toSafeInt())
        // 设置圆球的尺寸：宽34*比例，高34*比例（圆形按钮的大小）
        viewCircle.size((34 * ratio).toSafeInt(), (34 * ratio).toSafeInt())
        // 设置圆球的间距：左右边距各4*比例（让圆球和背景有间距）
        viewCircle.margin(start = (4 * ratio).toSafeInt(), end = (4 * ratio).toSafeInt())
    }

    /**
     * 设置是否选中
     */
    fun setChecked(isChecked: Boolean, needAnim: Boolean = false, callListener: Boolean = true) {
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
                if (callListener) {
                    onCheckChangeListener?.invoke(true, needAnim, animTime)
                }
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
                if (callListener) {
                    onCheckChangeListener?.invoke(false, needAnim, animTime)
                }
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
     * @isChecked 选中状态
     * @needAnim 是否需要动画
     * @animTime 动画时间
     */
    fun setOnCheckChangeListener(listener: (isChecked: Boolean, needAnim: Boolean, animTime: Long) -> Unit) {
        this.onCheckChangeListener = listener
    }

    /**
     * 在选择器发生改变时调用
     * @nowChecked 目前的选中状态
     * @willChecked 将要改变的选中状态
     * @return 是否拦截这次变化 (true:拦截)
     */
    fun setOnBeforeCheckChangeListener(listener: (nowChecked: Boolean, willChecked: Boolean) -> Boolean) {
        this.onBeforeCheckChangeListener = listener
    }

}