package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.base.utils.WeakHandler
import com.example.base.utils.function.value.orZero
import com.example.base.widget.BaseViewGroup
import com.example.common.utils.dp
import java.util.*

/**
 * Created by wangyanbin
 * 广告控件
 */
@SuppressLint("ClickableViewAccessibility")
class Advertising @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseViewGroup(context, attrs, defStyleAttr), AdvertisingImpl, DefaultLifecycleObserver {
    private lateinit var list: ArrayList<String>//图片路径数组
    private lateinit var triple: Triple<Int, Int, Int>//3个资源路径
    private var allowScroll = true//是否允许滑动
    private var autoScroll = true//是否自动滚动
    private var timer: Timer? = null//自动滚动的定时器
    private var banner: ViewPager2? = null//广告容器
    private var ovalLayout: LinearLayout? = null//圆点容器
    private val halfPosition by lazy { Int.MAX_VALUE / 2 }  //设定一个中心值下标
    private val advAdapter by lazy { AdvertisingAdapter() } //图片适配器
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) } //切线程
    var onPageClick: ((index: Int) -> Unit)? = null
    var onPageCurrent: ((index: Int) -> Unit)? = null

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    init {
        banner = ViewPager2(context).apply {
            getChildAt(0)?.overScrollMode = OVER_SCROLL_NEVER
            adapter = advAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                private var curIndex = 0//当前选中的数组索引
                private var oldIndex = 0//上次选中的数组索引
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    //切换圆点
                    curIndex = position % list.size
                    if (ovalLayout != null && list.size > 1) {
                        //圆点取消
                        ovalLayout?.getChildAt(oldIndex)?.setBackgroundResource(triple.second)
                        //圆点选中
                        ovalLayout?.getChildAt(curIndex)?.setBackgroundResource(triple.first)
                        oldIndex = curIndex
                    }
                    //切换
                    onPageCurrent?.invoke(curIndex)
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

    override fun onDrawView() {
        if (onFinishView()) addView(banner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (autoScroll) startRoll()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        if (autoScroll) stopRoll()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRoll()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实现方法">
    override fun start(uriList: ArrayList<String>, ovalLayout: LinearLayout?, triple: Triple<Int, Int, Int>, localAsset: Boolean) {
        this.list = uriList
        this.ovalLayout = ovalLayout
        this.triple = triple
        advAdapter.localAsset = localAsset
        //设置数据
        initData()
    }

    /**
     * 初始化圆点,图片数据
     */
    private fun initData() {
        //如果只有一第图时不显示圆点容器
        if (ovalLayout != null && list.size < 2) {
            ovalLayout?.layoutParams?.height = 0
        } else if (ovalLayout != null) {
            ovalLayout?.gravity = Gravity.CENTER
            //如果true代表垂直，否则水平
            val direction = ovalLayout?.layoutParams?.height.orZero > ovalLayout?.layoutParams?.width.orZero
            //左右边距
            val ovalMargin = triple.third.dp
            //添加圆点
            for (i in list.indices) {
                val imageView = ImageView(context)
                ovalLayout?.addView(imageView)
                val layoutParams = imageView.layoutParams as LinearLayout.LayoutParams
                if (direction) {
                    layoutParams.setMargins(ovalMargin, 0, ovalMargin, 0)
                } else {
                    layoutParams.setMargins(0, ovalMargin, 0, ovalMargin)
                }
                imageView.layoutParams = layoutParams
                imageView.setBackgroundResource(triple.second)
            }
            //选中第一个
            ovalLayout?.getChildAt(0)?.setBackgroundResource(triple.first)
        }
        //设置图片数据
        advAdapter.list = list
        advAdapter.onItemClick = { onPageClick?.invoke(it) }
        //设置默认选中的起始位置
        banner?.setCurrentItem(if (list.size > 1) halfPosition - halfPosition % list.size else 0, false)
    }

    /**
     * 开始自动滚动任务 图片大于1张才滚动
     */
    fun startRoll() {
        if (allowScroll && list.size > 1) {
            if (timer == null) {
                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        weakHandler.post {
                            val current = banner?.currentItem.orZero
                            var position = current + 1
                            if (current == 0 || current == Int.MAX_VALUE) position = halfPosition - halfPosition % list.size
                            banner?.currentItem = position
                        }
                    }
                }, 3000)
            }
        }
    }

    /**
     * 停止自动滚动任务
     */
    fun stopRoll() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }

    /**
     * 绑定对应页面的生命周期-》对应回调重写对应方法
     * @param lifecycleOwner
     */
    fun addLifecycleObserver(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun setAutoScroll(scroll: Boolean) {
        this.autoScroll = scroll
        if (!scroll) stopRoll()
    }

    override fun setOrientation(orientation: Int) {
        banner?.orientation = orientation
    }

    override fun setPageTransformer(marginPx: Int) {
        banner?.setPageTransformer(MarginPageTransformer(marginPx.dp))
    }
    // </editor-fold>

}