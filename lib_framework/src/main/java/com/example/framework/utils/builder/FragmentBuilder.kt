package com.example.framework.utils.builder

import android.os.Bundle
import android.transition.ChangeBounds
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.getSimpleName
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import com.example.framework.utils.function.value.safeSize
import com.example.framework.utils.function.value.toBundle
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by wangyanbin
 * 页面切换管理工具类
 *
 * Activity
 * └── supportFragmentManager（Activity的FM，你Builder用的）
 *     ├─ HomeFragment
 *     │   └── childFragmentManager（Home自己的子FM）
 *     │       └─ ViewPager里面的子FragmentA / B
 *     ├─ ListFragment
 *     ├─ MineFragment
 *     └─ MessageFragment
 *
 *  记录下标：
 *  override fun recreate() {
 *    intent = Intent().apply { putExtra(Extras.TAB_INDEX, tabBottom.selectedTabPosition) }
 *    super.recreate()
 *  }
 *
 *  使用的页面需要重写：
 *  override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
 *  }
 *
 *  @SuppressLint("MissingSuperCall")
 *  override fun onSaveInstanceState(outState: Bundle) {
 *  }
 *
 *  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
 *    super.onRestoreInstanceState(savedInstanceState)
 *    intent.getIntExtra(Extras.TAB_INDEX, 0).also { navigationBuilder.selectedItem(it) }//注意selected
 *  }
 *
 *  1) 四个子 Fragment 是在 MainActivity 内部的
 *  2) 当前 Fragment 第一次加载时调取 onResume
 *  3) 二级页面被打开其后关闭，统一调取一次（栈内初始化了几个 Fragment 就回调几个）
 *  override fun onResume() {
 *    super.onResume()
 *    if (isHidden) return
 *    refreshNow()
 *  }
 *
 *  1) 当前 Fragment 第一次加载时不会被调取
 *  2) 使用 FragmentManager 切换时，栈内有几个 Fragment 就回调几个
 *  3) !hidden 表示当前可见
 *  override fun onHiddenChanged(hidden: Boolean) {
 *    super.onHiddenChanged(hidden)
 *    if (!hidden) refreshNow()
 *  }
 *
 *  private fun refreshNow() {
 *    viewModel?.refresh()
 *    helper.zendeskInfo()
 *  }
 *
 *  add 和 replace 区别
 *  1) 如果要在容器内加载一连串 fragment，它们使用同一个 xml 文件，只是 id 有区分，此时就可能出现 ui 错位
 *  2) 这时需使用 replace 删除容器之前的 fragment 直接替换（保证当前容器内只有一个 fragment）
 *
 * 【多实例使用约定】 commitFragmentWithSharedTransition/commitFragmentWithSharedEnterTransition
 * 1) 多个 FragmentBuilder 实例可以传入同一个 fragmentManager，但 containerViewId 必须区分
 * 2) useAddHideMode=true 的 Builder 用于 Tab 常驻切换（add/hide），该容器禁止调用任何replace事务
 * 3) useAddHideMode=false 的 Builder 仅用于二级详情 replace 跳转，不使用 fragmentCache、getFragment、tab下标相关能力
 * 4) replace 会销毁容器内全部已有 Fragment，不可和 add-hide Tab共用同一个容器 ID
 */
class FragmentBuilder(private val observer: LifecycleOwner, private val fragmentManager: FragmentManager, private val containerViewId: Int, private val useAddHideMode: Boolean = true) {
    private var isBundleMode = false // 是否是添加参数的模式
    private var enableAnimation = false // 是否执行动画
    private var currentItem = -1 // 默认下标 -> 不指定任何值
    private var managerLength = 0 // 当前需要管理的总长度
    private var commitJob: Job? = null // 切换协程
    private var animResList: MutableList<Int>? = null // 动画集合
    private var fragList: MutableList<Pair<Class<*>, String>>? = null // 普通模式 class 集合
    private var fragBundleList: MutableList<Triple<Class<*>, String, Bundle>>? = null // 参数模式 class 集合
    private var listener: ((tab: Int) -> Unit)? = null // 切换监听
    private val fragmentCache by lazy { ConcurrentHashMap<Int, Fragment>() } // 存储声明的 fragment

