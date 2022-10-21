package com.example.mvvm

import android.os.Looper
import android.util.Log
import com.example.base.BuildConfig
import com.example.common.BaseApplication
import com.example.mvvm.activity.MainActivity
import com.zxy.recovery.core.Recovery

/**
 * Created by WangYanBin on 2020/8/14.
 */
class MyApplication : BaseApplication() {

    companion object {
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initialize()
    }

    //初始化一些第三方控件和单例工具类等
    private fun initialize() {
        if (BuildConfig.ISDEBUG) {
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
    }

}