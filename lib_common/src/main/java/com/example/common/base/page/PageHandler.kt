package com.example.common.base.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.window.embedding.ActivityEmbeddingController
import androidx.window.embedding.SplitController
import androidx.window.embedding.SplitInfo
import com.example.common.R
import com.example.common.base.BaseActivity
import com.example.common.base.BaseActivity.Companion.isAnyActivityStarting
import com.example.common.base.page.Extra.BUNDLE_OPTIONS
import com.example.common.base.page.Extra.RESULT_CODE
import com.example.common.base.page.PageInterceptor.Companion.shouldIntercept
import com.example.common.utils.ScreenUtil.getCurrentActivityWindowSizePx
import com.example.common.utils.function.getCustomOption
import com.example.common.utils.manager.AppManager
import com.example.common.widget.EmptyLayout
import com.example.common.widget.xrecyclerview.XRecyclerView
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.example.framework.utils.function.getIntent
import com.example.framework.utils.function.value.toBundle
import com.example.framework.utils.function.value.toPairs
import com.therouter.TheRouter
import com.therouter.router.Navigator
import com.therouter.router.matchRouteMap
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 列表页快速处理空数据状态
 * @param count 列表数据条数
 * @param resId 空态图片资源
 * @param text 自定义空提示文本（原生字符串，非String资源id）
 */
fun XRecyclerView?.setListEmpty(count: Int = 0, resId: Int? = null, text: String? = null) {
    this ?: return
    finishRefreshing()
    // 判断集合长度，有长度不展示EmptyLayout只做提示
    if (count <= 0) empty.setEmptyUi(resId, text)
}

/**
 * 更新空白占位布局图文
 * @param resId 空图资源
 * @param text 自定义提示文本字符串
 * @param viewIndex 空布局所在子View下标
 */
fun ViewGroup?.setEmptyUi(resId: Int? = null, text: String? = null, viewIndex: Int = 1) {
    this ?: return
    val emptyLayout = if (this is EmptyLayout) this else getEmptyLayout(viewIndex)
    emptyLayout?.error(resId, text)
}

/**
 * 获取/自动创建空白占位布局
 * @param viewIndex 空布局下标
 */
fun ViewGroup?.getEmptyLayout(viewIndex: Int = 1): EmptyLayout? {
    this ?: return null
    return if (childCount <= 1) {
        EmptyLayout(context).apply {
            onInflate()
        }.also{ addView(it) }
    } else {
        getChildAt(viewIndex) as? EmptyLayout
    }
}

/**
 * 页面跳转的构建
 */
fun Activity.navigation(path: String, vararg params: Pair<String, Any?>?, activityResultValue: ActivityResultLauncher<Intent>, options: ActivityOptionsCompat? = null) {
    // 构建router跳转
    val navigator = TheRouter.build(path)
    // createIntent内部会触发 PageInterceptor 的 process 方法,故而之前先set一个值,process内部做处理
    navigator.withBoolean(Extra.SKIP_INTERCEPT, true)
    val intent = navigator.createIntent(this)
    /**
     * 添加标记 : 检查目标页面是否已经在任务栈中，在的话直接拉起来
     * Activity 会调用 onNewIntent 方法来接收新的 Intent，并且它的生命周期方法调用顺序与普通启动 Activity 有所不同，
     * 不会调用 onCreate 和 onStart 方法，而是调用 onRestart、onResume 等方法。
     */
    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
    // 判断跳转参数
    var hasResultCode = false
    if (params.isNotEmpty()) {
        // 过滤掉 null 值
        val nonNullParams = params.filterNotNull()
        hasResultCode = nonNullParams.find { it.first == RESULT_CODE } != null
        // 排除 RESULT_CODE 参数，将其他参数添加到 Bundle 中
        val bundle = nonNullParams.filter { it.first != RESULT_CODE }.toBundle { this }
        intent.putExtras(bundle)
    }
    // 标记是否有动画配置
    if (null != options) {
        intent.putExtra(BUNDLE_OPTIONS, true)
    }
    // 获取一下拦截器
    navigator.navigateWithInterceptors({
        // 检查 Activity 是否存活
        if (!isFinishing && !isDestroyed) {
            // 跳转对应页面
            if (!hasResultCode) {
                startActivity(intent, options?.toBundle())
            } else {
                activityResultValue.launch(intent, options)
            }
            // 基类添加正在启动
            val cls = navigator.getDestinationClass() ?: return@navigateWithInterceptors
            if (BaseActivity::class.java.isAssignableFrom(cls)) isAnyActivityStarting = true
        }
    }, {
        it?.printStackTrace()
    })
}

