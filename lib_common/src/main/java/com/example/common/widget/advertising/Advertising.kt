package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.OVAL
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.common.utils.function.pt
import com.example.framework.utils.WeakHandler
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.*
import com.example.framework.widget.BaseViewGroup
import java.util.*

/**
 * Created by wangyanbin
 * 广告控件
 */
@SuppressLint("ClickableViewAccessibility")
class Advertising @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), AdvertisingImpl, LifecycleEventObserver {
    private var list = ArrayList<String>()//图片路径数组
    private var allowScroll = true//是否允许滑动
    private var autoScroll = true//是否自动滚动
    private var timer: Timer? = null//自动滚动的定时器
    private var banner: ViewPager2? = null//广告容器
    private var ovalLayout: LinearLayout? = null//圆点容器
    private val halfPosition by lazy { Int.MAX_VALUE / 2 }  //设定一个中心值下标
    private val triple by lazy {
        Triple(GradientDrawable().apply {
            shape = OVAL
            setColor(Color.parseColor("#3d81f2"))
        }, GradientDrawable().apply {
            shape = OVAL
            setColor(Color.parseColor("#6e7ce2"))
        }, 10)
    }//3个资源路径->圆点选中时的背景ID second：圆点未选中时的背景ID third：圆点间距 （圆点容器可为空写0）
    private val advAdapter by lazy { AdvertisingAdapter() } //图片适配器
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) } //切线程
    var onPagerClick: ((index: Int) -> Unit)? = null
    var onPagerCurrent: ((index: Int) -> Unit)? = null

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    init {
        banner = ViewPager2(context).apply {
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
            setOnTouchListener { _, event ->
                when (event?.action) {
                    MotionEvent.ACTION_UP -> if (autoScroll) startRoll()
                    else -> if (autoScroll) stopRoll()
                }
                false
            }
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
    override fun start(uriList: ArrayList<String>, ovalLayout: LinearLayout?, localAsset: Boolean) {
        this.list = uriList
        this.ovalLayout = ovalLayout
        advAdapter.localAsset = localAsset
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
        advAdapter.list = list
        advAdapter.onItemClick = { onPagerClick?.invoke(it) }
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
     * @param lifecycleOwner
     */
    fun addLifecycleObserver(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }
    // </editor-fold>

}