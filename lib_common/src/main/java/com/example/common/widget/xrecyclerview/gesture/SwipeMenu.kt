package com.example.common.widget.xrecyclerview.gesture

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.withStyledAttributes
import com.example.common.R
import com.example.framework.utils.function.value.orFalse
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.toSafeDouble
import com.example.framework.utils.function.value.toSafeInt
import kotlin.math.abs
import kotlin.math.max

/**
 * 仿IOS侧滑
 * <com.example.SwipeMenu
 *     android:id="@+id/swipeMenuView"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:swipeEnable="true"
 *     app:swipeIos="true"
 *     app:swipeLeft="true">
 *     <!-- 内容视图（必选，第一个子 View） -->
 *     <LinearLayout
 *         android:id="@+id/contentView"
 *         android:layout_width="match_parent"
 *         android:layout_height="wrap_content"
 *         android:background="@color/white"
 *         android:padding="16dp">
 *
 *         <TextView
 *             android:id="@+id/itemTitle"
 *             android:layout_width="wrap_content"
 *             android:layout_height="wrap_content"
 *             android:text="Item Content"
 *             android:textSize="16sp" />
 *
 *     </LinearLayout>
 *     <!-- 右侧菜单视图（可选，可添加多个子 View 作为菜单按钮） -->
 *     <TextView
 *         android:id="@+id/deleteBtn"
 *         android:layout_width="80dp"
 *         android:layout_height="match_parent"
 *         android:background="@color/red"
 *         android:gravity="center"
 *         android:text="删除"
 *         android:textColor="@color/white" />
 *
 *     <TextView
 *         android:id="@+id/starBtn"
 *         android:layout_width="80dp"
 *         android:layout_height="match_parent"
 *         android:background="@color/blue"
 *         android:gravity="center"
 *         android:text="置顶"
 *         android:textColor="@color/white" />
 *
 * </com.example.SwipeMenuView>
 */
class SwipeMenu @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {
    /**
     * 仿QQ，侧滑菜单展开时，点击除侧滑菜单之外的区域，关闭侧滑菜单
     * 增加一个布尔值变量，dispatch函数里，每次down时，为true，move时判断，如果是滑动动作，设为false
     * 在Intercept函数的up时，判断这个变量，如果仍为true 说明是点击事件，则关闭菜单
     */
    private var isUnMoved = true
    private var isUserSwiped = false
    // IOS类型下，是否拦截事件的flag
    private var iosInterceptFlag = false
    // 防止多只手指一起滑flag 在每次down里判断，touch事件结束清空
    private var isTouching = false
    // 代表当前是否是展开状态
    private var isExpand = false
    // 右滑删除功能的开关，默认开
    private var isSwipeEnable = true
    // IOS、QQ式交互，默认开
    private var isIos = true
    // 左滑右滑的开关,默认左滑打开菜单，默认开
    private var isLeftSwipe = true
    // 为了处理单击事件的冲突
    private var mScaleTouchSlop = 0
    // 计算滑动速度用
    private var mMaxVelocity = 0
    // 多点触摸只算第一根手指的速度
    private var mPointerId = 0
    // 自己的高度
    private var mHeight = 0
    // 右侧菜单宽度总和(最大滑动距离)
    private var mRightMenuWidths = 0
    // 滑动判定临界值（右侧菜单宽度的40%） 手指抬起时，超过了展开，没超过收起menu
    private var mLimit = 0
    // 存储contentView(第一个View)
    private var mContentView: View? = null
    // 存储的是当前正在展开的View
    private var mViewCache: SwipeMenu? = null
    // 滑动速度变量
    private var mVelocityTracker: VelocityTracker? = null
    // 平滑展开
    private var mExpandAnim: ValueAnimator? = null
    // 平滑关闭
    private var mCloseAnim: ValueAnimator? = null
    /**
     * 判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件
     * up-down的坐标，判断是否是滑动，如果是，则屏蔽一切点击事件
     */
    private val mFirstP by lazy { PointF() }
    private val mLastP by lazy { PointF() }