/**
 * 获取Router构建的class文件
 * Navigator 仅在 TheRouter.build(path) 后短期调用、用完即释放，不存在上下文（Context）被长期引用的场景
 */
fun Navigator.getDestinationClass(): Class<*>? {
    return try {
        // 使用 TheRouter.matchRouteMap() 查找 RouteItem
        val routeItem = matchRouteMap(url)
        // 从 RouteItem 中获取目标类的完整名称字符串
        val className = routeItem?.className ?: return null
        // 使用 Java 反射，将类名字符串转换为 Class 对象
        val targetClass = Class.forName(className)
        // 返回对应的类
        targetClass
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
        null
    }
}

fun String.getDestinationClass(): Class<*>? {
    return TheRouter.build(this).getDestinationClass()
}

/**
 * 路由路径扩展：去掉路由前缀，得到Activity短名，转小写
 * 例："/home/SplitActivity" -> "splitactivity"
 */
fun String.toActivitySimpleName(): String {
    return this.substringAfterLast("/").lowercase(Locale.getDefault())
}

/**
 * 获取Router构建的拦截器
 */
fun Navigator.navigateWithInterceptors(onContinue: () -> Unit, onInterrupt: (Throwable?) -> Unit) {
    try {
        // 匹配路由（复用 Navigator 自身的匹配逻辑）
        val routeItem = matchRouteMap(url)
        // 调用全局拦截器的 shouldIntercept，确保规则统一
        val isIntercepted = shouldIntercept(routeItem) { throwable ->
            // 异常回调（比如路由参数配置错误）
            throw throwable
        }
        // 根据拦截结果触发对应回调
        if (!isIntercepted) {
            onContinue()
        }
    } catch (e: Exception) {
        // 捕获其他意外异常（比如 matchRouteMap 失败）
        onInterrupt(e)
    }
}

/**
 * 取得当前页面所有获取的传输参数,丢给下个页面
 * *getIntent().getParams()
 */
fun Intent?.getParams(): Array<Pair<String, Any?>> {
    if (this == null) return emptyArray()
    val list = extras?.toPairs() ?: emptyList()
    return list.toTypedArray()
}

/**
 * 透明动画
 */
fun Context?.getNoneOptions(): ActivityOptionsCompat? {
    this ?: return null
    return getCustomOption(this, R.anim.set_alpha_none, R.anim.set_alpha_none)
}

/**
 * 默认透明动画配置
 */
fun Context?.getFadeOptions(): ActivityOptionsCompat? {
    this ?: return null
    return getCustomOption(this, R.anim.set_alpha_in, R.anim.set_alpha_out)
}

/**
 * 默认方向动画配置
 */
fun Context?.getSlideOptions(): ActivityOptionsCompat? {
    this ?: return null
    return getCustomOption(this, R.anim.set_translate_bottom_in, R.anim.set_translate_bottom_out)
}

/**
 * 页面如果在栈底,跳转拉起新页面的时候采用当前配置,过渡掉系统动画
 */
fun FragmentActivity?.getNonePreview(): ActivityOptionsCompat? {
    this ?: return null
    return getNoneOptions().apply {
        schedule({
            finish()
        }, 500)
    }
}

fun FragmentActivity?.getFadePreview(): ActivityOptionsCompat? {
    this ?: return null
    return getFadeOptions().apply {
        schedule({
            finish()
        }, 500)
    }
}

fun FragmentActivity?.getSlidePreview(): ActivityOptionsCompat? {
    this ?: return null
    return getSlideOptions().apply {
        schedule({
            finish()
        }, 500)
    }
}