    init {
        observer.doOnDestroy {
            commitJob?.cancel()
            animResList?.clear()
            fragList?.clear()
            fragBundleList?.clear()
            fragmentCache.clear()
        }
    }

    /**
     *  HomeFragment::class.java.getBind()
     *  @param first：class 名
     *  @param second：tag 值，不传默认为 class 名
     */
    fun bind(list: List<Pair<Class<*>, String>>, default: Int = 0) {
        fragList = list.toMutableList()
        init(false, default)
    }

    fun bind(vararg data: Pair<Class<*>, String>, default: Int = 0) {
        bind(data.toList(), default)
    }

    /**
     * SceneListFragment::class.java.getBindBundle("Scene${i}", pairs = arrayOf(Extra.ID to i))
     * @param first：class名
     * @param second：内存中存储的tag
     * @param third：pair对象 （first，fragment透传的key second，透传的值）
     */
    fun bindBundle(list: List<Triple<Class<*>, String, Bundle>>, default: Int = 0) {
        fragBundleList = list.toMutableList()
        init(true, default)
    }

    fun bindBundle(vararg data: Triple<Class<*>, String, Bundle>, default: Int = 0) {
        bindBundle(data.toList(), default)
    }

    /**
     * 初始化配置
     */
    private fun init(hasBundle: Boolean, default: Int) {
        isBundleMode = hasBundle
        managerLength = if (hasBundle) fragBundleList.safeSize else fragList.safeSize
        fragmentCache.clear()
        commit(default)
    }

    /**
     * 切换 Tab
     * @param tab 目标下标
     * @param force true: 绕过校验强制切换；false: 默认，重复选择/下标越界直接return
     */
    fun commit(tab: Int, force: Boolean = false) {
        if (force) {
            commitNow(tab)
        } else {
            if (currentItem == tab || tab > managerLength - 1 || tab < 0) return
            commitNow(tab)
        }
    }

    private fun commitNow(tab: Int) {
        commitJob?.cancel()
        commitJob = observer.lifecycleScope.launch(Main.immediate) {
            currentItem = tab
            // FragmentTransaction 对象一次性，一旦 commit 之后就不能再操作
            val transaction = fragmentManager.beginTransaction()
            setupTransactionAnim(transaction)
            // 使现有的 fragment 全部隐藏
            fragmentCache.values.forEach {
                transaction.hide(it)
            }
            // 获取到选中的fragment
            val fragment = if (isBundleMode) {
                newInstanceArguments()
            } else {
                newInstance()
            }
            // 不为空的情况下，显示出来
            if (null != fragment) {
                // 视图未创建，同步执行之前 pending 的 add 事务，保证 fragment 已经 add，再执行 show 规避异常
                if (fragment.view == null) {
                    fragmentManager.executePendingTransactions()
                }
                transaction.show(fragment)
                /**
                 * commit()：如果宿主 Activity 已经保存状态（onSaveInstanceState 之后），直接抛异常崩溃
                 * commitAllowingStateLoss()：不抛异常，直接丢掉本次 Fragment 事务，不执行
                 */
                transaction.commitAllowingStateLoss()
                // 回调此时下标
                listener?.invoke(tab)
            }
        }
    }

    private fun newInstance(): Fragment? {
        return fragList.safeGet(currentItem)?.let {
            val (fragClass, tag) = it
            val transaction = fragmentManager.beginTransaction()
            setupTransactionAnim(transaction)
            var fragment = fragmentManager.findFragmentByTag(tag)
            if (null == fragment) {
                fragment = fragClass.getDeclaredConstructor().newInstance() as? Fragment
                fragment ?: return null
                addOrReplaceFragment(transaction, fragment, tag)
            }
            fragment
        }
    }

