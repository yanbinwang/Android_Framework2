package com.example.mvvm.activity

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.widget.ImageView
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.BaseApplication.Companion.lastClickTime
import com.example.common.base.BaseActivity
import com.example.common.base.page.getFadePreview
import com.example.common.config.ARouterPath
import com.example.common.utils.applyFullScreen
import com.example.common.utils.function.decodeDimensions
import com.example.common.utils.function.getTypedDrawable
import com.example.framework.utils.function.view.alpha
import com.example.framework.utils.function.view.margin
import com.example.framework.utils.function.view.size
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

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
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    // 高版本splash存在开关
    private var mKeepOn = AtomicBoolean(true)

    companion object {
        /**
         * 是否引入高版本启动页
         */
        private val isHighVersion get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        /**
         * 安卓12之前版本点击图标启动app需要配置一个xml,但是xml中的宽高到实时获取的宽高值时会有出入,增加获取文件的方法做校准
         */
        @JvmStatic
        fun adjustSplash(ivSplash: ImageView?) {
            if (!isHighVersion) {
                ivSplash?.apply {
                    val targetItemIndex = 1
                    val layerDrawable = context.getTypedDrawable<LayerDrawable>(R.drawable.layout_list_splash)
                    val bitmapDrawable = layerDrawable?.getDrawable(targetItemIndex) as? BitmapDrawable
                    val marginTopDp = layerDrawable?.getLayerInsetTop(targetItemIndex)
                    val dimensions = bitmapDrawable.decodeDimensions()
                    size(dimensions[0],dimensions[1])
                    margin(top = marginTopDp)
                }
            }
        }

    }

    override fun isImmersionBarEnabled() = false

    override fun isSplashScreenEnabled() = isHighVersion

    /**
     * style 中的 windowBackground 已经显示了启动图， setContentView 会重复绘制，导致闪烁
     */
    override fun isBindingEnabled() = isHighVersion

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        adjustSplash(mBinding?.ivSplash)
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
            if (isHighVersion) {
                /**
                 * 用来控制启动屏何时退出、让应用主内容显示的 “条件开关”，核心作用是 “阻塞启动屏自动消失，直到你指定的业务准备完成”
                 */
                mSplashScreen?.setKeepOnScreenCondition(object : SplashScreen.KeepOnScreenCondition {
                    override fun shouldKeepOnScreen(): Boolean {
                        return mKeepOn.get()
                    }
                })
                /**
                 * 展示完毕的监听方法
                 */
                mSplashScreen?.setOnExitAnimationListener(object : SplashScreen.OnExitAnimationListener {
                    override fun onSplashScreenExit(splashScreenViewProvider: SplashScreenViewProvider) {
                        // 整体启动view
                        val splashScreenView = splashScreenViewProvider.view
//                    // 启动屏中央的图标
//                    val iconView = splashScreenViewProvider.iconView
//                    PropertyAnimator(iconView, 500)
//                        .animateWidth(iconView.measuredWidth, 300.pt)
//                        .animateHeight(iconView.measuredHeight, 354.pt)
//                        .start(onEnd = {
//                            iconView.alpha(1f,0f,500){
//                                // 移除监听
//                                splashScreenViewProvider.remove()
//                                //当前Activity是任务栈的根，执行相应逻辑
//                                jump(true)
//                            }
//                        })
                        // 结束时做个渐隐藏动画,然后开始执行跳转
                        splashScreenView.alpha(1f, 0f, 500) {
                            // 移除监听
                            splashScreenViewProvider.remove()
                            //当前Activity是任务栈的根，执行相应逻辑
                            jump(true)
                        }
                    }
                })
                initSplash()
            } else {
                jump(true)
            }
        }
    }

    private fun initSplash() {
        launch {
            // splash只存在半秒
            delay(500)
            // Splash 展示完毕
            mKeepOn.set(false)
        }
    }

    private fun jump(isDelay: Boolean = false) {
        // 此时页面切换的间隙，窗口没有可显示的内容(启动页目前就是栈内最后一个页面,直接关闭会黑一下,然后才是拉起对应页面)
        val jumpAction = {
//            navigation(if (ConfigHelper.getPrivacyAgreed()) {
//                if (isLogin()) {
//                    ARouterPath.MainActivity
//                } else {
//                    ARouterPath.StartActivity
//                }
//            } else {
//                ARouterPath.LaunchActivity
//            }, options = getFadePreview())
            navigation(ARouterPath.MainActivity, options = getFadePreview())
        }
        if (isDelay) {
            launch {
                val SPLASH_DELAY = 2000L
                // 计算还需要等待的时间
                val remainingTime = if (isHighVersion) {
                    SPLASH_DELAY
                } else {
                    // 计算从进程创建（预览窗口开始显示）到当前的耗时（即预览窗口已显示的时间）
                    val previewElapsed = SystemClock.elapsedRealtime() - lastClickTime.get()
                    // 修正延迟时间：总2000ms - 预览已消耗时间，最小为0（避免负数）
                    maxOf(0, SPLASH_DELAY - previewElapsed)
                }
                delay(remainingTime)
                jumpAction()
            }
        } else {
            jumpAction()
        }
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
//@Route(path = ARouterPath.SplashActivity)
//class SplashActivity : BaseActivity<Nothing>(), CancelAdapt {
//
//    override fun isImmersionBarEnabled() = false
//
//    override fun isBindingEnabled() = false
//
//    override fun initView(savedInstanceState: Bundle?) {
//        super.initView(savedInstanceState)
//        /**
//         * 当用户从最近任务列表重启 App 时，系统可能会创建新的SplashActivity实例并置于已有任务栈顶部（而非复用根部实例），导致启动页重复显示。
//         * 通过finish()销毁了这个多余的顶部实例，确保用户看到的是任务栈根部的页面，该逻辑在standard模式下是有效的
//         */
//        if (!isTaskRoot
//            && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
//            && intent.action != null
//            && intent.action == Intent.ACTION_MAIN
//        ) {
//            finish()
//            return
//        }
//        window.applyFullScreen()
//        //当前Activity不是任务栈的根，可能是通过其他Activity启动的
//        if (!isTaskRoot) {
//            jump()
//        } else {
//            //当前Activity是任务栈的根，执行相应逻辑
//            initSplash()
//        }
//    }
//
//    private fun initSplash() {
//        launch {
//            val SPLASH_DELAY = 2000L
//            // 计算已经过去的时间
//            val elapsedTime = SystemClock.elapsedRealtime() - lastClickTime.get()
//            // 计算还需要等待的时间
//            val remainingTime = if (SPLASH_DELAY - elapsedTime < 0) {
//                0
//            } else {
//                SPLASH_DELAY - elapsedTime
//            }
//            delay(remainingTime)
//            jump()
//        }
//    }
//
//    private fun jump() {
////        // 此时页面切换的间隙，窗口没有可显示的内容(启动页目前就是栈内最后一个页面,直接关闭会黑一下,然后才是拉起对应页面)
////        navigation(ARouterPath.MainActivity, options = getCustomOption(this, R.anim.set_alpha_in, R.anim.set_alpha_out))
////        // 延迟关闭启动页,解决黑屏问题
////        schedule(this, {
////            finish()
////        }, 500)
//        navigation(ARouterPath.MainActivity, options = getFadePreview())
//    }
//
//    /**
//     * 尝试解决启动页的dispatch闪退问题，这边切换成系统默认的dispatch逻辑
//     */
//    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//        if (ev.action == MotionEvent.ACTION_DOWN) {
//            onUserInteraction()
//        }
//        return if (window.superDispatchTouchEvent(ev)) {
//            true
//        } else {
//            onTouchEvent(ev)
//        }
//    }
//
//}