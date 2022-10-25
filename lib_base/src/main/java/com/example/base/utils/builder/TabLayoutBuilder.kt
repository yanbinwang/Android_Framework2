package com.example.base.utils.builder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.SparseArray
import android.view.View
import android.widget.Checkable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.base.utils.function.inflate
import com.example.base.utils.function.value.safeGet
import com.example.base.utils.function.view.adapter
import com.example.base.utils.function.view.bind
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author yan
 * 项目实际使用中，ui是肯定不会按照安卓原生的导航栏来实现对应的效果的
 * 故而提出一个接口类，需要实现对应效果的地方去实现
 */
abstract class TabLayoutBuilder<T>(private val tab: TabLayout, private var tabList: List<T>? = null) {
    private var builder: FrameLayoutBuilder? = null
    private var mediator: TabLayoutMediator? = null
    protected val context: Context get() = tab.context
    val currentIndex get() = tab.selectedTabPosition

    /**
     * 注入管理器
     */
    fun bind(builder: FrameLayoutBuilder, list: List<T>? = null) {
        this.builder = builder
        init(list)
        addOnTabSelectedListener()
    }

    /**
     * 注入viewpager2
     */
    fun bind(pager: ViewPager2, adapter: RecyclerView.Adapter<*>, isUserInput: Boolean = false, list: List<T>? = null) {
        pager.adapter = null
        mediator?.detach()
        init(list)
        pager.adapter(adapter, ViewPager2.ORIENTATION_HORIZONTAL, isUserInput)
        mediator = pager.bind(tab)
        addOnTabSelectedListener()
    }

    private fun init(list: List<T>? = null) {
        tab.removeAllTabs()
        if (null != list) tabList = list
        tabList?.forEach { _ -> tab.addTab(tab.newTab()) }
    }

    /**
     * 这个方法需要放在setupWithViewPager()后面
     */
    private fun addOnTabSelectedListener() {
        for (i in 0 until tab.tabCount) {
            tab.getTabAt(i)?.apply {
                context.inflate(getLayoutRes()).apply {
                    customView = this
                    view.isLongClickable = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) view.tooltipText = null
                    onBindView(TabViewHolder(this), tabList.safeGet(i), i == 0)
                }
            }
        }
        tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                onTabBind(tab, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                onTabBind(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            private fun onTabBind(tab: TabLayout.Tab?, selected: Boolean) {
                val tabView = tab?.customView ?: return
                onBindView(TabViewHolder(tabView), tabList.safeGet(tab.position), selected)
                builder?.selectTab(tab.position)
            }
        })
    }

    /**
     * 回调方法，返回对应控件
     */
    protected abstract fun getLayoutRes(): Int

    /**
     * 设置数据
     */
    protected abstract fun onBindView(holder: TabViewHolder, item: T?, selected: Boolean)

}

class TabViewHolder(private val itemView: View) {
    private val mViews by lazy { SparseArray<View>() }

    fun <V : View?> getView(viewId: Int): V {
        var view = mViews[viewId]
        if (view == null) {
            view = itemView.findViewById(viewId)
            mViews.put(viewId, view)
        }
        return view as V
    }

    fun setOnItemViewClickListener(listener: View.OnClickListener?): TabViewHolder {
        itemView.setOnClickListener(listener)
        return this
    }

    fun setOnItemViewLongClickListener(listener: View.OnLongClickListener?): TabViewHolder {
        itemView.setOnLongClickListener(listener)
        return this
    }

    fun setText(viewId: Int, value: String): TabViewHolder {
        val view = getView<TextView>(viewId)
        view.text = value
        return this
    }

    fun setTextColor(viewId: Int, color: Int): TabViewHolder {
        val view = getView<TextView>(viewId)
        view.setTextColor(color)
        return this
    }

    fun setTextColorRes(viewId: Int, colorRes: Int): TabViewHolder {
        val view = getView<TextView>(viewId)
        view.setTextColor(ContextCompat.getColor(itemView.context, colorRes))
        return this
    }

    fun setImageResource(viewId: Int, imgResId: Int): TabViewHolder {
        val view = getView<ImageView>(viewId)
        view.setImageResource(imgResId)
        return this
    }

    fun setBackgroundColor(viewId: Int, color: Int): TabViewHolder {
        val view = getView<View>(viewId)
        view.setBackgroundColor(color)
        return this
    }

    fun setBackgroundColorRes(viewId: Int, colorRes: Int): TabViewHolder {
        val view = getView<View>(viewId)
        view.setBackgroundResource(colorRes)
        return this
    }

    fun setImageDrawable(viewId: Int, drawable: Drawable?): TabViewHolder {
        val view = getView<ImageView>(viewId)
        view.setImageDrawable(drawable)
        return this
    }

    fun setImageDrawableRes(viewId: Int, drawableRes: Int): TabViewHolder {
        val drawable = ContextCompat.getDrawable(itemView.context, drawableRes)
        return setImageDrawable(viewId, drawable)
    }

    fun setImageBitmap(viewId: Int, imgBitmap: Bitmap): TabViewHolder {
        val view = getView<ImageView>(viewId)
        view.setImageBitmap(imgBitmap)
        return this
    }

    fun setVisible(viewId: Int, visible: Boolean): TabViewHolder {
        val view = getView<View>(viewId)
        view.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }

    fun setVisible(viewId: Int, visible: Int): TabViewHolder {
        val view = getView<View>(viewId)
        view.visibility = visible
        return this
    }

    fun setTag(viewId: Int, tag: Any?): TabViewHolder {
        val view = getView<View>(viewId)
        view.tag = tag
        return this
    }

    fun setTag(viewId: Int, key: Int, tag: Any?): TabViewHolder {
        val view = getView<View>(viewId)
        view.setTag(key, tag)
        return this
    }

    fun setChecked(viewId: Int, checked: Boolean): TabViewHolder {
        val view: Checkable = getView(viewId)
        view.isChecked = checked
        return this
    }

    fun setAlpha(viewId: Int, value: Float): TabViewHolder {
        getView<View>(viewId).alpha = value
        return this
    }

    fun setTypeface(viewId: Int, typeface: Typeface): TabViewHolder {
        val view = getView<TextView>(viewId)
        view.typeface = typeface
        view.paintFlags = view.paintFlags or Paint.SUBPIXEL_TEXT_FLAG
        return this
    }

    fun setTypeface(typeface: Typeface, vararg viewIds: Int): TabViewHolder {
        for (viewId in viewIds) {
            val view = getView<TextView>(viewId)
            view.typeface = typeface
            view.paintFlags = view.paintFlags or Paint.SUBPIXEL_TEXT_FLAG
        }
        return this
    }

    fun setOnClickListener(viewId: Int, listener: View.OnClickListener): TabViewHolder {
        val view = getView<View>(viewId)
        view.setOnClickListener(listener)
        return this
    }

}