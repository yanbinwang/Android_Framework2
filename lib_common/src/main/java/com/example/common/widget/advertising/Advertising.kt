package com.example.common.widget.advertising

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.example.common.utils.function.getCenterPixelColor
import com.example.common.utils.function.pt
import com.example.common.utils.function.safeRecycle
import com.example.framework.utils.function.value.createOvalDrawable
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.view.adapter
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.gone
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.reduceSensitivity
import com.example.framework.utils.function.view.size
import com.example.framework.utils.function.view.visible
import com.example.framework.widget.BaseViewGroup
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

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
    // 图片路径数组
    private var list = ArrayList<String>()
    // 图片背景数组
    private var coverList = ArrayList<Pair<Boolean, Int>>()
    // 是否允许滑动
    private var allowScroll = true
    // 是否自动滚动
    private var autoScroll = true
    // 3个资源路径->圆点选中时的背景ID second：圆点未选中时的背景ID third：圆点间距 （圆点容器可为空写0）
    private var triple = Triple(createOvalDrawable("#3d81f2"), createOvalDrawable("#6e7ce2"), 10)
    // 协程 Job 控制滚动任务
    private var scrollJob: Job? = null
    // 滚动间隔（可自定义，默认3秒）
    private val scrollPeriod = 3000L
    // 启动延迟（默认3秒，避免刚初始化就滚动）
    private val scrollDelay = 3000L
    // 广告容器
    private var banner: ViewPager2? = null
    // 圆点容器
    private var ovalLayout: LinearLayout? = null
    // 设定一个中心值下标
    private val halfPosition by lazy { Int.MAX_VALUE / 2 }
    // 图片适配器
    private val advAdapter by lazy { AdvertisingAdapter() }
    // 注册广告监听
    private val callback by lazy { object : OnPageChangeCallback() {
        // 当前选中的数组索引
        private var curIndex = 0
        // 上次选中的数组索引
        private var oldIndex = 0
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            // 切换圆点
            curIndex = position % list.size
            if (null != ovalLayout) {
                if (list.size > 1) {
                    ovalLayout?.getChildAt(oldIndex)?.background = triple.second
                    ovalLayout?.getChildAt(curIndex)?.background = triple.first
                    oldIndex = curIndex
                }
            }
            // 切换
            onPagerCurrent?.invoke(curIndex)
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            allowScroll = positionOffsetPixels == 0
            // 无需复杂容错，colorList已完整
            if (coverList.safeSize > 0) {
                val mPosition = position % list.size
                if (mPosition >= coverList.size - 1) return
                val (selected, startColor) = coverList.safeGet(mPosition) ?: (false to 0)
                val endColor = coverList.safeGet(mPosition + 1)?.second.orZero
                val blendedColor = ColorUtils.blendARGB(startColor, endColor, positionOffset)
                onPageScrolled?.invoke(selected to blendedColor)
            }
        }
    }}
    // 回调方法
    private var onPagerClick: ((index: Int) -> Unit)? = null
    private var onPagerCurrent: ((index: Int) -> Unit)? = null
    private var onPageScrolled: ((data: Pair<Boolean, Int>) -> Unit)? = null
    // 获取当前下标
    private val absolutePosition get() = halfPosition - halfPosition % list.size

    companion object {
        /**
         * 异步获取图片的中心像素颜色（用于封面色、主题色等场景）
         * @param context 上下文（注意：若为Activity/Fragment，需确保不泄露，建议用ApplicationContext）
         * @param imageSource 图片源（支持Int资源ID、String网络/本地URL等Glide支持的类型）
         * @return 中心像素颜色，获取失败时返回白色（Color.WHITE）
         * // 配置Palette，提高颜色提取质量
         * val palette = Palette.from(bitmap)
         *     // 增加颜色数量
         *     .maximumColorCount(32)
         *     .generate()
         * // 多种颜色提取策略，提高成功率
         * val pageColor = palette.getVibrantColor(palette.getMutedColor(palette.getLightVibrantColor(palette.getDarkVibrantColor(Color.WHITE))))
         */
        @SuppressLint("CheckResult")
        suspend fun getImageCenterPixelColor(context: Context, imageSource: Any): Int {
            return withContext(IO) {
                var bitmap: Bitmap? = null
                var futureTarget: FutureTarget<Bitmap>? = null
                try {
                    // 加载图片时可以指定尺寸，避免过大的Bitmap占用过多内存
                    futureTarget = Glide.with(context)
                        .asBitmap()
                        .also {
                            when (imageSource) {
                                is Int -> it.load(imageSource)
                                is String -> it.load(imageSource)
                                // 若需支持更多类型（如File、Uri），可补充case
                                // is File -> requestBuilder.load(imageSource)
                                // is Uri -> requestBuilder.load(imageSource)
                            }
                        }
                        // 缓存所有版本，减少重复加载
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        // 限制输出200x200像素，平衡精度与内存
                        .submit(200, 200)
                    // 等待Glide加载完成，获取Bitmap（超时会抛异常，被catch捕获）
                    bitmap = futureTarget.get() ?: return@withContext Color.WHITE
                    // 计算中心像素坐标，获取颜色
                    bitmap.getCenterPixelColor()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@withContext Color.WHITE
                } finally {
                    // 取消Glide请求，避免内存泄漏
                    futureTarget?.let {
                        Glide.with(context).clear(it)
                    }
                    bitmap.safeRecycle()
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    init {
        banner = ViewPager2(context).apply {
            size(MATCH_PARENT, MATCH_PARENT)
            reduceSensitivity()
            adapter(advAdapter)
            registerOnPageChangeCallback(callback)
            setOnTouchListener { _, event ->
                when (event?.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        if (autoScroll) stopRoll()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (autoScroll) startRoll()
                    }
                }
                // 不拦截触摸事件，不影响用户滑动
                false
            }
            isNestedScrollingEnabled = false
        }
    }

    override fun onInflate() {
        if (isInflate) addView(banner)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lifecycleOwner?.lifecycle?.addObserver(this)
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
            Lifecycle.Event.ON_DESTROY -> {
                stopRoll()
                banner?.unregisterOnPageChangeCallback(callback)
                source.lifecycle.removeObserver(this)
            }
            else -> {}
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实现方法">
    override fun start(uriList: ArrayList<String>) {
        this.list = uriList
        // 设置数据
        initData()
        // 开始滚动
        startRoll()
    }

    /**
     * 初始化圆点,图片数据
     */
    private fun initData() {
        // 如果只有一第图时不显示圆点容器
        if (ovalLayout != null) {
            if (list.size < 2) {
                ovalLayout?.gone()
            } else {
                ovalLayout?.gravity = Gravity.CENTER
                ovalLayout?.visible()
                ovalLayout?.removeAllViews()
                ovalLayout?.doOnceAfterLayout {
                    // 如果true代表垂直，否则水平
                    val direction = it.layoutParams?.height.orZero > it.layoutParams?.width.orZero
                    // 左右边距
                    val ovalMargin = triple.third.pt
                    // 添加圆点
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
                    // 选中第一个
                    it.getChildAt(0)?.background = triple.first
                }
            }
        }
        // 设置图片数据
        advAdapter.refresh(list)
        advAdapter.setOnItemClickListener {
            onPagerClick?.invoke(it)
        }
        // 设置默认选中的起始位置
        banner?.setCurrentItem(if (list.safeSize > 1) absolutePosition else 0, false)
    }

    /**
     * 放在start方法之前调取，不然会走默认
     */
    override fun setConfiguration(radius: Int, localAsset: Boolean, scroll: Boolean, ovalList: Triple<Drawable, Drawable, Int>?, ovalLayout: LinearLayout?, barList: ArrayList<Pair<Boolean, Int>>?) {
        this.autoScroll = scroll
        this.ovalLayout = ovalLayout
        this.coverList = barList ?: ArrayList()
        this.advAdapter.setParams(radius, localAsset)
        if (ovalList != null) this.triple = ovalList
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
    fun startRoll() {
        // 不允许滚动 || 图片少于1张 → 直接返回
        if (!autoScroll || list.size <= 1) return
        // 启动前先取消已有任务（避免重复启动）
        stopRoll()
        // 绑定生命周期：用 lifecycleOwner 的协程作用域，页面销毁时自动取消
        scrollJob = lifecycleOwner?.lifecycleScope?.launch {
            try {
                // 启动延迟
                delay(scrollDelay)
                // 循环滚动：flow 实现定时发射
                flow {
                    while (true) {
                        emit(Unit)
                        // 每次滚动间隔
                        delay(scrollPeriod)
                    }
                }.flowOn(IO).collect {
                    // 切换到主线程更新 UI（ViewPager2 必须在主线程操作）
                    withContext(Main.immediate) {
                        if (allowScroll) {
                            val current = banner?.currentItem.orZero
//                            val position = if (current == 0 || current == Int.MAX_VALUE) absolutePosition else current + 1
//                            banner?.currentItem = position
                            // 先计算下一个位置
                            val nextPosition = current + 1
                            // 如果下一个位置超过「中间值+10000」→ 重置到中间位置；否则用nextPosition
                            val targetPosition = if (nextPosition > halfPosition + 10000) absolutePosition else nextPosition
                            banner?.currentItem = targetPosition
                        }
                    }
                }
            } catch (_: CancellationException) {
                // 协程被取消时，静默处理（正常流程，无需打印日志）
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 停止自动滚动任务
     */
    fun stopRoll() {
        // 置空，避免内存泄漏
        scrollJob?.cancel()
        scrollJob = null
    }

//    /**
//     * 绑定对应页面的生命周期-》对应回调重写对应方法
//     * @param observer
//     */
//    fun addObserver(observer: LifecycleOwner) {
//        observer.lifecycle.addObserver(this)
//    }

    /**
     * 设置广告监听
     */
    fun setAdvertisingListener(onPagerClick: (index: Int) -> Unit = {}, onPagerCurrent: (index: Int) -> Unit = {}, onPageScrolled: (Pair<Boolean, Int>) -> Unit = {}) {
        this.onPagerClick = onPagerClick
        this.onPagerCurrent = onPagerCurrent
        this.onPageScrolled = onPageScrolled
    }
    // </editor-fold>

}