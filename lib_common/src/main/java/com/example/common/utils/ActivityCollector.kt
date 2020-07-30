package com.example.common.utils

import android.app.Activity
import com.example.base.utils.LogUtil
import java.util.*

/**
 * Created by WangYanBin on 2020/6/22.
 * activity管理类
 */
object ActivityCollector {
    var activities: MutableList<Activity> = ArrayList()

    @JvmStatic
    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    @JvmStatic
    fun removeActivity(activity: Activity?) {
        activities.remove(activity)
    }

    @JvmStatic
    fun finishAll() {
        //可通过设置flag来清除其他activity，也可根据name保留最底部的activity不被关闭
        for (activity in activities) {
            if (!activity.isFinishing && !activity.localClassName.contains("MainActivity")) {
                activity.finish()
            }
        }
    }
}