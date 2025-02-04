package com.example.common.utils.manager

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Process
import com.example.common.BaseApplication
import java.util.*
import kotlin.system.exitProcess

/**
 * description 管理App中Activity的类
 * author yan
 */
object AppManager {
    private val activityStack = Stack<Activity>()//存储activity栈
    val stackCount get() = activityStack.size//当前栈内所有的activity总数

    /**
     * 循环所有栈内Activity
     */
    fun forEach(func: Activity.() -> Unit) {
        try {
            synchronized(activityStack) { activityStack.forEach(func) }
        } catch (_: Exception) {
        }
    }

    /**
     * 添加Activity到容器中
     */
    fun addActivity(activity: Activity) {
        if (activityStack.size > 0) {
            if (!activityStack.contains(activity)) activityStack.push(activity)
        } else {
            activityStack.push(activity)
        }
        checkStack()
    }

    /**
     * 获取当前activity
     */
    fun currentActivity(): Activity? {
        return try {
            synchronized(activityStack) { activityStack.lastElement() }
        } catch (e: Exception) {
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
     * 移除指定的Activity
     */
    fun removeActivity(activity: Activity?) {
        if (activity != null) {
            activityStack.remove(activity)
        }
    }

    /**
     * 结束指定的Activity
     */
    fun finishActivity(activity: Activity?) {
        if (activity != null) {
            activityStack.remove(activity)
            activity.finish()
        }
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
    fun finishActivityClass(cls: Class<*>) {
        try {
            synchronized(activityStack) {
                activityStack.filter { it.javaClass == cls }
            }.forEach {
                finishActivity(it)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 结束非指定类名的Activity
     */
    fun finishNotTargetActivity(vararg cls: Class<*>) {
        try {
            synchronized(activityStack) {
                activityStack.filter { it.javaClass !in cls }
            }.forEach {
                finishActivity(it)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 结束指定类名的Activity
     */
    fun finishTargetActivity(vararg cls: Class<*>) {
        try {
            synchronized(activityStack) {
                activityStack.filter { it.javaClass in cls }
            }.forEach {
                finishActivity(it)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 结束所有Activity
     */
    fun finishAll() {
        try {
            while (activityStack.isNotEmpty()) {
                activityStack.pop()?.finish()
            }
        } catch (_: Exception) {
        }
    }

    /**
     * 遍历所有Activity并finish
     */
    fun finishAllForEach() {
        try {
            if (activityStack.size > 0) {
                synchronized(activityStack) {
                    activityStack.forEach { it.finish() }
                }
            }
        } catch (_: Exception) {
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
        } catch (_: Exception) {
        }
    }

    /**
     * 判断Activity是否存在
     */
    fun isExistActivity(vararg cls: Class<*>): Boolean {
        return try {
            synchronized(activityStack) {
                activityStack.find { it.javaClass in cls }
            } != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 判断Activity是否存在
     * p层调用
     */
    fun isExistOtherActivity(thisActivity: Any, vararg cls: Class<*>): Boolean {
        return try {
            synchronized(activityStack) {
                activityStack.find {
                    (it != thisActivity) && (it.javaClass in cls)
                }
            } != null
        } catch (e: Exception) {
            false
        }
    }

}