    init {
        mScaleTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        mMaxVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
        context.withStyledAttributes(attrs, R.styleable.SwipeMenu) {
            isSwipeEnable = getBoolean(R.styleable.SwipeMenu_swipeEnable, true)
            isIos = getBoolean(R.styleable.SwipeMenu_swipeIos, true)
            isLeftSwipe = getBoolean(R.styleable.SwipeMenu_swipeLeft, true)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 令自己可点击，从而获取触摸事件
        isClickable = true
        // 由于ViewHolder的复用机制，每次这里要手动恢复初始值
        mRightMenuWidths = 0
        mHeight = 0
        // 适配GridLayoutManager，将以第一个子Item(即ContentItem)的宽度为控件宽度
        var contentWidth = 0
        // 为了子View的高，可以matchParent(参考的FrameLayout 和LinearLayout的Horizontal)
        val measureMatchParentChildren = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY
        var isNeedMeasureChildHeight = false
        for (i in 0..<childCount) {
            val childView = getChildAt(i)
            // 令每一个子View可点击，从而获取触摸事件
            childView.isClickable = true
            if (childView.visibility != GONE) {
                // 后续计划加入上滑、下滑，则将不再支持Item的margin
                measureChild(childView, widthMeasureSpec, heightMeasureSpec)
                val lp = childView.layoutParams as MarginLayoutParams
                mHeight = max(
                    mHeight.toSafeDouble(),
                    childView.measuredHeight.toSafeDouble()
                ).toSafeInt()
                if (measureMatchParentChildren && lp.height == LayoutParams.MATCH_PARENT) {
                    isNeedMeasureChildHeight = true
                }
                // 第一个布局是Left item，从第二个开始才是RightMenu
                if (i > 0) {
                    mRightMenuWidths += childView.measuredWidth
                } else {
                    mContentView = childView
                    contentWidth = childView.measuredWidth
                }
            }
        }
        setMeasuredDimension(paddingLeft + paddingRight + contentWidth, mHeight + paddingTop + paddingBottom) //宽度取第一个Item(Content)的宽度
        // 滑动判断的临界值
        mLimit = mRightMenuWidths * 4 / 10
        // 如果子View的height有MatchParent属性的，设置子View高度
        if (isNeedMeasureChildHeight) {
            forceUniformHeight(childCount, widthMeasureSpec)
        }
    }

    /**
     * 给MatchParent的子View设置高度
     *
     * @param count
     * @param widthMeasureSpec
     */
    private fun forceUniformHeight(count: Int, widthMeasureSpec: Int) {
        // 以父布局高度构建一个Exactly的测量参数
        val uniformMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
        for (i in 0..<count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as MarginLayoutParams
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    // measureChildWithMargins 这个函数会用到宽，所以要保存一下
                    val oldWidth = lp.width
                    lp.width = child.measuredWidth
                    // Remeasure with new dimensions
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0)
                    lp.width = oldWidth
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childCount = childCount
        var left = 0 + paddingLeft
        var right = 0 + paddingLeft
        for (i in 0..<childCount) {
            val childView = getChildAt(i)
            if (childView.visibility != GONE) {
                // 第一个子View是内容 宽度设置为全屏
                if (i == 0) {
                    childView.layout(left, paddingTop, left + childView.measuredWidth, paddingTop + childView.measuredHeight)
                    left += childView.measuredWidth
                } else {
                    if (isLeftSwipe) {
                        childView.layout(left, paddingTop, left + childView.measuredWidth, paddingTop + childView.measuredHeight)
                        left += childView.measuredWidth
                    } else {
                        childView.layout(right - childView.measuredWidth, paddingTop, right, paddingTop + childView.measuredHeight)
                        right -= childView.measuredWidth
                    }
                }
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (isSwipeEnable) {
            acquireVelocityTracker(ev)
            val verTracker = mVelocityTracker
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件
                    isUserSwiped = false
                    // 仿QQ，侧滑菜单展开时，点击内容区域，关闭侧滑菜单
                    isUnMoved = true
                    // 每次DOWN时，默认是不拦截的
                    iosInterceptFlag = false
                    // 如果有别的指头摸过了，那么就return false。这样后续的move..等事件也不会再来找这个View了
                    if (isTouching) {
                        return false
                    } else {
                        // 第一个摸的指头，赶紧改变标志，宣誓主权
                        isTouching = true
                    }
                    mLastP[ev.rawX] = ev.rawY
                    // 判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件
                    mFirstP[ev.rawX] = ev.rawY
                    // 如果down，view和cacheview不一样，则立马让它还原。且把它置为null
                    if (mViewCache != null) {
                        if (mViewCache !== this) {
                            mViewCache?.smoothClose()
                            // IOS模式开启的话，且当前有侧滑菜单的View，且不是自己的，就该拦截事件咯
                            iosInterceptFlag = isIos
                        }
                        // 只要有一个侧滑菜单处于打开状态， 就不给外层布局上下滑动了
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                    // 求第一个触点的id， 此时可能有多个触点，但至少一个，计算滑动速率用
                    mPointerId = ev.getPointerId(0)
                }
                MotionEvent.ACTION_MOVE -> {
                    // IOS模式开启的话，且当前有侧滑菜单的View，且不是自己的，就该拦截事件咯。滑动也不该出现
                    if (iosInterceptFlag) {
                        return true
                    }
                    val gap = mLastP.x - ev.rawX
                    // 为了在水平滑动中禁止父类ListView等再竖直滑动
                    if (abs(gap.toDouble()) > 10 || abs(scrollX.toDouble()) > 10) { // 修改此处，使屏蔽父布局滑动更加灵敏
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                    // 仿QQ，侧滑菜单展开时，点击内容区域，关闭侧滑菜单。begin
                    if (abs(gap.toDouble()) > mScaleTouchSlop) {
                        isUnMoved = false
                    }
                    // 仿QQ，侧滑菜单展开时，点击内容区域，关闭侧滑菜单。end
                    scrollBy((gap).toInt(), 0) // 滑动使用scrollBy
                    // 越界修正
                    if (isLeftSwipe) { //左滑
                        if (scrollX < 0) {
                            scrollTo(0, 0)
                        }
                        if (scrollX > mRightMenuWidths) {
                            scrollTo(mRightMenuWidths, 0)
                        }
                    } else {
                        // 右滑
                        if (scrollX < -mRightMenuWidths) {
                            scrollTo(-mRightMenuWidths, 0)
                        }
                        if (scrollX > 0) {
                            scrollTo(0, 0)
                        }
                    }
                    mLastP[ev.rawX] = ev.rawY
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
                    if (abs((ev.rawX - mFirstP.x).toDouble()) > mScaleTouchSlop) {
                        isUserSwiped = true
                    }
                    // IOS模式开启的话，且当前有侧滑菜单的View，且不是自己的，就该拦截事件咯。滑动也不该出现
                    if (!iosInterceptFlag) { // 且滑动了 才判断是否要收起、展开menu
                        // 求伪瞬时速度
                        verTracker?.computeCurrentVelocity(1000, mMaxVelocity.toFloat())
                        val velocityX = verTracker?.getXVelocity(mPointerId).orZero
                        // 滑动速度超过阈值
                        if (abs(velocityX.toDouble()) > 1000) {
                            if (velocityX < -1000) {
                                // 左滑
                                if (isLeftSwipe) {
                                    // 平滑展开Menu
                                    smoothExpand()
                                } else {
                                    // 平滑关闭Menu
                                    smoothClose()
                                }
                            } else {
                                // 左滑
                                if (isLeftSwipe) {
                                    // 平滑关闭Menu
                                    smoothClose()
                                } else {
                                    //平滑展开Menu
                                    smoothExpand()
                                }
                            }
                        } else {
                            // 否则就判断滑动距离
                            if (abs(scrollX.toDouble()) > mLimit) {
                                // 平滑展开Menu
                                smoothExpand()
                            } else {
                                // 平滑关闭Menu
                                smoothClose()
                            }
                        }
                    }
                    // 释放
                    releaseVelocityTracker()
                    // 没有手指触碰
                    isTouching = false
                }
                else -> {}
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * @param event 向VelocityTracker添加MotionEvent
     * @see VelocityTracker.obtain
     * @see VelocityTracker.addMovement
     */
    private fun acquireVelocityTracker(event: MotionEvent?) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker?.addMovement(event)
    }

    /**
     * * 释放VelocityTracker
     *
     * @see VelocityTracker.clear
     * @see VelocityTracker.recycle
     */
    private fun releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker?.clear()
            mVelocityTracker?.recycle()
            mVelocityTracker = null
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        // 禁止侧滑时，点击事件不受干扰
        if (isSwipeEnable) {
            when (ev?.action) {
                // 屏蔽滑动时的事件
                MotionEvent.ACTION_MOVE -> {
                    if (abs((ev.rawX - mFirstP.x).toDouble()) > mScaleTouchSlop) {
                        return true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    // 为了在侧滑时，屏蔽子View的点击事件
                    if (isLeftSwipe) {
                        if (scrollX > mScaleTouchSlop) {
                            // 这里判断落点在内容区域屏蔽点击，内容区域外，允许传递事件继续向下的的
                            if (ev.x < width - scrollX) {
                                // 仿QQ，侧滑菜单展开时，点击内容区域，关闭侧滑菜单
                                if (isUnMoved) {
                                    smoothClose()
                                }
                                // true表示拦截
                                return true
                            }
                        }
                    } else {
                        if (-scrollX > mScaleTouchSlop) {
                            // 点击范围在菜单外 屏蔽
                            if (ev.x > -scrollX) {
                                // 仿QQ，侧滑菜单展开时，点击内容区域，关闭侧滑菜单
                                if (isUnMoved) {
                                    smoothClose()
                                }
                                return true
                            }
                        }
                    }
                    // 判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
                    if (isUserSwiped) {
                        return true
                    }
                }
            }
            // 模仿IOS 点击其他区域关闭
            if (iosInterceptFlag) {
                // IOS模式开启，且当前有菜单的View，且不是自己的 拦截点击事件给子View
                return true
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    /**
     * 每次ViewDetach的时候，判断一下 ViewCache是不是自己，如果是自己，关闭侧滑菜单，且ViewCache设置为null
     * 1) 防止内存泄漏(ViewCache是一个静态变量)
     * 2) 侧滑删除后自己后，这个View被Recycler回收，复用，下一个进入屏幕的View的状态应该是普通状态，而不是展开状态
     */
    override fun onDetachedFromWindow() {
        if (this === mViewCache) {
            mViewCache?.smoothClose()
            mViewCache = null
        }
        super.onDetachedFromWindow()
    }

    /**
     * 展开时，禁止长按
     */
    override fun performLongClick(): Boolean {
        if (abs(scrollX.toDouble()) > mScaleTouchSlop) {
            return false
        }
        return super.performLongClick()
    }

    fun smoothExpand() {
        // 展开就加入ViewCache：
        mViewCache = this
        // 侧滑菜单展开，屏蔽content长按
        if (null != mContentView) {
            mContentView?.isLongClickable = false
        }
        cancelAnim()
        mExpandAnim = ValueAnimator.ofInt(scrollX, if (isLeftSwipe) mRightMenuWidths else -mRightMenuWidths)
        mExpandAnim?.addUpdateListener { animation ->
            scrollTo((animation.animatedValue as Int), 0)
        }
        mExpandAnim?.interpolator = OvershootInterpolator()
        mExpandAnim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isExpand = true
            }
        })
        mExpandAnim?.setDuration(300)?.start()
    }

    /**
     * 每次执行动画之前都应该先取消之前的动画
     */
    private fun cancelAnim() {
        if (mCloseAnim != null && mCloseAnim?.isRunning.orFalse) {
            mCloseAnim?.cancel()
        }
        if (mExpandAnim != null && mExpandAnim?.isRunning.orFalse) {
            mExpandAnim?.cancel()
        }
    }

    /**
     * 平滑关闭
     */
    fun smoothClose() {
        mViewCache = null
        // 侧滑菜单展开，屏蔽content长按
        if (null != mContentView) {
            mContentView?.isLongClickable = true
        }
        cancelAnim()
        mCloseAnim = ValueAnimator.ofInt(scrollX, 0)
        mCloseAnim?.addUpdateListener { animation ->
            scrollTo((animation.animatedValue as Int), 0)
        }
        mCloseAnim?.interpolator = AccelerateInterpolator()
        mCloseAnim?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isExpand = false
            }
        })
        mCloseAnim?.setDuration(300)?.start()
    }

    /**
     * 快速关闭,用于 点击侧滑菜单上的选项,同时想让它快速关闭(删除 置顶)这个方法在ListView里是必须调用的
     * 在RecyclerView里，视情况而定，如果是mAdapter.notifyItemRemoved(pos)方法不用调用。
     */
    fun quickClose() {
        if (this === mViewCache) {
            // 先取消展开动画
            cancelAnim()
            // 关闭
            mViewCache?.scrollTo(0, 0)
            mViewCache = null
        }
    }

    /**
     * 设置侧滑功能开关
     */
    fun setSwipeEnable(swipeEnable: Boolean) {
        isSwipeEnable = swipeEnable
    }

    fun isSwipeEnable(): Boolean {
        return isSwipeEnable
    }

    /**
     * 设置是否开启IOS阻塞式交互 (回弹阻尼)
     */
    fun setIos(ios: Boolean): SwipeMenu {
        isIos = ios
        return this
    }

    fun isIos(): Boolean {
        return isIos
    }

    /**
     * 设置是否开启左滑出菜单，设置false 为右滑出菜单
     */
    fun setLeftSwipe(leftSwipe: Boolean): SwipeMenu {
        isLeftSwipe = leftSwipe
        return this
    }

    fun isLeftSwipe(): Boolean {
        return isLeftSwipe
    }

    /**
     * 返回ViewCache
     */
    fun getViewCache(): SwipeMenu? {
        return mViewCache
    }

}