/**
 * 创建安全的共享元素过渡参与者数组
 * 1) 规避系统 UI 过渡 bug，参考：https://plus.google.com/+AlexLockwood/posts/RPtwZ5nNebb
 * 2) 共享元素默认会把 DecorView 里的状态栏、导航栏背景一起纳入过渡，不加处理会闪屏
 * 3) enableEdgeToEdge() 页面：系统 statusBarBackground 不存在，includeStatusBar=true 也只会空跑
 * 4) EdgeToEdge 场景状态栏区域背景需要业务自行作为共享元素传入 otherParticipants
 * @param includeStatusBar 是否尝试将【系统状态栏背景View】加入共享元素动画，默认false（适配edgeToEdge主流场景）
 * @param otherParticipants 其他自定义共享元素，可变参数：View 和 transitionName 的配对
 * @return 共享元素 Pair 数组，传给 ActivityOptions.makeSceneTransitionAnimation
 * Pair(holder.sample_icon as View, "square_blue"), Pair(holder.sample_name as View, "sample_blue_title")
 * <ImageView
 *     android:id="@+id/square_blue"
 *     android:layout_width="150dp"
 *     android:layout_height="150dp"
 *     android:src="@drawable/bg_circle"
 *     android:transitionName="square_blue" />
 * <TextView
 *     android:id="@+id/title"
 *     style="@style/AppTitleThemeInverse"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:layout_gravity="center_vertical|start"
 *     android:transitionName="sample_blue_title" />
 */
fun FragmentActivity?.createSafeTransitionParticipants(includeStatusBar: Boolean = false, vararg otherParticipants: Pair<View, String>): Array<Pair<View, String>> {
    this ?: return emptyArray()
    // 获取 Activity 根 DecorView（包含状态栏、导航栏+页面内容）
    val decor = window.decorView
    var statusBar: View? = null
    if (includeStatusBar) {
        // 找到系统状态栏背景 View，android.R.id.statusBarBackground 是系统内置id
        statusBar = decor.findViewById(android.R.id.statusBarBackground)
    }
    // 找到系统导航栏（底部虚拟按键栏）背景 View
    val navBar = decor.findViewById<View>(android.R.id.navigationBarBackground)
    // 创建共享元素参与者列表，预分配容量3：状态栏、导航栏 + 自定义元素
    val participants = ArrayList<Pair<View,String>>(3)
    // 把状态栏 View 添加进列表（内部会判断view不为null才add）
    addNonNullViewToTransitionParticipants(statusBar, participants)
    // 把导航栏 View 添加进列表（同样非空判断）
    addNonNullViewToTransitionParticipants(navBar, participants)
    // 将外部传入的共享元素全部追加到参与者列表
    participants.addAll(listOf(*otherParticipants))
    // ArrayList转数组，作为最终共享元素数组返回
    return participants.toTypedArray<Pair<View, String>>()
}

/**
 * 如果view不为空，就把View和它的transitionName组成Pair，添加进共享参与者列表
 * @param view 待加入的View（状态栏/导航栏/自定义共享View）
 * @param participants 共享元素列表
 */
private fun addNonNullViewToTransitionParticipants(view: View?, participants: ArrayList<Pair<View, String>>) {
    // View为空直接return，不添加
    if (view == null) return
    // 组装Kotlin Pair：View + view自身的transitionName，添加到列表
    participants.add(Pair(view, view.transitionName))
}

/**
 * 检测大屏设备
 * @return true-检测到大屏设备并弹出提示，false-正常设备
 */
fun FragmentActivity?.checkLargeScreen(): Boolean {
    this ?: return false
    // 页面销毁直接返回
    if (isFinishing || isDestroyed) return false
    // 判断是否为大屏设备（宽度≥600dp）
    val config = resources.configuration
    // smallestScreenWidthDp 是设备物理尺寸，分屏不会变
    return config.smallestScreenWidthDp >= 600
}

/**
 * 判断当前是否处于Activity‑Embedding分栏嵌入会话
 * @return true 当前处于分栏；false 非分栏状态
 */
fun FragmentActivity?.checkEmbed(): Boolean {
    this ?: return false
    if (isFinishing || isDestroyed) return false
    // 设备支持embedding && 当前activity已经嵌入分栏
    return isActivityEmbeddingAvailable() && isActivityEmbedded()
}

/**
 * 持续监听：是否存在 HALF_OPENED 桌面模式(书本半开)
 * 注意：异步持续回调，lifecycleScope自动随页面销毁取消订阅
 * 1) 订阅建立瞬间，立刻回调 1 次当前真实状态，不需要等待窗口发生变化
 * 2) 每当窗口布局变化：折叠 / 展开、分屏、旋转、调整窗口大小，系统推送新的WindowLayoutInfo，再次执行 block
 * 3) Activity onDestroy → lifecycleScope 自动 cancel，collect 终止，不再接收事件，无内存泄漏
 */
