package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.base.utils.WeakHandler
import com.example.base.utils.function.dip2px
import com.example.base.widget.SimpleViewGroup
import com.example.common.R
import com.example.common.widget.advertising.adapter.AdvertisingAdapter
import com.example.common.widget.advertising.callback.AdvertisingImpl
import java.util.*

/**
 * Created by wangyanbin
 * 广告控件
 */
@SuppressLint("ClickableViewAccessibility")
class Advertising @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SimpleViewGroup(context, attrs, defStyleAttr), AdvertisingImpl, DefaultLifecycleObserver {
    private var allow = true//是否允许滑动
    private var scroll = true//是否自动滚动
    private var local = false//是否是本地
    private var curIndex = 0//当前选中的数组索引
    private var oldIndex = 0//上次选中的数组索引
    private var margin = 0//左右边距
    private var focusedId = 0 //圆点选中时的背景ID
    private var normalId = 0//圆点正常时的背景ID
    private var timer: Timer? = null//自动滚动的定时器
    private var list: ArrayList<String>? = null//图片网络路径数组
    private var localList: ArrayList<Int>? = null//图片本地路径数组
    private var banner: ViewPager2? = null//广告容器
    private var ovalLayout: LinearLayout? = null//圆点容器
    private val halfPosition by lazy { Int.MAX_VALUE / 2 }  //计算中心值
    private val adapter by lazy { AdvertisingAdapter() } //图片适配器
    private val weakHandler by lazy { WeakHandler(Looper.getMainLooper()) } //切线程
    var onItemClick: ((index: Int) -> Unit)? = null

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    init {
        banner = ViewPager2(context)
        banner?.getChildAt(0)?.overScrollMode = OVER_SCROLL_NEVER
        banner?.adapter = adapter
        banner?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        banner?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //切换圆点
                curIndex = position % (list?.size ?: 0)
                if (ovalLayout != null && (list?.size ?: 0) > 1) {
                    //圆点取消
                    ovalLayout?.getChildAt(oldIndex)?.setBackgroundResource(normalId)
                    //圆点选中
                    ovalLayout?.getChildAt(curIndex)?.setBackgroundResource(focusedId)
                    oldIndex = curIndex
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                allow = positionOffsetPixels == 0
            }
        })
    }

    override fun drawView() {
        if (onFinish()) addView(banner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (scroll) startTimer()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        if (scroll) stopTimer()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopTimer()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实现方法">
    fun startLocal(resList: ArrayList<Int>, ovalLayout: LinearLayout?) {
        localList = resList
        list = ArrayList()
        for (ignored in resList) {
            list?.add("")
        }
        start(list!!, ovalLayout, R.mipmap.ic_ad_select, R.mipmap.ic_ad_unselect, 10, true)
    }

    fun start(uriList: ArrayList<String>) {
        start(uriList, null)
    }

    fun start(uriList: ArrayList<String>, ovalLayout: LinearLayout?) {
        start(uriList, ovalLayout, R.mipmap.ic_ad_select, R.mipmap.ic_ad_unselect, 10, false)
    }

    override fun start(uriList: ArrayList<String>, ovalLayout: LinearLayout?, focusedId: Int, normalId: Int, margin: Int, local: Boolean) {
        this.local = local
        this.list = uriList
        this.ovalLayout = ovalLayout
        this.focusedId = focusedId
        this.normalId = normalId
        this.margin = margin
        //设置数据
        initData()
    }

    /**
     * 初始化圆点,图片数据
     */
    private fun initData() {
        //如果只有一第图时不显示圆点容器
        if (ovalLayout != null && list!!.size < 2) {
            ovalLayout?.layoutParams?.height = 0
        } else if (ovalLayout != null) {
            ovalLayout?.gravity = Gravity.CENTER
            //如果true代表垂直，否则水平
            val direction = ovalLayout!!.layoutParams.height > ovalLayout!!.layoutParams.width
            //左右边距
            val ovalMargin = context.dip2px(margin.toFloat())
            //添加圆点
            for (i in list!!.indices) {
                val imageView = ImageView(context)
                ovalLayout?.addView(imageView)
                val layoutParams = imageView.layoutParams as LinearLayout.LayoutParams
                if (direction) {
                    layoutParams.setMargins(ovalMargin, 0, ovalMargin, 0)
                } else {
                    layoutParams.setMargins(0, ovalMargin, 0, ovalMargin)
                }
                imageView.layoutParams = layoutParams
                imageView.setBackgroundResource(normalId)
            }
            //选中第一个
            ovalLayout?.getChildAt(0)?.setBackgroundResource(focusedId)
        }
        //设置图片数据
        if (local) adapter.localList = localList!! else adapter.list = list!!
        adapter.onItemClickListener = object : AdvertisingAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                onItemClick?.invoke(position)
            }
        }
        //设置默认选中的起始位置
        var position = 0
        if (list!!.size > 1) position = halfPosition - halfPosition % list!!.size
        banner?.setCurrentItem(position, false)
    }

    /**
     * 开始自动滚动任务 图片大于1张才滚动
     */
    private fun startTimer() {
        if (timer == null) {
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    if (allow && list!!.size > 1) {
                        weakHandler.post {
                            val current = banner!!.currentItem
                            var position = current + 1
                            if (current == 0 || current == Int.MAX_VALUE) position = halfPosition - halfPosition % list!!.size
                            banner!!.currentItem = position
                        }
                    }
                }
            }, 3000, 3000)
        }
    }

    /**
     * 停止自动滚动任务
     */
    private fun stopTimer() {
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
        this.scroll = scroll
        if (!scroll) stopTimer()
    }

    override fun setOrientation(orientation: Int) {
        banner?.orientation = orientation
    }

    override fun setPageTransformer(marginPx: Int) {
        banner?.setPageTransformer(MarginPageTransformer(context.dip2px(marginPx.toFloat())))
    }
    // </editor-fold>

}