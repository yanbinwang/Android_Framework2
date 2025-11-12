package com.example.common.utils.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Process
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.BaseApplication
import com.example.common.base.page.Extra
import com.example.common.base.page.getNoneOptions
import com.example.common.base.page.getPostcardClass
import com.example.common.config.ARouterPath
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

    /**
     * 当前栈内所有的 Activity 总数
     */
    val dequeCount: Int
        get() {
            return synchronized(LOCK) { activityDeque.size }
        }

    /**
     * 获取当前栈顶Activity全称（包含包名路径,优化API兼容性）
     */
    val currentActivityName: String?
        get() {
            val activity = currentActivity() ?: return null
            // 拼接 应用包名 + localClassName，得到完整类名（等价于原 shortClassName）
            return "${activity.packageName}.${activity.localClassName}"
        }

    /**
     * 获取当前栈顶Activity（非销毁状态）
     */
    fun currentActivity(): Activity? {
        return synchronized(LOCK) {
            // 从栈顶开始查找未销毁的Activity
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
     * 遍历所有存活的Activity
     * 批量关闭 ->
     * AppManager.forEachAlive {
     *     if (!isFinishing && !isDestroyed) {
     *         finish()
     *     }
     * }
     */
    fun forEachAlive(func: Activity.() -> Unit) {
        synchronized(LOCK) {
            activityDeque
                .mapNotNull { it.get() }
                .filter { !it.isDestroyed && !it.isFinishing }
                .forEach(func)
        }
    }

    /**
     * 添加Activity到任务栈中
     * 需要注意，只是将页面添加进了自定义的任务栈，控制Activity显示/关闭的是系统任务栈
     * 自定义栈 activityDeque：顺序为 [A, B, C, D, E]，这是通过在 Activity D 里调用 addActivity(E) 得到的结果
     * 系统任务栈：顺序为 [A, B, C, D]，我们只是把 E 添加到了自定义栈，并没有调用 startActivity 方法将其添加到系统任务栈，所以 E 不在系统任务栈中
     */
    fun addActivity(activity: Activity?) {
        activity ?: return
        synchronized(LOCK) {
            // 先移除已存在的相同实例，避免重复
            activityDeque.removeAll { it.get() == activity }
            activityDeque.add(WeakReference(activity))
            // 同步清理已销毁的引用
            cleanDestroyedActivities()
        }
    }

    /**
     * 移除指定的Activity
     */
    fun removeActivity(activity: Activity?) {
        activity ?: return
        synchronized(LOCK) {
            activityDeque.removeAll { it.get() == activity }
        }
    }

    /**
     * 结束指定的Activity
     */
    fun finishActivity(activity: Activity?) {
        activity ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        try {
            synchronized(LOCK) {
                activityDeque.removeAll { it.get() == activity }
            }
            activity.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    fun finishCurrentActivity() {
        currentActivity()?.let { finishActivity(it) }
    }

    /**
     * 结束指定类的所有Activity
     */
    fun finishActivitiesOfClass(cls: Class<*>?) {
        if (cls == null) return
        synchronized(LOCK) {
            activityDeque
                .mapNotNull { it.get() }
                .filter { it.javaClass == cls }
                .forEach { activity ->
                    // 复用已加try/catch的finishActivity
                    finishActivity(activity)
                }
        }
    }

    /**
     * 结束非指定类名的Activity（保留指定类）
     * @param cls 要保留的Activity类（可变参数，支持多个类）
     */
    fun finishNotTargetActivity(vararg cls: Class<*>?) {
        try {
            synchronized(LOCK) {
                // 过滤cls数组中的null，得到“有效保留类列表”
                val validKeepClasses = cls.filterNotNull()
                // 特殊逻辑：若有效保留类为空（即传入的全是null），则结束所有存活Activity
                val shouldFinishAll = validKeepClasses.isEmpty()
                // 过滤出需要结束的Activity
                val activitiesToFinish = activityDeque
                    .mapNotNull { it.get() }
                    .filter { activity ->
                        // 条件：若需结束所有，则直接保留；否则排除“有效保留类”
                        (shouldFinishAll || activity.javaClass !in validKeepClasses) && !activity.isDestroyed && !activity.isFinishing
                    }
                    .toList()
                // 批量结束Activity
                activitiesToFinish.forEach { activity ->
                    finishActivity(activity)
                }
                // 清理无效引用
                cleanDestroyedActivities()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 结束指定类名的Activity
     * @param cls 要结束的Activity类（可变参数，支持多个类）
     */
    fun finishTargetActivity(vararg cls: Class<*>?) {
        try {
            synchronized(LOCK) {
                val validTargetClasses = cls.filterNotNull()
                if (validTargetClasses.isEmpty()) return
                val activitiesToFinish = activityDeque
                    .mapNotNull { it.get() }
                    .filter { activity ->
                        activity.javaClass in validTargetClasses && !activity.isDestroyed && !activity.isFinishing
                    }
                    .toList()
                activitiesToFinish.forEach { activity ->
                    finishActivity(activity)
                }
                cleanDestroyedActivities()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 结束所有Activity（保留核心逻辑，加锁保证线程安全），可通过application再次拉起
     */
    fun finishAllActivities() {
        try {
            synchronized(LOCK) {
                val activities = activityDeque.mapNotNull { it.get() }
                activityDeque.clear()
                activities.forEach { activity ->
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        activity.finish()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 关闭所有页面，除了传入的指定页面，并且会拉起它
     */
    fun finishAllAndLaunchActivity(cls: Class<*>?) {
        finishAllActivities()
        launchTargetActivity(cls)
    }

    /**
     * 结束除指定类外的所有Activity，若指定类不存在则启动
     */
    fun finishAllExcept(cls: Class<*>?) {
        cls ?: return
        try {
            synchronized(LOCK) {
                val targetActivity = activityDeque
                    .mapNotNull { it.get() }
                    .find { it.javaClass == cls && !it.isDestroyed && !it.isFinishing }
                val activitiesToFinish = activityDeque
                    .mapNotNull { it.get() }
                    .filter { it.javaClass != cls }
                activitiesToFinish.forEach { activity ->
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        activity.finish()
                    }
                }
                if (targetActivity == null) {
                    launchTargetActivity(cls)
                }
                cleanDestroyedActivities()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 启动指定Activity（优化上下文和Flag使用）
     */
    private fun launchTargetActivity(cls: Class<*>?) {
        cls ?: return
        val context = BaseApplication.instance
        try {
            val intent = Intent(context, cls).apply {
                // 补充FLAG_ACTIVITY_CLEAR_TASK，与finishAllActivities()配合，确保新Activity是根节点，
                // 强制系统清除目标任务栈中所有现有 Activity，确保新启动的 Activity 是 “唯一根节点”，避免极端情况下系统残留旧栈信息导致的动画或启动模式异常。
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 遍历所有Activity并finish，不可通过application掉起
     */
    fun exitApp() {
        finishAllActivities()
        // 获取当前进程的唯一LifecycleOwner,
        ProcessLifecycleOwner.get().lifecycleScope.launch(Main.immediate) {
            // 预留300毫秒,避免因页面未完全销毁就杀进程，导致资源释放不彻底
            delay(300)
            try {
                Process.killProcess(Process.myPid())
                // exitProcess(0)是Android隐藏API，替换为Java标准的System.exit(0)，兼容性更强
                // System.exit(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 清理栈中已销毁的Activity引用（避免弱引用未及时回收的残留）
     * 调用前确保已在synchronized(LOCK)块内
     */
    private fun cleanDestroyedActivities() {
//        synchronized(LOCK) {
        activityDeque.removeAll { ref ->
            val activity = ref.get()
            activity == null || activity.isDestroyed || activity.isFinishing
        }
//        }
    }

    /**
     * 判断指定类的Activity是否存在（存活状态）
     */
    fun isActivityAlive(cls: Class<*>?): Boolean {
        cls ?: return false
        synchronized(LOCK) {
            return activityDeque
                .mapNotNull { it.get() }
                .any { it.javaClass == cls && !it.isDestroyed && !it.isFinishing }
        }
    }

    /**
     * 判断除当前Activity外，是否存在指定类的Activity
     */
    fun isOtherActivityAlive(current: Activity, cls: Class<*>?): Boolean {
        cls ?: return false
        synchronized(LOCK) {
            return activityDeque
                .mapNotNull { it.get() }
                .any { it !== current && it.javaClass == cls && !it.isDestroyed && !it.isFinishing }
        }
    }

    /**
     * 重启app任务栈
     * 1.安卓12+如果当前任务栈为空的情况下,通过application拉起一个页面,写了动画也是无响应的
     * 2.通过和推送通知一样的处理,先拉起一个全屏透明的页面,然后跳转到对应配置的页面(其余页面全部关闭)
     */
    fun rebootTaskStackAndLaunchTarget(className: String? = ARouterPath.StartActivity) {
        ARouter.getInstance().build(ARouterPath.LinkActivity)
            .withString(Extra.SOURCE, "normal")
            .withString(Extra.ID, className)
            .navigation()
    }

    /**
     * app如果未登录也可以进首页,需要一个兜底逻辑
     * 1)确保任务栈内存在首页
     * 2)确保任务栈内至少存在一个页面
     */
    fun ensureMainActivityAliveWithFallback(context: Context, resp: () -> Unit) {
        val mainClazz = ARouterPath.MainActivity.getPostcardClass()
        if (!isActivityAlive(mainClazz)) {
            ARouter.getInstance().build(ARouterPath.MainActivity)
                .withOptionsCompat(context.getNoneOptions())
                .navigation()
        }
        resp.invoke()
    }

}