package com.example.common.utils.manager

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Process
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import com.example.common.BaseApplication
import com.example.common.R
import com.example.common.utils.function.getCustomOption
import com.example.framework.utils.builder.TimerBuilder.Companion.schedule
import java.util.Stack
import kotlin.system.exitProcess

/**
 * description 管理App中Activity的类
 * author yan
 */
object AppManager {
    private val activityStack = Stack<Activity>()//存储activity栈
    val stackCount get() = activityStack.size//当前栈内所有的activity总数

    /**
     * 获取当前activity
     */
    fun currentActivity(): Activity? {
        return try {
            synchronized(activityStack) {
                activityStack.lastElement()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取当前activity名称
     */
    val currentActivityName: String?
        get() {
            val am = BaseApplication.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val cn = am.getRunningTasks(1)[0].topActivity
            return cn?.shortClassName
        }

    /**
     * 循环所有栈内Activity
     */
    fun forEach(func: Activity.() -> Unit) {
        try {
            synchronized(activityStack) {
                activityStack.forEach(func)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 添加Activity到任务栈中
     * 需要注意，只是将页面添加进了自定义的任务栈，控制activity显示/关闭的是系统任务栈
     * 自定义栈 activityStack：顺序为 [A, B, C, D, E]，这是通过在 Activity D 里调用 addActivity(E) 得到的结果
     * 系统任务栈：顺序为 [A, B, C, D]，我们只是把 E 添加到了自定义栈，并没有调用 startActivity 方法将其添加到系统任务栈，所以 E 不在系统任务栈中
     */
    fun addActivity(activity: Activity?) {
        activity ?: return
        if (activityStack.isNotEmpty()) {
            if (!activityStack.contains(activity)) {
                activityStack.push(activity)
            }
        } else {
            activityStack.push(activity)
        }
        checkStack()
    }

    /**
     * 移除指定的Activity
     */
    fun removeActivity(activity: Activity?) {
        activity ?: return
        activityStack.remove(activity)
    }

    /**
     * 结束指定的Activity
     */
    fun finishActivity(activity: Activity?) {
        activity ?: return
        activityStack.remove(activity)
        activity.finish()
        checkStack()
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    fun finishCurrentActivity() {
        val activity = activityStack.pop()
        activityStack.remove(activity)
        activity.finish()
    }

    /**
     * 结束指定类名的Activity
     */
    fun finishActivityClass(cls: Class<*>?) {
        try {
            synchronized(activityStack) {
                activityStack.filter { it.javaClass == cls }
            }.forEach {
                finishActivity(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 结束非指定类名的Activity
     */
    fun finishNotTargetActivity(vararg cls: Class<*>?) {
        try {
            synchronized(activityStack) {
                activityStack.filter { it.javaClass !in cls }
            }.forEach {
                finishActivity(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 结束指定类名的Activity
     */
    fun finishTargetActivity(vararg cls: Class<*>?) {
        try {
            synchronized(activityStack) {
                activityStack.filter { it.javaClass in cls }
            }.forEach {
                finishActivity(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    /**
//     * 结束所有Activity，可通过application再次拉起
//     * 单项操作不加锁
//     */
//    fun finishAll() {
//        try {
//            while (activityStack.isNotEmpty()) {
//                activityStack.pop()?.finish()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }

    /**
     * 结束所有Activity，可通过application再次拉起
     * 加锁后多线程调取
     */
    fun finishAllActivity() {
        try {
            synchronized(activityStack) {
                val iterator = activityStack.iterator()
                while (iterator.hasNext()) {
                    val activity = iterator.next()
                    activity.finish()
                    iterator.remove()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 关闭所有页面，除了传入的指定页面，并且会拉起它
     */
    fun finishAllActivity(cls: Class<*>) {
        try {
            synchronized(activityStack) {
                // 关闭所有非指定类的Activity
                activityStack.filter { it.javaClass != cls }.forEach {
                    finishActivity(it)
                }
                // 检查指定类的Activity是否存在
                if (activityStack.find { it.javaClass == cls } == null) {
                    BaseApplication.instance.applicationContext?.let {
                        // 使用applicationContext启动 Activity 需要添加FLAG_ACTIVITY_NEW_TASK标志，否则会抛出异常
                        val intent = Intent(it, cls).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        it.startActivity(intent)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 遍历所有Activity并finish，不可通过application掉起
     */
    fun exitProcess() {
        try {
            if (activityStack.isNotEmpty()) {
                synchronized(activityStack) {
                    activityStack.forEach {
                        it.finish()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val nPid = Process.myPid()
        Process.killProcess(nPid)
        exitProcess(0)
    }

    /**
     * 检查stack防止内存泄漏
     */
    private fun checkStack() {
        try {
            synchronized(activityStack) {
                activityStack.filter { it.isDestroyed }
            }.forEach {
                activityStack.remove(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 判断Activity是否存在
     */
    fun isExistActivity(vararg cls: Class<*>?): Boolean {
        return try {
            synchronized(activityStack) {
                activityStack.find { it.javaClass in cls }
            } != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 判断除当前Activity外，是否有其余页面存在
     */
    fun isExistOtherActivity(thisActivity: Any, vararg cls: Class<*>?): Boolean {
        return try {
            synchronized(activityStack) {
                activityStack.find {
                    (it != thisActivity) && (it.javaClass in cls)
                }
            } != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}

/**
 * 页面如果在栈底,跳转拉起新页面的时候采用当前配置,过渡掉系统动画
 */
fun FragmentActivity?.getFadePreview(): ActivityOptionsCompat? {
    this ?: return null
    return getCustomOption(this, R.anim.set_alpha_in, R.anim.set_alpha_out).apply {
        schedule(this@getFadePreview, {
            finishAfterTransition()
        }, 500)
    }
}