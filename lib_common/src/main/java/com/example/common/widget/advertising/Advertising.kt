package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.OVAL
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.common.utils.function.color
import com.example.common.utils.function.pt
import com.example.common.utils.function.ptFloat
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.parseColor
import com.example.framework.utils.function.view.adapter
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.reduceSensitivity
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup
import java.util.Timer
import java.util.TimerTask

/**
 * Created by wangyanbin
 * 广告控件
 * 解决方案1
 * android:nestedScrollingEnabled="false"
 * viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
 * @Override
 * public void onPageSelected(int position) {
 * super.onPageSelected(position);
 * if (position == /*index of page with RecyclerView*/) {
 * nestedScrollView.setNestedScrollingEnabled(false);
 * } else {
 * nestedScrollView.setNestedScrollingEnabled(true);
 * }
 * }
 * });
 * 解决方案2
 * 配置 ScrollView 的传递模式
 * 可以通过修改 ScrollView 的传递模式来解决滑动卡顿问题。
 * 将传递模式设置为 "nonTouch"，
 * 以便 ScrollView 只在滚动到底部或顶部时才将滑动事件传递给父级视图。这将确保 ScrollView 不会与 ViewPager2 上的滑动事件产生冲突，从而提高应用程序的滑动性能。
 * <ScrollView
 * android:layout_width="match_parent"
 * android:layout_height="match_parent"
 * android:nestedScrollingEnabled="true"
 * app:layout_behavior="@string/appbar_scrolling_view_behavior"
 * android:overScrollMode="never"
 * android:scrollIndicators="none"
 * android:scrollbarStyle="outsideOverlay"
 * android:scrollbars="vertical">
 * 解决方案3
 * 禁用 NestedScrollView 的嵌套滚动特性
 * 如果您使用的是 NestedScrollView，则可以通过将 android:nestedScrollingEnabled 属性设置为 "false" 来禁用其嵌套滚动特性。
 * 这将确保 NestedScrollView 不会与 ViewPager2 上的滑动事件产生冲突，从而提高应用程序的滑动性能。
 */
@SuppressLint("ClickableViewAccessibility")
class Advertising @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), AdvertisingImpl, LifecycleEventObserver {
    //图片路径数组
    private var list = ArrayList<String>()
    //是否允许滑动
    private var allowScroll = true
    //是否自动滚动
    private var autoScroll = true
    //3个资源路径->圆点选中时的背景ID second：圆点未选中时的背景ID third：圆点间距 （圆点容器可为空写0）
    private var triple = Triple(drawable("#3d81f2"), drawable("#6e7ce2"), 10)
    //自动滚动的定时器
    private var timer: Timer? = null
    //广告容器
    private var banner: ViewPager2? = null
    //圆点容器
    private var ovalLayout: LinearLayout? = null
    //设定一个中心值下标
    private val halfPosition by lazy { Int.MAX_VALUE / 2 }
    //图片适配器
    private val advAdapter by lazy { AdvertisingAdapter() }
    //切线程
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) }
    //回调方法
    private var onPagerClick: ((index: Int) -> Unit)? = null
    private var onPagerCurrent: ((index: Int) -> Unit)? = null

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    init {
        banner = ViewPager2(context).apply {
            reduceSensitivity()
            adapter(advAdapter, userInputEnabled = true)
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                private var curIndex = 0//当前选中的数组索引
                private var oldIndex = 0//上次选中的数组索引
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    //切换圆点
                    curIndex = position % list.size
                    if (null != ovalLayout) {
                        if (list.size > 1) {
                            ovalLayout?.getChildAt(oldIndex)?.background = triple.second
                            ovalLayout?.getChildAt(curIndex)?.background = triple.first
                            oldIndex = curIndex
                        }
                    }
                    //切换
                    onPagerCurrent?.invoke(curIndex)
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    allowScroll = positionOffsetPixels == 0
                }
            })
