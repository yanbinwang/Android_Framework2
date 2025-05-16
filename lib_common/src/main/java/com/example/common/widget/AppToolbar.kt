package com.example.common.widget

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.common.R
import com.example.common.utils.function.getStatusBarHeight
import com.example.common.utils.function.pt
import com.example.common.utils.function.setTheme
import com.example.framework.utils.function.color
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.view.applyConstraints
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.bold
import com.example.framework.utils.function.view.center
import com.example.framework.utils.function.view.centerVertically
import com.example.framework.utils.function.view.clearBackground
import com.example.framework.utils.function.view.clearHighlightColor
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.endToEndOf
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.padding
import com.example.framework.utils.function.view.setResource
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.startToEndOf
import com.example.framework.utils.function.view.startToStartOf
import com.example.framework.utils.function.view.textSize
import com.example.framework.utils.function.view.tint
import com.example.framework.utils.function.view.topToTopOf
import com.example.framework.widget.BaseViewGroup
import java.util.concurrent.ConcurrentHashMap

/**
 * 仿系统Toolbar自定义头
 */
class AppToolbar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr) {
    private var mActivity: Activity? = null
    val rootView by lazy { ConstraintLayout(context) }
    val idsMap by lazy { ConcurrentHashMap<String, Int>() }

    companion object {
        // 标题
        const val TITLE_TEXT = "title_text"      // 标题文本
        const val TITLE_SHADOW = "title_shadow"  // 标题阴影线
        // 左侧按钮
        const val LEFT_ICON = "left_icon"        // 左侧图标按钮
        const val LEFT_TEXT = "left_text"        // 左侧文本按钮
        const val LEFT_CUSTOM_VIEW = "left_custom_view" // 左侧自定义视图（任意 View 类型）
        // 右侧按钮
        const val RIGHT_ICON = "right_icon"      // 右侧图标按钮
        const val RIGHT_TEXT = "right_text"      // 右侧文本按钮
        const val RIGHT_CUSTOM_VIEW = "right_custom_view" // 右侧自定义视图（任意 View 类型）
    }

    init {
        rootView.size(MATCH_PARENT, WRAP_CONTENT)
        rootView.padding(top = getStatusBarHeight())
    }

    override fun onInflate() {
        if (isInflate) addView(rootView)
    }

    /**
     * 建立页面视图绑定关系
     */
    fun bind(activity: AppCompatActivity) {
        mActivity = activity
        activity.doOnDestroy {
            idsMap.clear()
        }
    }

