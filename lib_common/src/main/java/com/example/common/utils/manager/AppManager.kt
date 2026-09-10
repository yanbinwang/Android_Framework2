package com.example.common.utils.manager

import android.app.Activity
import android.content.Intent
import android.os.Process
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.BaseApplication
import com.example.common.base.page.Extra
import com.example.common.base.page.getDestinationClass
import com.example.common.base.page.getNoneOptions
import com.example.common.config.RouterPath
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import com.therouter.TheRouter
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * description 管理App中Activity的类
 * author yan
 * 方法嵌套导致多个LOCK锁开启,其实本质是同一把锁，且 synchronized 是可重入锁，嵌套不会导致死锁，只会产生 “重入计数” 的轻微开销
 */
object AppManager {
    // 对象锁
    private val LOCK = Any()
    // 存储 Activity 的弱引用（避免内存泄漏）
    private val activityDeque = ArrayDeque<WeakReference<Activity>>()
    // 当前栈内所有的 Activity 总数 (自定义镜像栈数量不等于系统任务栈真实数量，多 taskAffinity 场景会失真)
    val customStackActivityCount: Int
        get() {
            return synchronized(LOCK) { activityDeque.size }
        }
    // 获取当前栈顶 Activity 全称（包含包名路径）
    val currentActivityName: String?
        get() {
            val activity = currentActivity() ?: return null
            // 拼接 [应用包名 + localClassName] 得到完整类名（等价于原 shortClassName） -> com.example.activity.TestActivity
            return "${activity.packageName}.${activity.localClassName}"
        }

    /**
     * 获取当前栈顶 Activity（非销毁状态）
     * 从栈顶开始查找未销毁的Activity
     */
    fun currentActivity(): Activity? {
        return synchronized(LOCK) {
            activityDeque
                .asReversed()
                .firstOrNull { ref ->
                    ref.get()?.let {
                        !it.isDestroyed && !it.isFinishing
                    } ?: false
                }?.get()
        }
    }

    /**
     * 遍历所有存活的 Activity
     * 示例: 批量关闭
     * AppManager.forEachAlive {
     *   finish()
     * }
     * 注意：快照生成后 Activity 可能随时被 finish，lambda 内部建议二次校验 Activity 存活状态
     * 禁止传入阻塞/耗时逻辑；禁止在lambda内部操作AppManager集合
     */
    fun forEachAlive(func: Activity.() -> Unit) {
        val snapshot = synchronized(LOCK) {
            activityDeque
                .mapNotNull { it.get() }
                .filter { !it.isDestroyed && !it.isFinishing }
                .toList()
        }
        snapshot.forEach(func)
    }

    /**
     * 添加 Activity 到任务栈中
     * 1) 只是将页面添加进了自定义的任务栈，而控制 Activity 显示/关闭的是系统任务栈
     * 2) 自定义栈 activityDeque：顺序为 [A, B, C, D, E]，这是通过在 Activity D 里调用 addActivity(E) 得到的结果
     * 3) 系统任务栈：顺序为 [A, B, C, D]，只把 E 添加到了自定义栈，并没有调用 startActivity 方法将其添加到系统任务栈，所以 E 不在系统任务栈中
     */
    fun addActivity(activity: Activity?) {
        activity ?: return
        synchronized(LOCK) {
            // 先移除已存在的相同实例，避免重复
            activityDeque.removeAll { it.get() === activity }
            activityDeque.add(WeakReference(activity))
            // 同步清理已销毁的引用
            cleanDestroyedActivities()
        }
    }

    /**
     * 移除指定的 Activity
     */
    fun removeActivity(activity: Activity?) {
        activity ?: return
        synchronized(LOCK) {
            activityDeque.removeAll { it.get() === activity }
        }
    }

