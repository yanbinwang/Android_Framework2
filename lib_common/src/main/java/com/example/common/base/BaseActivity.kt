package com.example.common.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityOptionsCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.app.hubert.guide.NewbieGuide
import com.app.hubert.guide.listener.OnGuideChangedListener
import com.app.hubert.guide.listener.OnPageChangedListener
import com.app.hubert.guide.model.GuidePage
import com.example.common.R
import com.example.common.base.bridge.BaseImpl
import com.example.common.base.bridge.BaseView
import com.example.common.base.page.interf.TransparentOwner
import com.example.common.base.page.navigation
import com.example.common.event.Event
import com.example.common.event.EventBus
import com.example.common.network.socket.topic.WebSocketObserver
import com.example.common.utils.DataBooleanCache
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.ScreenUtil.screenWidth
import com.example.common.utils.function.registerResultWrapper
import com.example.common.utils.manager.AppManager
import com.example.common.utils.permission.PermissionHelper
import com.example.common.utils.removeNavigationBarDrawable
import com.example.common.utils.setNavigationBarDrawable
import com.example.common.utils.setNavigationBarLightMode
import com.example.common.utils.setStatusBarLightMode
import com.example.common.widget.dialog.AppDialog
import com.example.common.widget.dialog.LoadingDialog
import com.example.common.widget.textview.edittext.SpecialEditText
import com.example.framework.utils.builder.TimerBuilder
import com.example.framework.utils.function.color
import com.example.framework.utils.function.getIntent
import com.example.framework.utils.function.value.hasAnnotation
import com.example.framework.utils.function.value.isMainThread
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.SupervisorJob
import me.jessyan.autosize.AutoSizeCompat
import me.jessyan.autosize.AutoSizeConfig
import java.lang.reflect.ParameterizedType
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * Created by WangYanBin on 2020/6/3.
 * 对应页面传入继承自BaseViewModel的数据模型类，以及由系统生成的ViewDataBinding绑定类
 * 在基类中实现绑定，向ViewModel中注入对应页面的Activity和Context
 * 無xml的界面，泛型括號裡傳ViewDataBinding
 * 如果希望打开的页面有自定义的动画效果，可以重写oncreate，或者调取基类的initview方法
 * // 在需要转换动画的 Activity 中
 * @Override
 * protected void onCreate(Bundle savedInstanceState) {
 *     super.onCreate(savedInstanceState);
 * // 自定义滑入动画（从右侧进入）
 * Slide slide = new Slide(Gravity.END);
 * slide.setDuration(300);
 * getWindow().setEnterTransition(slide);
 * // 自定义滑出动画（向右侧退出）
 * Slide slideExit = new Slide(Gravity.START);
 * slideExit.setDuration(300);
 * 当 A 启动 B 时，A 被覆盖的过程	应用于 被启动的 Activity（B）
 * getWindow().setExitTransition(slideExit);
 * 当 B 返回 A 时，B 退出的过程	应用于 返回的 Activity（B）
 * getWindow().setReturnTransition(slideExit);
 * }
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseActivity<VDB : ViewDataBinding?> : AppCompatActivity(), BaseImpl, BaseView, CoroutineScope {
    protected var mBinding: VDB? = null
    protected var mSplashScreen: SplashScreen? = null
    protected val mClassName get() = javaClass.simpleName.lowercase(Locale.getDefault())
    protected val mResultWrapper = registerResultWrapper()
    protected val mActivityResult = mResultWrapper.registerResult { onActivityResultListener?.invoke(it) }
    protected val mDialog by lazy { AppDialog(this) }
    protected val mPermission by lazy { PermissionHelper(this) }
    private var onActivityResultListener: ((result: ActivityResult) -> Unit)? = null
    private var onWindowInsetsChanged: ((insets: WindowInsetsCompat) -> Unit)? = null
    private val immersionBar by lazy { ImmersionBar.with(this) }
    private val loadingDialog by lazy { LoadingDialog(this) }//刷新球控件，相当于加载动画
    private val dataManager by lazy { ConcurrentHashMap<MutableLiveData<*>, Observer<Any?>>() }
    private val job = SupervisorJob()//https://blog.csdn.net/chuyouyinghe/article/details/123057776
    override val coroutineContext: CoroutineContext get() = Main.immediate + job//加上SupervisorJob，提升协程作用域

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    companion object {
        var onFinishListener: OnFinishListener? = null
        var isAnyActivityStarting = false

        fun Context.startActivity(cls: Class<out Activity>, vararg pairs: Pair<String, Any?>) {
            startActivity(getIntent(cls, *pairs).apply {
                if (this@startActivity is Application) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            })
            if (BaseActivity::class.java.isAssignableFrom(cls)) isAnyActivityStarting = true
        }

        fun Activity.startActivityForResult(cls: Class<out Activity>, requestCode: Int, vararg pairs: Pair<String, Any?>) {
            startActivityForResult(getIntent(cls, *pairs), requestCode)
            if (BaseActivity::class.java.isAssignableFrom(cls)) isAnyActivityStarting = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * 在 Android 中，enableEdgeToEdge() 方法是在 API 29（Android 10） 及以上版本引入的，用于实现「边缘到边缘」（edge-to-edge）的显示效果（让内容延伸到状态栏和导航栏下方）。它的兼容性逻辑是：
         * 高版本（API 29+ (>=安卓10)）：正常生效，系统会自动处理内容与系统栏（状态栏、导航栏）的布局关系，实现内容渗透到系统栏区域。
         * 低版本（API < 29 (<=安卓9)）：该方法本质上是一个「空实现」（no-op），不会做任何操作。因为低版本 Android 系统本身不支持边缘到边缘的显示模式，系统栏（尤其是导航栏）会保持默认的不透明状态，内容不会渗透下去。
         * 该方法会默认将系统栏（状态栏、导航栏）设置为「透明 / 半透明」以实现内容延伸效果
         */
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (needTransparentOwner) {
            overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_none)
            requestedOrientation = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        AppManager.addActivity(this)
        WebSocketObserver.addObserver(this)
        isAnyActivityStarting = false
        if (isEventBusEnabled()) {
            EventBus.instance.register(this) {
                it.onEvent()
            }
        }
        if (isImmersionBarEnabled()) initImmersionBar()
        initView(savedInstanceState)
        initEvent()
        initData()
    }

    /**
     * 是否默认操作状态栏/导航栏属性
     */
    protected open fun isImmersionBarEnabled(): Boolean {
        return true
    }

    /**
     * 不需要binding的页面《》内传Nothing，并调取该方法
     */
    protected open fun isBindingEnabled(): Boolean {
        return true
    }

    /**
     * 不需要启动过渡页的页面调取该方法
     */
    protected open fun isSplashScreenEnabled(): Boolean {
        return false
    }

    /**
     * 1.如果当前设备支持状态栏字体变色，会设置状态栏字体为黑色/如果当前设备不支持状态栏字体变色，会使当前状态栏加上透明度，否则不执行透明度
     * 2.导航栏字体深色或亮色，只支持android O(api26)以上版本,背景在5.0+可设置,为兼顾最低6.0(23+)的手机,统一底部为白色
     *
     * ImmersionBar.with(this)
     *              .transparentStatusBar()  //透明状态栏，不写默认透明色
     *              .transparentNavigationBar()  //透明导航栏，不写默认黑色(设置此方法，fullScreen()方法自动为true)
     *              .transparentBar()             //透明状态栏和导航栏，不写默认状态栏为透明色，导航栏为黑色（设置此方法，fullScreen()方法自动为true）
     *              .statusBarColor(R.color.colorPrimary)     //状态栏颜色，不写默认透明色
     *              .navigationBarColor(R.color.colorPrimary) //导航栏颜色，不写默认黑色
     *              .barColor(R.color.colorPrimary)  //同时自定义状态栏和导航栏颜色，不写默认状态栏为透明色，导航栏为黑色
     *              .statusBarAlpha(0.3f)  //状态栏透明度，不写默认0.0f
     *              .navigationBarAlpha(0.4f)  //导航栏透明度，不写默认0.0F
     *              .barAlpha(0.3f)  //状态栏和导航栏透明度，不写默认0.0f
     *              .statusBarDarkFont(true)   //状态栏字体是深色，不写默认为亮色
     *              .navigationBarDarkIcon(true) //导航栏图标是深色，不写默认为亮色
     *              .autoDarkModeEnable(true) //自动状态栏字体和导航栏图标变色，必须指定状态栏颜色和导航栏颜色才可以自动变色哦
     *              .autoStatusBarDarkModeEnable(true,0.2f) //自动状态栏字体变色，必须指定状态栏颜色才可以自动变色哦
     *              .autoNavigationBarDarkModeEnable(true,0.2f) //自动导航栏图标变色，必须指定导航栏颜色才可以自动变色哦
     *              .flymeOSStatusBarFontColor(R.color.btn3)  //修改flyme OS状态栏字体颜色
     *              .fullScreen(true)      //有导航栏的情况下，activity全屏显示，也就是activity最下面被导航栏覆盖，不写默认非全屏
     *              .hideBar(BarHide.FLAG_HIDE_BAR)  //隐藏状态栏或导航栏或两者，不写默认不隐藏
     *              .addViewSupportTransformColor(toolbar)  //设置支持view变色，可以添加多个view，不指定颜色，默认和状态栏同色，还有两个重载方法
     *              .titleBar(view)    //解决状态栏和布局重叠问题，任选其一
     *              .titleBarMarginTop(view)     //解决状态栏和布局重叠问题，任选其一
     *              .statusBarView(view)  //解决状态栏和布局重叠问题，任选其一
     *              .fitsSystemWindows(true)    //解决状态栏和布局重叠问题，任选其一，默认为false，当为true时一定要指定statusBarColor()，不然状态栏为透明色，还有一些重载方法
     *              .supportActionBar(true) //支持ActionBar使用
     *              .statusBarColorTransform(R.color.orange)  //状态栏变色后的颜色
     *              .navigationBarColorTransform(R.color.orange) //导航栏变色后的颜色
     *              .barColorTransform(R.color.orange)  //状态栏和导航栏变色后的颜色
     *              .removeSupportView(toolbar)  //移除指定view支持
     *              .removeSupportAllView() //移除全部view支持
     *              .navigationBarEnable(true)   //是否可以修改导航栏颜色，默认为true
     *              .navigationBarWithKitkatEnable(true)  //是否可以修改安卓4.4和emui3.x手机导航栏颜色，默认为true
     *              .navigationBarWithEMUI3Enable(true) //是否可以修改emui3.x手机导航栏颜色，默认为true
     *              .keyboardEnable(true)  //解决软键盘与底部输入框冲突问题，默认为false，还有一个重载方法，可以指定软键盘mode
     *              .keyboardMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)  //单独指定软键盘模式
     *              .setOnKeyboardListener(new OnKeyboardListener() {    //软键盘监听回调，keyboardEnable为true才会回调此方法
     *                    @Override
     *                    public void onKeyboardChange(boolean isPopup, int keyboardHeight) {
     *                        LogUtils.e(isPopup);  //isPopup为true，软键盘弹出，为false，软键盘关闭
     *                    }
     *               })
     *              .setOnNavigationBarListener(onNavigationBarListener) //导航栏显示隐藏监听，目前只支持华为和小米手机
     *              .setOnBarListener(OnBarListener) //第一次调用和横竖屏切换都会触发，可以用来做刘海屏遮挡布局控件的问题
     *              .addTag("tag")  //给以上设置的参数打标记
     *              .getTag("tag")  //根据tag获得沉浸式参数
     *              .reset()  //重置所以沉浸式参数
     *              .init();  //必须调用方可应用以上所配置的参数
     */
    override fun initImmersionBar(statusBarDark: Boolean, navigationBarDark: Boolean, navigationBarColor: Int) {
        super.initImmersionBar(statusBarDark, navigationBarDark, navigationBarColor)
        window?.apply {
            setStatusBarLightMode(statusBarDark)
            setNavigationBarLightMode(navigationBarDark)
            setNavigationBarDrawable(navigationBarColor) {
                onWindowInsetsChanged?.invoke(it)
            }
        }
        immersionBar?.apply {
            reset()
            statusBarDarkFont(statusBarDark, 0.2f)
            navigationBarDarkIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) navigationBarDark else false, 0.2f)//edge会导致低版本ui深浅代码失效,但是会以传入的颜色值为主(偏深为白,反之为黑)
//            navigationBarColor(navigationBarColor)//颜色的配置在高版本上容易出问题,统一改为底部方法
            init()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        if (isSplashScreenEnabled()) {
            // 启动遮罩初始化操作(必须放在setContentView()之前)
            mSplashScreen = installSplashScreen()
        }
        if (isBindingEnabled()) {
            val type = javaClass.genericSuperclass
            if (type is ParameterizedType) {
                try {
                    val vdbClass = type.actualTypeArguments[0] as? Class<VDB>
                    val method = vdbClass?.getDeclaredMethod("inflate", LayoutInflater::class.java)
                    mBinding = method?.invoke(null, layoutInflater) as? VDB
                    mBinding?.lifecycleOwner = this
                    setContentView(mBinding?.root)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        ARouter.getInstance().inject(this)
    }

    override fun initEvent() {
    }

    override fun initData() {
    }

    override fun getResources(): Resources {
        //AutoSize的防止界面错乱的措施,同时确认其在主线程运行
        if (isMainThread) {
            AutoSizeConfig.getInstance()
                .setScreenWidth(screenWidth)
                .setScreenHeight(screenHeight)
            AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources())
        }
        return super.getResources()
    }

    override fun onStop() {
        super.onStop()
        AutoSizeConfig.getInstance().stop(this)
    }

    override fun onRestart() {
        super.onRestart()
        AutoSizeConfig.getInstance().restart()
    }

    override fun finish() {
        onFinishListener?.onFinish(this)
        super.finish()
        if (needTransparentOwner) overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        window?.removeNavigationBarDrawable()
        removeBackCallback()
        clearOnActivityResultListener()
        clearOnWindowInsetsChanged()
        AppManager.removeActivity(this)
        for ((key, value) in dataManager) {
            key.removeObserver(value)
        }
        dataManager.clear()
        mActivityResult.unregister()
        mBinding?.unbind()
        job.cancel()//之后再起的job无法工作
//        coroutineContext.cancelChildren()//之后再起的可以工作
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="页面管理方法">
    /**
     * ViewModel 中定义无值事件（用 Unit 替代 Any）
     * val reason by lazy { MutableLiveData<Unit>() } // 无值事件
     * Unit 类型的 value 是 Unit 实例（非 null），会触发回调
     */
    protected fun <T> MutableLiveData<T>?.observe(block: T.() -> Unit) {
        this ?: return
        val observer = Observer<Any?> { value ->
            if (value != null) {
                (value as? T)?.let { block(it) }
            }
        }
        dataManager[this] = observer
        observe(this@BaseActivity, observer)
    }

    /**
     * 保存当前注册的回调（用于移除旧回调）
     */
    private var backCallback: Any? = null
    protected fun setOnBackPressedListener(onBackPressedListener: (() -> Unit)) {
        // 移除旧回调，避免重复执行
        removeBackCallback()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+ 使用 OnBackInvokedCallback
            val callback = OnBackInvokedCallback {
                onBackPressedListener.invoke()
            }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback)
            backCallback = callback
        } else {
            // API <33 使用 OnBackPressedCallback
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressedListener.invoke()
                }
            }
            onBackPressedDispatcher.addCallback(this, callback)
            backCallback = callback
        }
    }

    /**
     * 移除当前注册的返回回调（恢复默认返回行为）
     */
    protected fun removeBackCallback() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                (backCallback as? OnBackInvokedCallback)?.let {
                    onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it)
                }
            }
            else -> {
                (backCallback as? OnBackPressedCallback)?.remove()
            }
        }
        backCallback = null
    }

    /**
     * 恢复默认返回行为（移除所有自定义回调）
     */
    protected fun restoreDefaultBackBehavior() {
        removeBackCallback()
    }

    /**
     * 实际开发中，严禁new一个activity，这是安卓框架所不允许的，故而跳转的回调也只能开给子类，但是fragment可以公开
     */
    protected fun setOnActivityResultListener(onActivityResultListener: ((result: ActivityResult) -> Unit)) {
        this.onActivityResultListener = onActivityResultListener
    }

    protected fun clearOnActivityResultListener() {
        onActivityResultListener = null
    }

    /**
     * 用于设置自定义Insets处理逻辑
     * var lastNavBarBottom = 0
     * var lastImeBottom = 0
     * var lastImeVisible = false
     * ViewCompat.setOnApplyWindowInsetsListener(decorView) { _, insets ->
     *     // 1. 精准获取导航栏底部高度（不受键盘影响）
     *     val navBarBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
     *     // 2. 精准获取软键盘高度及显示状态
     *     val imeType = WindowInsetsCompat.Type.ime()
     *     val isImeVisible = insets.isVisible(imeType)
     *     val imeBottom = if (isImeVisible) insets.getInsets(imeType).bottom else 0
     *     // 3. 只有当任意值变化时，才触发回调（避免频繁调用）
     *     if (navBarBottom != lastNavBarBottom
     *         || imeBottom != lastImeBottom
     *         || isImeVisible != lastImeVisible
     *     ) {
     *         lastNavBarBottom = navBarBottom
     *         lastImeBottom = imeBottom
     *         lastImeVisible = isImeVisible
     *         // 通过回调通知外部
     *         onInsetsChanged(navBarBottom, imeBottom, isImeVisible)
     *     }
     *     // 4. 不修改原始 insets，让系统正常分发给子视图（关键！）
     *     insets
     * }
     * // 场景1：处理键盘弹出/收起
     * if (isImeVisible) {
     * // 键盘显示：调整输入框位置，避免被键盘遮挡
     * editText.translationY = -imeBottom.toFloat()
     * } else {
     * // 键盘隐藏：恢复输入框位置
     * editText.translationY = 0f
     * }
     * // 场景2：适配导航栏高度（如底部按钮距离屏幕底部的距离）
     * bottomButton.setPadding(0, 0, 0, navBarBottom)
     * // 场景3：结合两者（如聊天界面，键盘弹出时同时考虑导航栏）
     * if (isImeVisible) {
     * // 键盘高度已包含导航栏时，可能需要减去导航栏高度
     * val actualKeyboardHeight = imeBottom - navBarBottom
     * messageList.setPadding(0, 0, 0, actualKeyboardHeight)
     *  }
     */
    protected fun setOnWindowInsetsChanged(onWindowInsetsChanged: (insets: WindowInsetsCompat) -> Unit) {
        this.onWindowInsetsChanged = onWindowInsetsChanged
    }

    protected fun clearOnWindowInsetsChanged() {
        onWindowInsetsChanged = null
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="软键盘相关">
    /**
     * 点击EditText之外的部分关闭软键盘
     */
    private var flagMove = false
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            ACTION_DOWN -> {
                flagMove = false
            }
            ACTION_MOVE -> {
                flagMove = true
            }
            ACTION_UP -> {
                if (!flagMove) {
                    val v = currentFocus
                    if (isShouldHideInput(v, ev)) {
                        clearEditTextFocus(v)
                        hideInputMethod(v)
                    }
                    return super.dispatchTouchEvent(ev)
                }
            }
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return if (window.superDispatchTouchEvent(ev)) {
            true
        } else {
            onTouchEvent(ev)
        }
    }

    protected fun hideInputMethod(v: View?) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(v?.windowToken, 0)
    }

    /**
     * 设置EditText失去焦点
     */
    private fun clearEditTextFocus(view: View?) {
        if (view != null && view is EditText) {
            view.clearFocus()
        }
    }

    /**
     * 判断是否应该隐藏软键盘
     */
    private fun isShouldHideInput(v: View?, event: MotionEvent): Boolean {
        if (v != null && (v is EditText || v is AppCompatEditText || v is SpecialEditText)) {
            val leftTop = intArrayOf(0, 0)
            val width: Int
            val height: Int
            //获取输入框当前的location位置
            val parent = v.findSpecialEditTextParent(5)
            if (parent != null) {
                parent.getLocationInWindow(leftTop)
                height = parent.height
                width = parent.width
            } else {
                v.getLocationInWindow(leftTop)
                height = v.height
                width = v.width
            }
            val left = leftTop[0]
            val top = leftTop[1]
            val bottom = top + height
            val right = left + width
            // 点击的是输入框区域，保留点击EditText的事件
            return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
        }
        return false
    }

    private fun View.findSpecialEditTextParent(maxTimes: Int): View? {
        var view = this
        for (i in 0..maxTimes) {
            if (view is SpecialEditText) return view
            view = view.parent as? View ?: return null
        }
        return null
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="订阅相关">
    protected open fun Event.onEvent() {
    }

    protected open fun isEventBusEnabled(): Boolean {
        return false
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="BaseView实现方法-初始化一些工具类和全局的订阅">
    override fun showDialog(flag: Boolean, second: Long, block: () -> Unit) {
        loadingDialog.apply { setDialogCancelable(flag) }.show()
        if (second > 0) {
            TimerBuilder.schedule(this, {
                hideDialog()
                block.invoke()
            }, second)
        }
    }

    override fun hideDialog() {
        loadingDialog.dismiss()
    }

    override fun showGuide(label: String, isOnly: Boolean, vararg pages: GuidePage, guideListener: OnGuideChangedListener?, pageListener: OnPageChangedListener?) {
        val labelTag = DataBooleanCache(label)
        if (!labelTag.get()) {
            if (isOnly) labelTag.set(true)
            val builder = NewbieGuide.with(this)//传入activity
                .setLabel(label)//设置引导层标示，用于区分不同引导层，必传！否则报错
                .setOnGuideChangedListener(guideListener)
                .setOnPageChangedListener(pageListener)
                .alwaysShow(true)
            for (page in pages) {
                page.backgroundColor = color(R.color.bgOverlay)//此处处理一下阴影背景
                builder.addGuidePage(page)
            }
            builder.show()
        }
    }

    override fun navigation(path: String, vararg params: Pair<String, Any?>?, options: ActivityOptionsCompat?): Activity? {
        navigation(path, params = params, activityResultValue = mActivityResult, options = options)
        return this
    }
    // </editor-fold>

}

//fun AppCompatActivity.launch(
//    context: CoroutineContext = EmptyCoroutineContext,
//    start: CoroutineStart = CoroutineStart.DEFAULT,
//    block: suspend CoroutineScope.() -> Unit
//) = lifecycleScope.launch(context, start, block)
//
//fun <T> AppCompatActivity.async(
//    context: CoroutineContext = EmptyCoroutineContext,
//    start: CoroutineStart = CoroutineStart.DEFAULT,
//    block: suspend CoroutineScope.() -> T
//) = lifecycleScope.async(context, start, block)

val BaseActivity<*>.needTransparentOwner get() = hasAnnotation(TransparentOwner::class.java)

interface OnFinishListener {
    fun onFinish(act: BaseActivity<*>)
}

interface OnCreateListener {
    fun onCreate(act: BaseActivity<*>)
}