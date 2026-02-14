package com.example.common.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.transition.Fade
import android.transition.Slide
import android.transition.Visibility
import android.view.Gravity
import android.view.Gravity.BOTTOM
import android.view.Gravity.LEFT
import android.view.Gravity.RIGHT
import android.view.Gravity.TOP
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.ColorRes
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.common.R
import com.example.common.base.BasePopupWindow.Companion.PopupAnimType.ALPHA
import com.example.common.base.BasePopupWindow.Companion.PopupAnimType.NONE
import com.example.common.base.BasePopupWindow.Companion.PopupAnimType.TRANSLATE
import com.example.common.base.bridge.BaseImpl
import com.example.common.utils.ScreenUtil.screenHeight
import com.example.common.utils.function.pt
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.view.background
import com.example.framework.utils.function.view.doOnceAfterLayout
import com.example.framework.utils.function.view.layoutGravity
import com.example.framework.utils.function.view.size
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType

/**
 * Created by WangYanBin on 2020/7/13.
 * 所有弹框的基类 (用于实现上下左右弹出的效果，如有特殊动画需求，重写setAnimation方法/默认底部弹出样式配置需要页面重写监听setOnWindowInsetsChanged,每次改变时候调用setNavigationBar,且不支持电池颜色修改)
 * 1) 由于PopupWindow设置了isClippingEnabled=false,故而会撑满整个手机屏幕变成全屏
 * 2) 可使用BaseBottomSheetDialogFragment替代底部弹出样式,不用重写setOnWindowInsetsChanged
 * 3) 左右弹出类似于原生DrawerLayout控件,做了于系统一致的底部导航栏高亮效果
 * <androidx.drawerlayout.widget.DrawerLayout
 *         android:id="@+id/drawer"
 *         android:layout_width="match_parent"
 *         android:layout_height="match_parent">
 *
 *         <FrameLayout
 *             android:layout_width="match_parent"
 *             android:layout_height="match_parent">
 *              .....
 *         </FrameLayout>
 *
 *         <include
 *             android:id="@+id/view_drawer"
 *             layout="@layout/view_deal_drawer"
 *             android:layout_width="340pt"
 *             android:layout_height="match_parent"
 *             android:layout_gravity="end" />
 *
 *     </androidx.drawerlayout.widget.DrawerLayout>
 */