    /**
     * 默认二级页面标题配置
     * title->标题
     * titleColor->标题颜色
     * bgColor->背景颜色
     * hasShade->标题底部是否带阴影
     */
    fun setTitle(title: String = "", titleColor: Int = R.color.textPrimary, bgColor: Int = R.color.bgToolbar, hasShade: Boolean = false): AppToolbar {
        rootView.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else context.color(bgColor))
        if (title.isNotBlank()) {
            handleView<TextView>(TITLE_TEXT, {
                TextView(context).also {
                    it.setTheme(title, titleColor)
                    it.textSize(R.dimen.textSize18)
                    it.bold(true)
                    it.size(WRAP_CONTENT, 44.pt)
                    it.gravity = Gravity.CENTER
                    it.filters = arrayOf(InputFilter.LengthFilter(10))
                    it.ellipsize = TextUtils.TruncateAt.END
                }
            }, {
                center(it)
            })
        }
        if (hasShade) {
            handleView<View>(TITLE_SHADOW, {
                View(context).also {
                    it.background(R.color.bgLine)
                    it.size(MATCH_PARENT, 1.pt)
                }
            }, {
                startToEndOf(it)
                endToEndOf(it)
                topToTopOf(it)
            }).margin(top = 44.pt)
        }
        setLeft()
        return this
    }

    /**
     * 页面不需要标题，只需要定制的返回按钮及特定背景
     */
    fun setTitleSecondary(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity?.finish() }, bgColor: Int = R.color.bgToolbar): AppToolbar {
        rootView.setBackgroundColor(if (0 == bgColor) Color.TRANSPARENT else context.color(bgColor))
        setLeft(resId, tintColor, onClick = onClick)
        return this
    }

    /**
     * 1.继承BaseActivity，在xml中include对应标题布局
     * 2.把布局bind传入工具类，实现绑定后，调取对应方法（private val titleBuilder by lazy { TitleBuilder(this, mBinding?.titleRoot) }）
     */
    fun setTransparent(title: String = "", titleColor: Int = R.color.textPrimary): AppToolbar {
        return setTitle(title, titleColor, 0)
    }

    fun setTransparentSecondary(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity?.finish() }): AppToolbar {
        return setTitleSecondary(resId, tintColor, onClick, 0)
    }

    /**
     * 设置左/右侧按钮图片资源
     * resId->图片
     * tintColor->图片覆盖色（存在相同图片颜色不同的情况，直接传覆盖色即可）
     * onClick->点击事件
     */
    fun setLeft(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = { mActivity?.finish() }): AppToolbar {
        createImageView(LEFT_ICON, resId, tintColor, onClick) {
            startToStartOf(it)
            centerVertically(it)
        }
        return this
    }

    /**
     * 设置左/右侧文字
     * label->文案
     * labelColor->文案颜色
     * drawable->是否包含图片（默认就是左侧的）
     *  1.drawable(res, width, height)获取图片，设置宽高
     *  3.view.setCompoundDrawables(startDrawable, topDrawable, endDrawable, bottomDrawable)调取绘制
     *  4.drawablePadding?.let { view.compoundDrawablePadding = it }文字间距
     * onClick->点击事件
     */
    fun setLeft(label: String, labelColor: Int = R.color.textPrimary, drawable: Drawable? = null, onClick: () -> Unit = { mActivity?.finish() }): AppToolbar {
        createTextView(LEFT_TEXT, label, labelColor, drawable, onClick) {
            startToStartOf(it)
            centerVertically(it)
        }
        return this
    }

    /**
     * 1.创建视图的函数
     * 2.配置视图的回调
     * setLeft({ ImageView(mActivity) }) { img ->
     *     img.setImageResource(R.drawable.ic_back)
     *     img.setOnClickListener { mActivity.finish() }
     *     img.setPadding(10.pt, 10.pt, 10.pt, 10.pt)
     * }
     */
    inline fun <reified T : View> setLeft(crossinline creator: () -> T, rsp: (T) -> Unit = {}): AppToolbar {
        //margin属性是插入后才可以设置的
        handleView(LEFT_CUSTOM_VIEW, creator) {
            startToStartOf(it)
            centerVertically(it)
        }.also(rsp)
        return this
    }

    fun setRight(resId: Int = R.mipmap.ic_btn_back, tintColor: Int = 0, onClick: () -> Unit = {}): AppToolbar {
        createImageView(RIGHT_ICON, resId, tintColor, onClick) {
            endToEndOf(it)
            centerVertically(it)
        }
        return this
    }

    fun setRight(label: String, labelColor: Int = R.color.textPrimary, drawable: Drawable? = null, onClick: () -> Unit = {}): AppToolbar {
        createTextView(RIGHT_TEXT, label, labelColor, drawable, onClick) {
            endToEndOf(it)
            centerVertically(it)
        }
        return this
    }

    inline fun <reified T : View> setRight(crossinline creator: () -> T, rsp: (T) -> Unit = {}): AppToolbar {
        handleView(RIGHT_CUSTOM_VIEW, creator) {
            endToEndOf(it)
            centerVertically(it)
        }.also(rsp)
        return this
    }

    /**
     * 创建左右侧按钮方法
     */
    private fun createImageView(key: String, resId: Int, tintColor: Int, onClick: () -> Unit, block: ConstraintSet.(Int) -> Unit) {
        handleView<ImageView>(key, {
            ImageView(context).also {
                it.setResource(resId)
                if (tintColor != 0) it.tint(tintColor)
                it.size(44.pt, 44.pt)
                it.padding(10.pt, 10.pt, 10.pt, 10.pt)
                it.click {
                    onClick.invoke()
                }
            }
        }, block)
    }

    private fun createTextView(key: String, label: String, labelColor: Int, drawable: Drawable? = null, onClick: () -> Unit, block: ConstraintSet.(Int) -> Unit) {
        handleView<TextView>(key, {
            TextView(context).also {
                it.setTheme(label, labelColor)
                it.padding(start = 15.pt, end = 15.pt)
                it.textSize(R.dimen.textSize14)
                it.gravity = Gravity.CENTER
                if (drawable != null) {
                    it.clearBackground()
                    it.clearHighlightColor()
                    drawable.setTint(context.color(labelColor))
                    it.setCompoundDrawables(drawable, null, null, null)
                    it.compoundDrawablePadding = 2.pt
                }
                it.click {
                    onClick.invoke()
                }
            }
        }, block)
    }

    /**
     * 外层创建view
     */
    inline fun <reified T : View> handleView(key: String, crossinline creator: () -> T, noinline block: ConstraintSet.(Int) -> Unit = {}): T {
        // 移除上一次的视图
        val lastId = idsMap[key]
        if (lastId != null && lastId != View.NO_ID) {
            rootView.findViewById<T>(lastId)?.let {
                rootView.removeView(it)
            }
        }
        // 生成新的唯一 id
        val newViewId = View.generateViewId()
        idsMap[key] = newViewId
        val newView = creator.invoke()
        newView.id = newViewId
        rootView.addView(newView)
        rootView.applyConstraints {
            block(newViewId)
        }
        return newView
    }

    /**
     * 获取某个特定的view
     */
    inline fun <reified T : View> safeGet(key: String): T? {
        val id = idsMap[key]
        return if (id != null && id != View.NO_ID) {
            rootView.findViewById(id)
        } else {
            null
        }
    }

    /**
     * 检测是否创建
     */
    fun nonNull(vararg keys: String): Boolean {
        return keys.all { idsMap[it] != null }
    }

}