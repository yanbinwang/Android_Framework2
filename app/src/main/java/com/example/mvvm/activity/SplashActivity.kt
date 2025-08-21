package com.example.mvvm.activity

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.BaseApplication.Companion.lastClickTime
import com.example.common.base.BaseActivity
import com.example.common.config.ARouterPath
import com.example.common.utils.applyFullScreen
import com.example.common.utils.function.getFadePreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jessyan.autosize.internal.CancelAdapt

/**
 *  Created by wangyanbin
 *  app启动页
 *  1.安卓本身bug会在初次安装应用后点击图标再次拉起启动页，造成界面显示不不正常
 *  2.启动页去除autosize的兼容布局，改用dp绘制，因为启动app使用了自定义的dp背景，启动app时布局兼容并未启动会大小不一
 *  3.可采用TransitionTheme样式（纯白）或者继承该样式，将底色直接改为对应启动页的颜色，或者底色改为xml绘制的图片，可实现点击后瞬间加载页面，不至于黑屏（安卓进栈不重写会黑屏）
 *  4.可在当前页面调用app内全局需要的接口，比如用户信息/版本信息/服务器配置字段，自己实现一个假的2s+的倒计时，保证这些接口请求完成后再进去首页
 *
 *  启动页可以新增xml，里面价格有倒计时view来做app全局公用的请求处理（不成功不给进app）
 *  国内app需要告知书页面，因为splash页面的background使用了一个xml图片，所有在告知书页面需要延时关闭，不然会有页面残留
 *  launch {
 *  delay(500)
 *  activity.finish()
 *  }
 *  进入登录页或者首页的背景如果不是纯白色，可以继承TransitionTheme自己写一个对应颜色的样式
 *
 *  SystemClock.elapsedRealtime() 是 Android 系统中 SystemClock 类提供的一个方法。
 *  它返回的是自系统启动开始到调用该方法时所经过的时间，包含了系统处于睡眠状态的时间。也就是说，从设备开机（包括关机充电等情况）起，
 *  不管设备是处于正常运行、休眠还是其他状态，这个时间都会持续累加。该方法返回的时间单位是毫秒（ms
 */
@Route(path = ARouterPath.SplashActivity)
class SplashActivity : BaseActivity<Nothing>(), CancelAdapt {

    override fun isImmersionBarEnabled() = false

    override fun isBindingEnabled() = false

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        /**
         * 当用户从最近任务列表重启 App 时，系统可能会创建新的SplashActivity实例并置于已有任务栈顶部（而非复用根部实例），导致启动页重复显示。
         * 通过finish()销毁了这个多余的顶部实例，确保用户看到的是任务栈根部的页面，该逻辑在standard模式下是有效的
         */
        if (!isTaskRoot
            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
            && intent.action != null
            && intent.action == Intent.ACTION_MAIN
        ) {
            finish()
            return
        }
        window.applyFullScreen()
        //当前Activity不是任务栈的根，可能是通过其他Activity启动的
        if (!isTaskRoot) {
            jump()
        } else {
            //当前Activity是任务栈的根，执行相应逻辑
            initSplash()
        }
    }

    private fun initSplash() {
        launch {
            val SPLASH_DELAY = 2000L
            // 计算已经过去的时间
            val elapsedTime = SystemClock.elapsedRealtime() - lastClickTime
            // 计算还需要等待的时间
            val remainingTime = if (SPLASH_DELAY - elapsedTime < 0) {
                0
            } else {
                SPLASH_DELAY - elapsedTime
            }
            delay(remainingTime)
            jump()
        }
    }

    private fun jump() {
//        // 此时页面切换的间隙，窗口没有可显示的内容(启动页目前就是栈内最后一个页面,直接关闭会黑一下,然后才是拉起对应页面)
//        navigation(ARouterPath.MainActivity, options = getCustomOption(this, R.anim.set_alpha_in, R.anim.set_alpha_out))
//        // 延迟关闭启动页,解决黑屏问题
//        schedule(this, {
//            finish()
//        }, 500)
        navigation(ARouterPath.MainActivity, options = getFadePreview())
    }

    /**
     * 尝试解决启动页的dispatch闪退问题，这边切换成系统默认的dispatch逻辑
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            onUserInteraction()
        }
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else {
            onTouchEvent(ev)
        }
    }

}