@Suppress("LeakingThis", "UNCHECKED_CAST")
abstract class BasePopupWindow<VDB : ViewDataBinding>(private val activity: FragmentActivity, private val popupWidth: Int = MATCH_PARENT, private val popupHeight: Int = WRAP_CONTENT, private var popupAnimStyle: PopupAnimType = NONE, private val popupSlide: Int = BOTTOM, private val hasLight: Boolean = true) : PopupWindow(), BaseImpl {
    private var showJob: Job? = null
    private val window get() = activity.window
    private val layoutParams by lazy { window.attributes }
    private val isTranslate get() = popupAnimStyle == TRANSLATE
    // 项目框架采用enableEdgeToEdge,属于全屏展示,如果是底部弹出的弹框,给页面适配底部导航栏
    private val navigationBarView by lazy {
        View(context).apply {
            size(MATCH_PARENT, WRAP_CONTENT)
        }
    }
    private val parentView by lazy {
        LinearLayout(context).apply {
            size(MATCH_PARENT, WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            layoutGravity = BOTTOM
        }
    }
    protected var mBinding: VDB? = null
    protected val rootView get() = mBinding?.root
    protected val context: Context get() = activity
    protected val ownerActivity: Activity = activity
    protected val lifecycleOwner get() = ownerActivity as? LifecycleOwner

    companion object {
        private const val ANIM_DURATION = 300L // 动画时长
        private const val NAV_BAR_DELAY = 350L // 导航栏延迟设置时长

        /**
         * 内置常量集
         */
        enum class PopupAnimType {
            NONE, TRANSLATE, ALPHA
        }
    }

    init {
        initView(null)
        initEvent()
        initData()
    }

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun initView(savedInstanceState: Bundle?) {
        // 强制规定传入的动画如果是方向类型的,参数必须在规定范围内
        if (isTranslate) {
            val slideList = listOf(TOP, BOTTOM, LEFT, RIGHT)
            if (!slideList.contains(popupSlide)) {
                popupAnimStyle = NONE
            }
        }
        // 设置内部view
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            try {
                val vdbClass = type.actualTypeArguments[0] as? Class<VDB>
                val method = vdbClass?.getMethod("inflate", LayoutInflater::class.java)
                mBinding = method?.invoke(null, window.layoutInflater) as? VDB
                mBinding?.lifecycleOwner = lifecycleOwner
                if (isTranslate && popupSlide != TOP) {
                    parentView.addView(mBinding?.root)
                    parentView.addView(navigationBarView)
                    setContentView(parentView)
                } else {
                    mBinding?.root?.let { setContentView(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 基础交互属性配置
        width = if (popupWidth < 0) popupWidth else popupWidth.pt
        height = if (popupHeight < 0) popupHeight else popupHeight.pt
        isFocusable = true
        isOutsideTouchable = true
        // 完全撑满整个屏幕
        isClippingEnabled = false
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        // 如果是方向类型此时非左右弹出的情况下,需要即刻加载一下底层导航栏.避免动画遮罩
        if (isTranslate && popupSlide != TOP) {
            if (popupSlide == BOTTOM) setNavigationBarColor()
            ViewCompat.getRootWindowInsets(window.decorView)?.let {
                setNavigationBar(it)
            }
        }
        // 绑定宿主生命周期
        lifecycleOwner.doOnDestroy {
            mBinding?.unbind()
            mBinding = null
        }
    }

    override fun initEvent() {
        setAnimation()
        setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setOnDismissListener {
            if (hasLight) {
                layoutParams?.alpha = 1f
                window.attributes = layoutParams
            }
            if (isTranslate && popupSlide != TOP && popupSlide != BOTTOM) {
                setNavigationBarColor(R.color.bgTransparent)
            }
        }
    }

    /**
     * 默认底部弹出
     */
    private fun setAnimation() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val (enter, exit) = when (popupAnimStyle) {
                ALPHA -> Pair(
                    Fade().apply { duration = ANIM_DURATION; mode = Visibility.MODE_IN },
                    Fade().apply { duration = ANIM_DURATION; mode = Visibility.MODE_OUT }
                )
                TRANSLATE -> Pair(
                    Slide().apply { duration = ANIM_DURATION; mode = Visibility.MODE_IN; slideEdge = popupSlide },
                    Slide().apply { duration = ANIM_DURATION; mode = Visibility.MODE_OUT; slideEdge = popupSlide }
                )
                NONE -> null to null
            }
            enterTransition = enter
            exitTransition = exit
            // 确保在 API >= M 时也设置 animationStyle
            animationStyle = when (popupAnimStyle) {
                NONE -> -1
                else -> 0 // 使用 0 表示使用系统默认或不应用旧版动画
            }
//        } else {
//            animationStyle = when (popupAnimStyle) {
//                ALPHA -> R.style.PopupAlphaAnimStyle
//                TRANSLATE -> R.style.PopupTranslateAnimStyle
//                NONE -> -1
//            }
//        }
    }

    override fun initData() {
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="重写方法">
    /**
     * 基于锚点 View 显示 (强依赖 View 的上下文)
     */
    override fun showAsDropDown(anchor: View?) {
        showPopup({ super.showAsDropDown(anchor) }, ::checkShowAsDropDownConditions)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        showPopup({ super.showAsDropDown(anchor, xoff, yoff) }, ::checkShowAsDropDownConditions)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        showPopup({ super.showAsDropDown(anchor, xoff, yoff, gravity) }, ::checkShowAsDropDownConditions)
    }

    /**
     * 基于父容器 + 坐标显示 (不直接依赖某个锚点 View)
     */
    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        showPopup({ super.showAtLocation(parent, gravity, x, y) }, ::checkShowAtLocationConditions)
    }

//    private fun checkShowAsDropDownConditions() = Looper.myLooper() != null &&
//            Looper.myLooper() == Looper.getMainLooper() &&
//            rootView?.context != null &&
//            (rootView?.context as? Activity)?.isFinishing == false &&
//            (rootView?.context as? Activity)?.isDestroyed == false
//
//    private fun checkShowAtLocationConditions() = Looper.myLooper() != null &&
//            Looper.myLooper() == Looper.getMainLooper() &&
//            (context as? Activity)?.isFinishing == false &&
//            (context as? Activity)?.isDestroyed == false

    private fun checkShowAsDropDownConditions(): Boolean {
        return checkPopupShowConditions(rootView?.context)
    }

    private fun checkShowAtLocationConditions(): Boolean {
        return checkPopupShowConditions()
    }

    /**
     * 通用的弹窗显示前置校验方法
     * @param targetContext 可选的上下文（优先用传入的，没有则用成员变量context）
     * @return 是否满足显示条件
     */
    private fun checkPopupShowConditions(targetContext: Context? = null): Boolean {
        // 校验是否在主线程（PopupWindow必须在主线程操作）
        val mainLooper = Looper.getMainLooper()
        if (Looper.myLooper() != mainLooper) {
            return false
        }
        // 确定要校验的Context（优先用传入的，兜底用成员变量）
        val checkContext = targetContext ?: context
        // 校验Context是Activity且状态正常
        val activity = checkContext as? Activity
        return activity?.let {
            !it.isFinishing && !it.isDestroyed
        } ?: false
    }

    private fun showPopup(showFunction: () -> Unit, checkCondition: () -> Boolean) {
        if (checkCondition()) {
            try {
                setAttributes()
                showFunction.invoke()
                if (isTranslate && popupSlide != TOP && popupSlide != BOTTOM) {
                    showJob?.cancel()
                    showJob = lifecycleOwner?.lifecycleScope?.launch {
                        delay(NAV_BAR_DELAY)
                        setNavigationBarColor()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setAttributes() {
        if (hasLight) {
            layoutParams?.alpha = 0.7f
            window.attributes = layoutParams
        }
    }

    override fun dismiss() {
        if (!isShowing) return
        if ((context as? Activity)?.isFinishing.orFalse) return
        if ((context as? Activity)?.isDestroyed.orFalse) return
        if ((context as? Activity)?.window?.windowManager == null) return
        if (window.windowManager == null) return
        if (window.decorView.parent == null) return
        super.dismiss()
    }

    /**
     * 设置导航栏高度
     * 页面重写监听setOnWindowInsetsChanged,每次改变时候调用该方法
     */
    open fun setNavigationBar(insets: WindowInsetsCompat) {
        val navBarBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        if (navigationBarView.height != navBarBottom) {
            navigationBarView.size(height = navBarBottom)
        }
        if (popupHeight == MATCH_PARENT) {
            val nowHeight = screenHeight - navBarBottom
            if (mBinding?.root?.height != nowHeight) {
                mBinding?.root.size(height = nowHeight)
            }
        } else {
            val nowHeight = if (popupHeight < 0) popupHeight else popupHeight.pt + navBarBottom
            if (height != nowHeight) {
                height = nowHeight
            }
        }
    }

    /**
     * 设置导航栏颜色,初始化随页面,调用一次即可 (需要注意电池黑白无法改变)
     */
    open fun setNavigationBarColor(@ColorRes navigationBarColor: Int = getNavigationBarColor()) {
        navigationBarView.background(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) navigationBarColor else R.color.bgBlack)
    }

    /**
     * 获取导航栏颜色,可在基类重写
     */
    @ColorRes
    open fun getNavigationBarColor(): Int {
        return R.color.appNavigationBar
    }

    /**
     * 默认底部坐标展示
     */
    open fun show() {
        if (!isShowing) showAtLocation(rootView, BOTTOM, 0, 0)
    }

    /**
     * 控件上方显示(以v的中心位置/左边距->为开始位置)
     * 获取自身的长宽高
     * 一般情况：
     * 若 popupView 及其子视图的布局参数和内容都是固定的，在调用 measure 方法之后，measuredWidth 和 measuredHeight 能够反映出 PopupWindow 根视图确切的宽高。例如，popupView 是一个包含固定文本的 TextView 或者有固定尺寸的 ImageView 等，测量得到的宽高是准确的。
     * 特殊情况：
     * 依赖外部资源：要是 popupView 依赖于外部资源（如网络图片），在资源还未加载完成时进行测量，得到的宽高可能不准确。因为在资源加载完成之前，视图并不知道其最终的大小。
     * 布局依赖于父容器：如果 popupView 的布局依赖于父容器的大小或者其他动态因素，仅使用 View.MeasureSpec.UNSPECIFIED 进行测量可能无法得到确切的宽高。例如，popupView 中有一个 LinearLayout 其 layout_weight 属性生效，在这种情况下，需要根据实际的布局参数来创建合适的 MeasureSpec 进行测量。
     */
    open fun showUp(anchor: View?, center: Boolean = true) {
        if (!isShowing) {
            /**
             * Gravity.NO_GRAVITY->用于指定 PopupWindow 的对齐方式。Gravity.NO_GRAVITY 表示不使用任何默认的对齐方式，而是完全根据后面传入的 x 和 y 坐标来确定位置
             *
             * x->
             * 如果 center 为 true，则 x 坐标的计算方式是 (location[0] + anchor?.width.orZero / 2) - measuredWidth / 2。
             * 其中 location[0] 是 anchor 视图在屏幕上的 x 坐标，anchor?.width.orZero 是 anchor 视图的宽度（如果 anchor 为 null 则宽度为 0），
             * measuredWidth 是 PopupWindow 的根视图的宽度。这个计算的目的是将 PopupWindow 在水平方向上相对于 anchor 视图居中显示
             * 如果 center 为 false，则 x 坐标的计算方式是 location[0] - measuredWidth，即将 PopupWindow 的左侧与 anchor 视图的左侧对齐，然后根据 PopupWindow 的宽度进行偏移
             *
             * y->
             * y 坐标的计算是 location[1] - measuredHeight，其中 location[1] 是 anchor 视图在屏幕上的 y 坐标，
             * measuredHeight 是 PopupWindow 的根视图的高度。这个计算的目的是将 PopupWindow 显示在 anchor 视图的上方，偏移量为 PopupWindow 的高度。
             */
            rootView?.doOnceAfterLayout {
                val location = IntArray(2)
                anchor?.getLocationOnScreen(location)
                it.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                val measuredWidth = it.measuredWidth.orZero
                val measuredHeight = it.measuredHeight.orZero
                showAtLocation(anchor, Gravity.NO_GRAVITY, if (center) ((location[0] + anchor?.width.orZero / 2) - measuredWidth / 2) else ((location[0]) - measuredWidth / 2), location[1] - measuredHeight)
            }
        }
    }

    /**
     * 控件下方显示
     */
    open fun showDown(anchor: View?) {
        if (!isShowing) showAsDropDown(anchor)
    }
    // </editor-fold>

}