//            setOnTouchListener { _, event ->
//                when (event?.action) {
//                    MotionEvent.ACTION_UP -> if (autoScroll) startRoll()
//                    else -> if (autoScroll) stopRoll()
//                }
//                false
//            }
//            isNestedScrollingEnabled = false
        }
    }

    override fun onInflateView() {
        if (isInflate()) addView(banner)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRoll()
        (banner?.parent as? ViewGroup)?.removeAllViews()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> startRoll()
            Lifecycle.Event.ON_PAUSE -> stopRoll()
            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            else -> {}
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实现方法">
    override fun start(uriList: ArrayList<String>, radius: Int, localAsset: Boolean, ovalList: Triple<Drawable, Drawable, Int>?, ovalLayout: LinearLayout?) {
        this.list = uriList
        this.ovalLayout = ovalLayout
        this.advAdapter.setParams(radius, localAsset)
        if (ovalList != null) this.triple = ovalList
        //设置数据
        initData()
    }

    /**
     * 初始化圆点,图片数据
     */
    private fun initData() {
        //如果只有一第图时不显示圆点容器
        if (ovalLayout != null) {
            if (list.size < 2) {
                ovalLayout?.gone()
            } else {
                ovalLayout?.gravity = Gravity.CENTER
                ovalLayout?.visible()
                ovalLayout?.removeAllViews()
                ovalLayout?.doOnceAfterLayout {
                    //如果true代表垂直，否则水平
                    val direction = it.layoutParams?.height.orZero > it.layoutParams?.width.orZero
                    //左右边距
                    val ovalMargin = triple.third.pt
                    //添加圆点
                    for (i in list.indices) {
                        ImageView(context).apply {
                            if (direction) {
                                margin(start = ovalMargin, end = ovalMargin)
                                size(width = it.measuredHeight, height = it.measuredHeight)
                            } else {
                                margin(top = ovalMargin, bottom = ovalMargin)
                                size(width = it.measuredWidth, height = it.measuredWidth)
                            }
                            background = triple.second
                            it.addView(this)
                        }
                    }
                    //选中第一个
                    it.getChildAt(0)?.background = triple.first
                }
            }
        }
        //设置图片数据
        advAdapter.refresh(list)
        advAdapter.setOnItemClickListener { onPagerClick?.invoke(it) }
        //设置默认选中的起始位置
        banner?.setCurrentItem(if (list.size > 1) halfPosition - halfPosition % list.size else 0, false)
    }

    /**
     * 放在start之前
     */
    override fun setAutoScroll(scroll: Boolean) {
        this.autoScroll = scroll
        stopRoll()
    }

    override fun setOrientation(orientation: Int) {
        banner?.orientation = orientation
    }

    override fun setPageTransformer(marginPx: Int) {
        banner?.setPageTransformer(MarginPageTransformer(marginPx.pt))
    }

    /**
     * 默认图片资源
     */
    private fun drawable(colorString:String): Drawable {
        return GradientDrawable().apply {
            shape = OVAL
            setColor(colorString.parseColor())
        }
    }

    /**
     * 开始自动滚动任务 图片大于1张才滚动
     */
    private fun startRoll() {
        if (autoScroll) {
            if (list.size > 1) {
                if (timer == null) {
                    timer = Timer()
                    timer?.schedule(object : TimerTask() {
                        override fun run() {
                            if (allowScroll) {
                                weakHandler.post {
                                    val current = banner?.currentItem.orZero
                                    var position = current + 1
                                    if (current == 0 || current == Int.MAX_VALUE) position = halfPosition - halfPosition % list.size
                                    banner?.currentItem = position
                                }
                            }
                        }
                    }, 0, 3000)
                }
            }
        }
    }

    /**
     * 停止自动滚动任务
     */
    private fun stopRoll() {
        if (autoScroll) {
            if (timer != null) {
                timer?.cancel()
                timer = null
            }
        }
    }

    /**
     * 绑定对应页面的生命周期-》对应回调重写对应方法
     * @param observer
     */
    fun addObserver(observer: LifecycleOwner) {
        observer.lifecycle.addObserver(this)
    }

    /**
     * 设置广告监听
     */
    fun setAdvertisingListener(onPagerClick: (index: Int) -> Unit, onPagerCurrent: (index: Int) -> Unit) {
        this.onPagerClick = onPagerClick
        this.onPagerCurrent = onPagerCurrent
    }
    // </editor-fold>

}