    private fun newInstanceArguments(): Fragment? {
        return fragBundleList.safeGet(currentItem)?.let {
            val (fragClass, tag, bundle) = it
            val transaction = fragmentManager.beginTransaction()
            setupTransactionAnim(transaction)
            var fragment = fragmentManager.findFragmentByTag(tag)
            if (null == fragment) {
                fragment = fragClass.getDeclaredConstructor().newInstance() as? Fragment
                fragment ?: return null
                fragment.arguments = bundle
                addOrReplaceFragment(transaction, fragment, tag)
            }
            fragment
        }
    }

    private fun setupTransactionAnim(transaction: FragmentTransaction) {
        // 只有 add 模式才设置动画（replace 模式禁用，避免闪退）
        if (!useAddHideMode) return
        // 设置动画（进入、退出、返回进入、返回退出）- >只有add这种保留原fragment在栈内的情况才会设置动画
        if (enableAnimation) {
            // 安全取值，没有为0的情况下就是默认不执行动画
            val enter = animResList.safeGet(0).orZero
            val exit = animResList.safeGet(1).orZero
            val popEnter = animResList.safeGet(2).orZero
            val popExit = animResList.safeGet(3).orZero
            // 根据长度判断设定的动画
            if (animResList.safeSize == 2) {
                transaction.setCustomAnimations(enter, exit)
            } else {
                transaction.setCustomAnimations(enter, exit, popEnter, popExit)
            }
        } else {
            transaction.setCustomAnimations(0, 0, 0, 0)
        }
    }

    private fun addOrReplaceFragment(transaction: FragmentTransaction, fragment: Fragment, tag: String?) {
        // add会将视图保存在栈内，适用于首页切换，replace会直接替换，如果子fragment列表要切换使用此方法，需要注意，replace使用后，动画就失效了
        if (useAddHideMode) {
            transaction.add(containerViewId, fragment, tag)
        } else {
            transaction.replace(containerViewId, fragment, tag)
        }
        transaction.commitAllowingStateLoss()
        // replace 栈内只有一个，集合也只存一个
        if (!useAddHideMode) {
            fragmentCache.clear()
        }
        fragmentCache[currentItem] = fragment
    }

    /**
     * 开启带共享元素的 Fragment 事务（普通replace切换，不加入回退栈）适合容器内直接替换 Fragment，没有返回栈需求
     * @param targetFragment 目标 Fragment 实例
     * @param reenterTransition 回退栈恢复时，Fragment 重新进入视图的内容过渡动画
     * @param exitTransition fragment被替换销毁，自身内容退出消失的过渡动画
     * @param sharedElementEnterTransition 共享元素进入过渡，默认 ChangeBounds
     * val slideTransition = Slide(Gravity.START)
     * slideTransition.setDuration(500)
     */
    fun commitFragmentWithSharedTransition(targetFragment: Fragment, reenterTransition: Any?, exitTransition: Any?, sharedElementEnterTransition: Any? = ChangeBounds()) {
        if (useAddHideMode) return
        // 页面回退时，当前 Fragment 重新进入视图的过渡动画
        targetFragment.reenterTransition = reenterTransition
        // 本 Fragment 被移除/被替换，退出消失的过渡动画
        targetFragment.exitTransition = exitTransition
        // 共享元素：从上个页面跳进来，共享元素入场形变过渡
        targetFragment.sharedElementEnterTransition = sharedElementEnterTransition
        // 替换页面 FrameLayout
        fragmentManager.beginTransaction()
            .replace(containerViewId, targetFragment)
            .commitAllowingStateLoss()
    }

