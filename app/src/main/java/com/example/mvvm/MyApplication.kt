package com.example.mvvm

import android.os.Looper
import android.util.Log
import com.example.common.BaseApplication
import com.example.common.config.CacheData.deviceToken
import com.example.common.utils.function.toJsonString
import com.example.framework.utils.function.value.isDebug
import com.example.framework.utils.function.value.toArray
import com.example.framework.utils.logE
import com.example.framework.utils.logWTF
import com.example.home.activity.LinkActivity
import com.example.mvvm.activity.MainActivity
import com.example.thirdparty.firebase.FireBaseUtil
import com.example.thirdparty.firebase.NotificationUtil
import com.zxy.recovery.core.Recovery

/**
 * Created by WangYanBin on 2020/8/14.
 */
class MyApplication : BaseApplication() {

    companion object {
        val instance: MyApplication
            get() = BaseApplication.instance as MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    //初始化一些第三方控件和单例工具类等
    private fun initialize() {
        if (isDebug) {
            //debug	是否开启debug模式
            //recoverInBackground 当应用在后台时发生Crash，是否需要进行恢复
            //recoverStack	是否恢复整个Activity Stack，否则将恢复栈顶Activity
            //mainPage	回退的界面
            //callback	发生Crash时的回调
            //silent	SilentMode	是否使用静默恢复，如果设置为true的情况下，那么在发生Crash时将不显示RecoveryActivity界面来进行恢复，而是自动的恢复Activity的堆栈和数据，也就是无界面恢复
            Recovery.getInstance()
                .debug(true)
                .recoverInBackground(false)
                .recoverStack(true)
                .mainPage(MainActivity::class.java)
                .recoverEnabled(true)//发布版本不跳转
//                .callback(new MyCrashCallback())
                .silent(false, Recovery.SilentMode.RECOVER_ACTIVITY_STACK)
//                .skip(TestActivity.class)
                .init(this)
        } else {
            //当前若是发布包，接管系统loop，让用户感知不到程序闪退
            while (true) {
                try {
                    Looper.loop()
                } catch (e: Throwable) {
                    println("AppCatch -${Log.getStackTraceString(e)}")
                }
            }
        }
        //通知类/firebase初始化
        initFireBase()
    }

    private fun initFireBase() {
        NotificationUtil.init()
        FireBaseUtil.notificationIntentGenerator = { ctx, map ->
            "收到firebase\nmap:${map.toJsonString()}".logWTF
            LinkActivity.byPush(instance, *map.toArray { it.key to it.value })
        }
        FireBaseUtil.tokenRefreshListener = {
            deviceToken.set(it)
            "firebase token $it".logE
        }
        FireBaseUtil.refreshToken()
        FireBaseUtil.initTestReport()
    }

}