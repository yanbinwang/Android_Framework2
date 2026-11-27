package com.example.common.base.page

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.core.layout.WindowSizeClass
import androidx.window.embedding.ActivityEmbeddingController
import androidx.window.embedding.SplitController
import androidx.window.embedding.SplitInfo
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowMetricsCalculator
import androidx.window.layout.adapter.computeWindowSizeClass
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
import kotlinx.coroutines.flow.distinctUntilChanged
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
 * 获取当前 Activity 窗口的尺寸快照 (依赖 window-core-android)
 * 【注意】本方法仅返回调用瞬间的窗口快照，**不会自动监听窗口变化**；
 * 如需窗口尺寸变更自动刷新UI，需要配合 WindowInfoTracker 监听，每次布局变更后重新调用本方法。
 *
 * 使用示例:
 *  val sizeClass = currentWindowSizeClass()
 *  val isMedium = sizeClass?.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) == true
 *  val isExpanded = sizeClass?.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) == true
 *  isMedium（窗口宽度 ≥600dp）：
 *    可开启双栏，左侧列表、右侧详情（订单 / 持仓 / 交易记录）
 *    折叠机展开、平板横屏满足条件时启用；若App窗口被分屏压缩到 ＜600dp，则变为false，切回单栏
 *  isExpanded（窗口宽度 ≥840dp）：
 *    更宽的大屏窗口，可进一步放大图表、增加更多辅助面板
 */
fun FragmentActivity?.currentWindowSizeClass(): WindowSizeClass? {
    this ?: return null
    return WindowSizeClass.BREAKPOINTS_V2.computeWindowSizeClass(
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
    )
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
    // 页面销毁直接返回
    if (isFinishing || isDestroyed) return false
    // 设备支持embedding && 当前activity已经嵌入分栏
    return isActivityEmbeddingAvailable() && isActivityEmbedded()
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
fun FragmentActivity?.isActivityEmbeddingAvailable(): Boolean {
    this ?: return false
    return SplitController.getInstance(this).splitSupportStatus == SplitController.SplitSupportStatus.SPLIT_AVAILABLE
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
    if (isFinishing || isDestroyed) return
    // 全部条件满足，启动副页，触发 SplitPairRule
    startActivity(getIntent(targetCls, *pairs).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

/**
 * 监听当前 Activity 窗口内的折叠铰链/折痕状态变化
 * 仅在页面生命周期 >= STARTED（onStart ~ onStop之间）接收回调；onStop 自动暂停，切回前台自动恢复
 *
 * @param block 回调，返回当前窗口内全部 [FoldingFeature] 集合
 *  单铰链折叠屏：List最多1个元素；三折叠多铰链设备会返回多条 Feature
 *  普通无折痕设备/窗口内不存在折痕：返回空List
 *  FoldingFeature 字段说明：
 *  - state：折叠状态 [FoldingFeature.State]
 *     FLAT：完全展开，屏幕为一整块平面
 *     HALF_OPENED：半折悬停（Tabletop桌面模式，水平铰链常用）
 *  - orientation：铰链方向 [FoldingFeature.Orientation]
 *     HORIZONTAL：水平铰链，上下对折，常用于桌面悬停tabletop
 *     VERTICAL：垂直铰链，左右对折，书本模式
 *  - bounds：折痕/铰链在当前窗口坐标系下的Rect区域
 *  - occlusionType：遮挡类型
 *     NONE：铰链不遮挡显示内容
 *     FULL：铰链区域完全遮挡画面
 *  - isSeparating：该折痕是否把窗口分割为两块独立可视区域
 *
 * 注意：
 * 1) 拿到的是【当前 Activity 窗口内部】的折痕，不是全局屏幕状态。Activity Embedding 左右双 Activity，各自只能拿到自己窗口内的 Feature
 * 2) 分屏拖拽、折叠开合、旋转、窗口 resize 都会触发回调
 * 3) 回调在主线程 Main.immediate，可以直接操作 View；增加保护：if(isFinishing || window == null) return
 * 4) 只监听窗口折痕，不能用来判断 Activity Embedding 的容器数量，容器数量仍需要 SplitController/ActivityEmbeddingController
 *
 * 调用示例：
 * val foldJob = observeFoldingState { featureList ->
 *     val firstFeature = featureList.firstOrNull()
 *     when (firstFeature?.state) {
 *         FoldingFeature.State.HALF_OPENED -> {
 *             if (firstFeature.orientation == FoldingFeature.Orientation.HORIZONTAL) {
 *                 // Tabletop 桌面悬停
 *             } else {
 *                 // 书本对折模式
 *             }
 *         }
 *         FoldingFeature.State.FLAT -> {
 *             // 完全展开
 *         }
 *         null -> {
 *             // 无铰链 / 当前窗口不存在折痕
 *         }
 *     }
 * }
 */
fun FragmentActivity?.observeFoldingState(block: (List<FoldingFeature>) -> Unit): Job? {
    val activity = this ?: return null
    return activity.lifecycleScope.launch(Main.immediate) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            WindowInfoTracker.getOrCreate(activity)
                .windowLayoutInfo(activity)
                // 添加缓冲区，防止快速状态切换丢事件
                .buffer()
                // 过滤重复窗口布局信息：仅当窗口布局真正发生变化才下发，连续相同状态直接丢弃
                .distinctUntilChanged()
                .collectLatest { layoutInfo ->
                    // 页面正在 finish 或者 window 已经脱离，直接跳过 UI 更新
                    if (activity.isFinishing || activity.window == null) return@collectLatest
                    // 提取折叠/铰链对象
                    val foldingFeatureList = layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>()
                    block(foldingFeatureList)
                }
        }
    }
}

/**
 * 监听 Activity Embedding 分屏容器数量变化
 * 仅在页面生命周期 >= STARTED（onStart ~ onStop之间）接收回调；onStop自动暂停，切回前台自动恢复
 * @param block 回调
 *  containerCount：当前 Activity Embedding 容器数量，取值 0 / 1 / 2
 *  targetSplit：当前匹配到的 [SplitInfo]，如果不在任何嵌入分屏内则为null
 *
 * 逻辑说明：
 *  从splitInfoList取最后一条匹配当前Activity的SplitInfo，判断当前Activity属于primary或secondary栈；
 *  根据primary/secondary ActivityStack是否非空统计容器个数。
 *
 * 注意：
 * 1) 此为 Activity Embedding 专属能力，用于判断当前是否双 Activity 左右分屏；
 *    仅能感知由 Activity Embedding 产生的多 Activity 容器，不能替代 WindowInfoTracker 获取折痕、窗口尺寸、WindowSizeClass
 * 2) 分屏创建/销毁、窗口 resize、折叠状态变更、Activity 栈切换都会触发回调
 * 3) 回调在主线程 Main.immediate，可以直接操作 View；增加保护：if(isFinishing || window == null) return
 * 4) 和 [observeFoldingState] 配套使用，二者职责分离：
 *     observeEmbeddedContainerCount → Activity栈/容器数量
 *     observeFoldingState → 当前窗口折痕、铰链状态
 */
fun FragmentActivity?.observeEmbeddedContainerCount(block: (containerCount: Int, targetSplit: SplitInfo?) -> Unit): Job? {
    val activity = this ?: return null
    return activity.lifecycleScope.launch(Main.immediate) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            SplitController.getInstance(activity)
                .splitInfoList(activity)
                .buffer()
                .collectLatest { splitInfoList ->
                    // 页面正在 finish 或者 window 已经脱离，直接跳过回调
                    if (activity.isFinishing || activity.window == null) return@collectLatest
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
}