    /**
     * 打开下一级 Fragment，支持共享元素 + enterTransition、过渡重叠控制，加入回退栈，适合页面内 Fragment 打开详情二级页，支持返回回退
     * @param targetFragment 目标Fragment实例
     * @param enterTransition Fragment首次入场的内容过渡动画（Any?兼容平台/AndroidX Transition）
     * @param sharedElementEnterTransition 共享元素入场形变过渡
     * @param sharedSourceView 源页面的共享View
     * @param sharedTransitionName 共享元素transitionName
     * @param overlap 是否允许入场/退场动画重叠（allowEnterTransitionOverlap & allowReturnTransitionOverlap）
     */
    fun commitFragmentWithSharedEnterTransition(targetFragment: Fragment, enterTransition: Any?, sharedElementEnterTransition: Any?, sharedSourceView: View, sharedTransitionName: String, overlap: Boolean) {
        if (useAddHideMode) return
        // Fragment首次进入时，自身内容的入场过渡动画（仅第一次打开fragment触发）
        targetFragment.enterTransition = enterTransition
        // 是否允许【入场动画】和上一页退场动画重叠执行
        targetFragment.allowEnterTransitionOverlap = overlap
        // 回退时，是否允许【返回入场动画】和当前页面退场动画重叠执行
        targetFragment.allowReturnTransitionOverlap = overlap
        // 共享元素：从上个页面跳进来，共享View的形变过渡
        targetFragment.sharedElementEnterTransition = sharedElementEnterTransition
        fragmentManager.beginTransaction()
            .replace(containerViewId, targetFragment)
            .addToBackStack(null)
            .addSharedElement(sharedSourceView, sharedTransitionName)
            .commitAllowingStateLoss()
    }

    /**
     * 获取当前选中的下标
     */
    fun getCurrentIndex(): Int {
        return currentItem
    }

    /**
     * 获取对应的fragment
     * 存在获取不到的情况(直接从0选择2,3的页面，然后获取1，本身并未添加进map，拿到的就是null)
     */
    fun <T : Fragment> getFragment(index: Int): T? {
        return fragmentCache[index] as? T
    }

    /**
     * 设置动画
     * builder.setAnimation(
     *     R.anim.set_translate_right_in, -> 新Fragment进入动画
     *     R.anim.set_translate_left_out, -> 旧Fragment退出动画
     *     R.anim.set_translate_left_in, -> 返回时旧Fragment重新进入动画
     *     R.anim.set_translate_right_out -> 返回时新Fragment退出动画
     * )
     */
    fun setAnimation(vararg elements: Int) {
        enableAnimation = true
        animResList = elements.toMutableList()
    }

    /**
     * 设置点击事件
     */
    fun setOnTabSelectedListener(listener: ((tab: Int) -> Unit)) {
        this.listener = listener
    }

    /**
     * 注册Fragment生命周期回调
     * @param callback 生命周期回调对象
     * @param recursive 是否递归监听子 FragmentManager 内的 Fragment，默认false（Tab场景一般不需要递归）
     *
     * private val callback by lazy { object : FragmentManager.FragmentLifecycleCallbacks() {
     *   override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
     *    super.onFragmentAttached(fm, f, context)
     *   }
     *
     *   override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
     *    super.onFragmentResumed(fm, f)
     *   }
     * }}
     */
    fun registerLifecycleCallbacks(callback: FragmentManager.FragmentLifecycleCallbacks, recursive: Boolean = false) {
        fragmentManager.registerFragmentLifecycleCallbacks(callback, recursive)
        observer.doOnDestroy {
            fragmentManager.unregisterFragmentLifecycleCallbacks(callback)
        }
    }

}

/**
 * 获取路由绑定信息（类 + 路由标识名称）
 * @param name 自定义路由名，不传默认取类名小写
 */
fun Class<*>.getBind(name: String? = null): Pair<Class<*>, String> {
    return this to getSimpleName(name)
}

/**
 * 获取路由绑定信息（类 + 路由标识名称 + Bundle参数）
 * @param name 自定义路由名，不传默认取类名小写
 * @param pairs 参数键值对
 */
fun Class<*>.getBindBundle(name: String? = null, vararg pairs: Pair<String, Any?>): Triple<Class<*>, String, Bundle> {
    val bundle = pairs.toBundle { this }
    return Triple(this, getSimpleName(name), bundle)
}