// 窗口占设备最大窗口比例阈值，视为接近完整大屏展开
const val FULL_DISPLAY_RATIO_THRESHOLD = 0.85f
// 绝对窗口宽度px阈值，过滤普通双折叠大比例分栏误命中
const val ABSOLUTE_HUGE_WIDTH_THRESHOLD = 1850

/**
 * 监听Activity‑Embedding有效容器数量变化
 * @return Job 跟随Activity lifecycleScope自动取消
 */
fun FragmentActivity?.observeEmbeddedContainerCount(block: (containerCount: Int, targetSplit: SplitInfo?) -> Unit): Job? {
    val activity = this ?: return null
    return activity.lifecycleScope.launch(Main.immediate) {
        SplitController.getInstance(activity)
            .splitInfoList(activity)
            // 添加缓冲区，防止快速状态切换丢事件
            .buffer()
            .collectLatest { splitInfoList ->
                // 从末尾向前遍历，找到第一个匹配的分屏信息 (折叠不会匹配到)
                val targetSplit = splitInfoList.lastOrNull { splitInfo ->
                    splitInfo.primaryActivityStack.contains(activity) || splitInfo.secondaryActivityStack.contains(activity)
                }
                if (targetSplit == null) {
                    block(0, null)
                    return@collectLatest
                }
                // 统计非空 ActivityStack 数量（基于 Activity 存活）
                var containerCount = 0
                if (!targetSplit.primaryActivityStack.isEmpty) containerCount++
                if (!targetSplit.secondaryActivityStack.isEmpty) containerCount++
                block(containerCount, targetSplit)
            }
    }
}

/**
 * Activity‑Embedding 条件化启动副页 Activity
 * 内部完成全套防护：旋转重建拦截、能力检测、已分栏拦截、窗口阈值校验
 * @param targetCls 需要启动的副页 Activity Class
 * @param pairs 需要传输的页面参数
 * @param minWidthDp 规则匹配最小宽度 dp，默认 600
 * @param minSmallestDp 规则匹配最小边 dp，默认 600
 */
fun FragmentActivity?.startEmbedSecondaryIfNeeded(targetCls: Class<out Activity>, vararg pairs: Pair<String, Any?>, minWidthDp: Float = 600f, minSmallestDp: Float = 600f) {
    this ?: return
    // 设备系统层面不支持 embedding，直接返回
    if (!isActivityEmbeddingAvailable()) return
    // 已经处于分栏，不要再启动副页，避免重复实例
    if (isActivityEmbedded()) return
    // 页面存活则不开启
    if (AppManager.isActivityAlive(targetCls)) return
    // 获取当前窗口宽高 px
    val (wPx, hPx) = getCurrentActivityWindowSizePx(this)
    val density = resources.displayMetrics.density
    val widthDp = wPx / density
    val smallestDp = minOf(wPx, hPx) / density
    // 必须同时满足宽度、最小边阈值
    if (widthDp < minWidthDp || smallestDp < minSmallestDp) return
    // 执行时刻 Activity 可能已 finish
    if (isFinishing) return
    // 全部条件满足，启动副页，触发 SplitPairRule
    startActivity(getIntent(targetCls, *pairs).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

/**
 * 判断当前 Activity 是否正处于 Activity‑Embedding 分栏嵌入容器内
 * 仅识别【同应用内 Embedding 分栏】；用户手动拖拽的跨App系统分屏，此方法返回 false
 * 1) 折叠大屏展开，App 全屏单栈运行，未命中 SplitPairRule/SplitPlaceholderRule → false（硬件支持分栏，但并未切分页面）
 * 2) 折叠大屏展开，命中分栏规则，两个 Activity 左右并排分栏运行 → true
 * 3) 极异常场景：折叠闭合小屏，却被分栏容器托管 → true
 */
fun FragmentActivity?.isActivityEmbedded(): Boolean {
    this ?: return false
    return ActivityEmbeddingController.getInstance(this).isActivityEmbedded(this)
}

/**
 * 查询设备&系统是否具备 Activity‑Embedding 分栏硬件与系统能力
 * 1) 返回 true 仅代表设备支持该特性，不代表此刻 App 正在分栏显示
 * 2) 仅用于能力预检测、埋点；不可用于判断运行时分栏状态
 * 3) 原生 Android12L + 平板也会为 true
 */
fun Context?.isActivityEmbeddingAvailable(): Boolean {
    this ?: return false
    return SplitController.getInstance(this).splitSupportStatus == SplitController.SplitSupportStatus.SPLIT_AVAILABLE
}