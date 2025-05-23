package com.example.mvvm.widget.customitem

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.parseColor
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.adapter
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.reduceSensitivity
import com.example.framework.utils.function.view.size

/**
 * 当前控件适用于顶部多项按钮，底部切换button的情况
 * private val data by lazy {
 *     listOf(
 *         listOf(
 *             "ic_launcher" to "标记1",
 *             "ic_launcher" to "标记2",
 *             "ic_launcher" to "标记3",
 *             "ic_launcher" to "标记4",
 *             "ic_launcher" to "标记5",
 *             "ic_launcher" to "标记6",
 *             "ic_launcher" to "标记7",
 *             "ic_launcher" to "标记8"
 *         ), listOf(
 *             "ic_launcher" to "标记9",
 *             "ic_launcher" to "标记10",
 *             "ic_launcher" to "标记11",
 *             "ic_launcher" to "标记12"
 *         )
 *     )
 * }
 */
class CustomItemLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {
    //内容data
    private var data: List<List<Triple<Boolean, String, String>>>? = null
    //回调方法
    private var onPagerClick: ((index: Int) -> Unit)? = null
    private var onPagerCurrent: ((index: Int) -> Unit)? = null
    //3个资源路径->圆点选中时的背景ID second：圆点未选中时的背景ID third：圆点间距 （圆点容器可为空写0）
    private var ovalList = Triple(drawable("#3d81f2"), drawable("#6e7ce2"), 10)
    //按钮适配器
    private val itemAdapter by lazy { CustomItemAdapter() }
    //按钮容器
    private val banner by lazy { ViewPager2(context) }
    //圆点容器
    private val ovalLayout by lazy { LinearLayout(context) }
    //注册广告监听
    private val callback by lazy { object : OnPageChangeCallback() {
        private var curIndex = 0//当前选中的数组索引
        private var oldIndex = 0//上次选中的数组索引

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            //切换圆点
            curIndex = position
            if (data.safeSize > 1) {
                ovalLayout.getChildAt(oldIndex)?.background = ovalList.second
                ovalLayout.getChildAt(curIndex)?.background = ovalList.first
                oldIndex = curIndex
            }
            //切换
            onPagerCurrent?.invoke(curIndex)
        }
    }}

    init {
        gravity = Gravity.CENTER
        orientation = VERTICAL
    }

    /**
     * 默认图片资源
     */
    private fun drawable(colorString: String, width: Int = 40, height: Int = 3): Drawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(4.ptFloat, 4.ptFloat, 4.ptFloat, 4.ptFloat)
            setColor(colorString.parseColor())
            setSize(width.pt, height.pt)
        }
    }

    /**
     * 传入需要加载的数据
     * 1）先将需要加载的按钮集合数据处理成view需要的list，通过toNewList扩展转换即可，再调取init方法配置页面
     * 2）用例参考
     * val originalList = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) //假设原集合已转为列表
     * val chunkedList = originalList.chunked(8) //按8个元素分块
     * 结果：[ [1,2,3,4,5,6,7,8], [9,10] ]
     */
    fun build(list: List<Triple<Boolean, String, String>>, ovalList: Triple<Drawable, Drawable, Int>, columns: Int = 4, tabCount: Int = 8) {
        this.data = list.chunked(tabCount)
        this.ovalList = ovalList
        this.itemAdapter.apply {
            setConfiguration(columns, tabCount)
            refresh(data)
            setOnItemClickListener {
                //返回的是传入的集合下标，而view外层持有服务器的集合，可以通过当前下标去获取外层服务器集合对应的值，然后处理
                onPagerClick?.invoke(it)
            }
        }
        initData()
    }

    /**
     * 初始化按钮,圆点数据
     */
    private fun initData() {
        //默认初始化操作
        removeAllViews()
        addView(banner)
        addView(ovalLayout)
        //顶部按钮
        banner.apply {
            reduceSensitivity()
            size(MATCH_PARENT)
            adapter(itemAdapter, pageLimit = true)
            registerOnPageChangeCallback(callback)
        }
        //圆点按钮
        ovalLayout.apply {
            removeAllViews()
            size(MATCH_PARENT)
            gravity = Gravity.CENTER
        }
        ovalLayout.doOnceAfterLayout {
            //如果true代表垂直，否则水平
            val direction = it.layoutParams?.height.orZero > it.layoutParams?.width.orZero
            //左右边距
            val ovalMargin = ovalList.third.pt
            //添加圆点
            for (i in 0 until data.safeSize) {
                ImageView(context).apply {
                    if (direction) {
                        margin(start = ovalMargin, end = ovalMargin)
                        size(width = it.measuredHeight, height = it.measuredHeight)
                    } else {
                        margin(top = ovalMargin, bottom = ovalMargin)
                        size(width = it.measuredWidth, height = it.measuredWidth)
                    }
                    background = ovalList.second
                    it.addView(this)
                }
            }
            //选中第一个
            it.getChildAt(0)?.background = ovalList.first
        }
    }

    /**
     * 添加生命周期管控
     */
    fun addObserver(observer: LifecycleOwner) {
        observer.doOnDestroy {
            banner.unregisterOnPageChangeCallback(callback)
        }
    }

    /**
     * 设置按钮监听
     * onPagerClick:每页内的tab的下标
     * onPagerCurrent：当前页数下标
     */
    fun setCustomListener(onPagerClick: (index: Int) -> Unit, onPagerCurrent: (index: Int) -> Unit) {
        this.onPagerClick = onPagerClick
        this.onPagerCurrent = onPagerCurrent
    }

}