    /**
     * 结束指定的 Activity
     */
    fun finishActivity(activity: Activity?) {
        activity ?: return
        if (activity.isDestroyed || activity.isFinishing) return
        synchronized(LOCK) {
            activityDeque.removeAll { it.get() === activity }
        }
        try {
            activity.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 结束当前栈顶 Activity（堆栈中最后一个压入的）
     */
    fun finishCurrentActivity() {
        currentActivity()?.let { finishActivity(it) }
    }

    /**
     * 结束指定类的所有 Activity
     */
    fun finishActivitiesOfClass(cls: Class<*>?) {
        cls ?: return
        // 锁内仅做筛选，生成快照拷贝
        val snapshot = synchronized(LOCK) {
            activityDeque
                .mapNotNull { it.get() }
                .filter { it.javaClass === cls && !it.isDestroyed && !it.isFinishing }
                .toList()
        }
        // 锁已经释放，在外层执行关闭逻辑，复用 finishActivity 自带状态校验+try‑catch
        snapshot.forEach { activity ->
            finishActivity(activity)
        }
    }

    /**
     * 结束非指定类名的 Activity（保留指定类）
     * @param cls 要保留的 Activity 类（可变参数，支持多个类）
     */
    fun finishNotTargetActivity(vararg cls: Class<*>?) {
        val snapshot = synchronized(LOCK) {
            // 过滤 cls 数组中的null，得到“有效保留类列表”
            val validKeepClasses = cls.filterNotNull()
            // 特殊逻辑：若有效保留类为空（即传入的全是null），则结束所有存活 Activity
            val shouldFinishAll = validKeepClasses.isEmpty()
            // 过滤出需要结束的Activity
            activityDeque
                .mapNotNull { it.get() }
                .filter { activity ->
                    // 若需结束所有，则直接保留；否则排除“有效保留类”
                    (shouldFinishAll || activity.javaClass !in validKeepClasses) && !activity.isDestroyed && !activity.isFinishing
                }
                .toList()
        }
        // 批量结束 Activity
        snapshot.forEach { activity ->
            finishActivity(activity)
        }
    }

    /**
     * 结束指定类名的 Activity
     * @param cls 要结束的Activity类（可变参数，支持多个类）
     */
    fun finishTargetActivity(vararg cls: Class<*>?) {
        val snapshot = synchronized(LOCK) {
            val validTargetClasses = cls.filterNotNull()
            if (validTargetClasses.isEmpty()) {
                return@synchronized emptyList()
            }
            activityDeque
                .mapNotNull { it.get() }
                .filter { activity ->
                    activity.javaClass in validTargetClasses && !activity.isDestroyed && !activity.isFinishing
                }
                .toList()
        }
        snapshot.forEach { activity ->
            finishActivity(activity)
        }
    }

    /**
     * 结束所有 Activity，可通过 Application 再次拉起
     */
    fun finishAllActivities() {
        val snapshot = synchronized(LOCK) {
            // 锁内：取出所有持有实例，同时直接清空自定义栈
            val list = activityDeque
                .mapNotNull { it.get() }
                .filter { !it.isDestroyed && !it.isFinishing }
                .toList()
            activityDeque.clear()
            return@synchronized list
        }
        // 锁释放，批量关闭，复用 finishActivity 内部的状态校验、集合操作、异常保护
        snapshot.forEach { activity ->
            finishActivity(activity)
        }
    }

    /**
     * 结束除指定类外的所有 Activity，若指定类不存在则启动
     */
    fun finishAllExcept(cls: Class<*>?) {
        cls ?: return
        val (activitiesToFinish, targetActivity) = synchronized(LOCK) {
            val targetActivity = activityDeque
                .mapNotNull { it.get() }
                .find { it.javaClass === cls && !it.isDestroyed && !it.isFinishing }
            val activitiesToFinish = activityDeque
                .mapNotNull { it.get() }
                .filter { it.javaClass !== cls && !it.isDestroyed && !it.isFinishing }
            activitiesToFinish to targetActivity
        }
        activitiesToFinish.forEach { activity ->
            finishActivity(activity)
        }
        if (targetActivity == null) {
            launchTargetActivity(cls)
        }
    }

    /**
     * 关闭全部页面，强制重新拉起目标Activity（全部销毁，新建实例，不复用已有实例）
     */
    fun finishAllAndLaunchActivity(cls: Class<*>?) {
        cls ?: return
        finishAllActivities()
        launchTargetActivity(cls)
    }

    /**
     * 启动指定 Activity
     */
    private fun launchTargetActivity(cls: Class<*>?) {
        cls ?: return
        val context = BaseApplication.instance.applicationContext
        try {
            val intent = Intent(context, cls).apply {
                // 补充 FLAG_ACTIVITY_CLEAR_TASK，与 finishAllActivities() 配合，确保新 Activity 是根节点，
                // 强制系统清除目标任务栈中所有现有 Activity，确保新启动的 Activity 是 “唯一根节点”，避免极端情况下系统残留旧栈信息导致的动画或启动模式异常
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 退出App：关闭所有 Activity 后自杀杀死进程
     * 注意：不可在 Application#onCreate 阶段调用；kill 进程不会触发 Application.onTerminate
     * 300ms 仅为经验延时，不能 100% 保证全部 Activity 的 onDestroy 执行完毕
     */
    fun exitApp() {
        finishAllActivities()
        // 获取当前进程的唯一LifecycleOwner,
        ProcessLifecycleOwner.get().lifecycleScope.launch(Main.immediate) {
            // 预留 300 毫秒,避免因页面未完全销毁就杀进程，导致资源释放不彻底
            delay(300L)
            try {
                Process.killProcess(Process.myPid())
                // exitProcess(0) 是 Android 隐藏 API，替换为 Java 标准的 System.exit(0) 兼容性更强
                // System.exit(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 清理栈中已经被回收/销毁的 Activity 弱引用
     * 1) 函数内部自带锁保护，可在任意位置调用
     * 2) 若调用线程已经持有 LOCK 锁，会触发 synchronized 锁重入，属于安全行为
     * 3) 仅操作内部集合，不会触发Activity生命周期回调
     */
    private fun cleanDestroyedActivities() {
        synchronized(LOCK) {
            activityDeque.removeAll { ref ->
                val activity = ref.get()
                // 弱引用已被回收 或者 Activity已经标记销毁/结束，则移除
                activity == null || activity.isDestroyed || activity.isFinishing
            }
        }
    }

    /**
     * 判断指定类的 Activity 是否存在（存活状态）
     * @param cls Activity 类对象
     * @return true：栈中存在该类且未销毁/未结束的 Activity
     */
    fun isActivityAlive(cls: Class<*>?): Boolean {
        cls ?: return false
        synchronized(LOCK) {
            return activityDeque
                .mapNotNull { it.get() }
                .any { it.javaClass === cls && !it.isDestroyed && !it.isFinishing }
        }
    }

    /**
     * 判断除当前 Activity 外，是否存在指定类的存活 Activity
     * @param current 需要排除的当前 Activity 实例
     * @param cls 目标 Activity 类
     * @return true：栈中存在其它同类型存活Activity
     */
    fun isOtherActivityAlive(current: Activity, cls: Class<*>?): Boolean {
        cls ?: return false
        synchronized(LOCK) {
            return activityDeque
                .mapNotNull { it.get() }
                .any { it !== current && it.javaClass === cls && !it.isDestroyed && !it.isFinishing }
        }
    }

    /**
     * 重启app任务栈，保证目标页面在任务栈中「唯一存活」
     * 1) Android12+ 如果当前任务栈为空，通过 Application 拉起页面，系统会忽略 Activity 转场动画
     * 2) Trick：先拉起全屏透明中转 LinkActivity，再由中转页跳转目标路由，恢复动画效果
     * 3) 适用场景：注销、顶号、推送唤醒，清空旧栈后跳转目标页面
     * @param path TheRouter目标路由路径
     */
    fun rebootTaskStackAndLaunchTarget(path: String) {
        TheRouter.build(RouterPath.LinkActivity)
            .withString(Extra.SOURCE, "normal")
            .withString(Extra.ID, path)
            .navigation()
    }

    /**
     * 重启任务栈：先关闭已有同类页面，执行 block 跳转，延迟整理栈，仅保留目标类页面
     * @param path 目标路由路径
     * @param block 真正跳转逻辑，支持自定义 intent 参数
     */
    fun rebootTaskStackAndLaunchTarget(path: String, block: () -> Unit) {
        // 获取跳转的class
        val clazz = path.getDestinationClass()
        // 不管存在不存在,先关闭
        finishTargetActivity(clazz)
        // 跳转对应页面 (内部构建的跳转可能带有跳转参数,故而接口回调处理)
        block.invoke()
        // 延迟关闭,避免动画叠加(忽略需要跳转的页面)
        ProcessLifecycleOwner.get().schedule({
            // 对应页面会被忽略关闭,如果block.invoke()拉起了此时就不会被关闭
            finishAllExcept(clazz)
        }, 500)
    }

    /**
     * 判断目标页面 Class 是否存活
     * 1) 目标页面已存在：直接关闭当前 Activity
     * 2) 目标页面不存在：执行 block 跳转目标页面
     * @param targetCls 需要判断的目标页面Class（Login/Register）
     * @param currentActivity 当前页面实例，用于finish自己
     * @param block 跳转逻辑（TheRouter导航）
     */
    fun ensureTargetActivityAliveWithFallback(targetCls: Class<*>, currentActivity: Activity, block: () -> Unit) {
        ensureMainActivityAliveWithFallback {
            if (isActivityAlive(targetCls)) {
                finishActivity(currentActivity)
            } else {
                block.invoke()
            }
        }
    }

    /**
     * app如果未登录也可以进首页,需要一个兜底逻辑
     * 1) 确保任务栈内存在首页
     * 2) 确保任务栈内至少存在一个页面
     */
    fun ensureMainActivityAliveWithFallback(block: () -> Unit) {
        val mainClazz = RouterPath.MainActivity.getDestinationClass()
        if (!isActivityAlive(mainClazz)) {
            val context = currentActivity() ?: BaseApplication.instance.applicationContext
            TheRouter.build(RouterPath.MainActivity)
                .withOptionsCompat(context.getNoneOptions()?.toBundle())
                .navigation()
        }
        block.invoke()
    }

    /**
     * 保证首页 MainActivity 始终存活的前提下，关闭「非指定排除列表」的页面
     */
    fun ensureMainActivityAliveWithFallback(path: String, block: () -> Unit) {
        // 获取跳转的class
        val clazz = path.getDestinationClass()
        // 排除的页面
        val excludedList = arrayListOf(clazz)
        // 当前app不登录也可以进入首页,故而首页作为一整个app的底座,是必须存在的
        if (path != RouterPath.MainActivity) {
            excludedList.add(RouterPath.MainActivity.getDestinationClass())
        }
        // 保证首页存活
        ensureMainActivityAliveWithFallback {
            // 执行跳转对应页面
            if (path != RouterPath.MainActivity) {
                block.invoke()
            }
            // 延迟关闭,避免动画叠加(忽略需要跳转的页面)
            ProcessLifecycleOwner.get().schedule({
                finishNotTargetActivity(*excludedList.toTypedArray())
            }, 500)
        }
    }

}