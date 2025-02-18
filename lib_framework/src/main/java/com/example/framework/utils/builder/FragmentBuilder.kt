package com.example.framework.utils.builder

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import com.example.framework.utils.function.doOnDestroy
import com.example.framework.utils.function.value.getSimpleName
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.function.value.safeGet
import java.io.Serializable

/**
 *  Created by wangyanbin
 *  页面切换管理工具类
 *  manager->當前頁面的FragmentManager
 *  containerViewId->當前頁面FrameLayout佈局的id
 *  fragmentList->fragment的集合
 *  tab->没人选中的下标
 *
 *  //记录下标
 *  override fun recreate() {
 *  intent = Intent().apply { putExtra(Extras.TAB_INDEX, tabBottom.selectedTabPosition) }
 *  super.recreate()
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
 *  super.onRestoreInstanceState(savedInstanceState)
 *  intent.getIntExtra(Extras.TAB_INDEX, 0).also { navigationBuilder.selectedItem(it) }//注意selected
 *  }
 *
 *  /**
 *   * 1.4个子Fragment是在MainActivity内部的
 *   * 2.当前Fragment第一次加载时调取onResume
 *   * 3.二级页面被打开其后关闭，统一调取一次（栈内初始化了几个Fragment就回调几个）
 *  */
 *  override fun onResume() {
 *  super.onResume()
 *  if (isHidden) return
 *  refreshNow()
 *  }
 *
 *  1.当前Fragment第一次加载时不会被调取
 *  2.使用FragmentManager切换时，栈内有几个Fragment就回调几个
 *  3.!hidden表示当前可见
 *  override fun onHiddenChanged(hidden: Boolean) {
 *  super.onHiddenChanged(hidden)
 *  if (!hidden) refreshNow()
 *  }
 *
 *  private fun refreshNow() {
 *  viewModel?.refresh()
 *  helper.zendeskInfo()
 *  }
 *
 *  add和replace区别，如果我要在容器内加载一连串fragment，它们使用的是同一个xml文件，只是id有区分，此时就可能出现ui错位
 *  这种时候就使用replace直接删除容器之前的fragment，直接替换（保证当前容器内只有一个fragment）
 */
class FragmentBuilder(private val manager: FragmentManager, private val containerViewId: Int, private val extras: Boolean = true) {
    private var mCurrentItem = -1//默认下标
    private var isArguments = false//是否是添加参数的模式
    private var isAnimation = false//是否执行动画
    private var animList: MutableList<Int>? = null//动画集合
    private var clazzList: List<Pair<Class<*>, String>>? = null//普通模式class集合
    private var clazzBundleList: List<Triple<Class<*>, String, Bundle>>? = null//参数模式class集合
    private var onTabShowListener: ((tab: Int) -> Unit)? = null//切换监听
    private val bufferList by lazy { ArrayList<Fragment>() }//存储声明的fragment

    /**
     *  HomeFragment::class.java.getBind()
     *  first：class名
     *  second：tag值，不传默认为class名
     */
    fun bind(clazzList: List<Pair<Class<*>, String>>, default: Int = 0) {
        this.isArguments = false
        this.bufferList.clear()
        this.clazzList = clazzList
        selectTab(default)
    }

    fun bind(vararg clazzPair: Pair<Class<*>, String>) {
        bind(listOf(*clazzPair))
    }

    /**
     * EvidencePageFragment::class.java.getBind(Extras.REQUEST_ID to id, "EviPager${id}")
     * first：class名
     * second：pair对象 （first，fragment透传的key second，透传的值）
     * third：内存中存储的tag
     */
    fun bindBundle(clazzBundleList: List<Triple<Class<*>, String, Bundle>>, default: Int = 0) {
        this.isArguments = true
        this.bufferList.clear()
        this.clazzBundleList = clazzBundleList
        selectTab(default)
    }

    fun bindBundle(vararg clazzTriple: Triple<Class<*>, String, Bundle>) {
        bindBundle(listOf(*clazzTriple))
    }

    /**
     * 切换选择
     */
    fun selectTab(tab: Int) {
        if (mCurrentItem == tab) return
        mCurrentItem = tab
        val transaction = manager.beginTransaction()
        //设置动画（进入、退出、返回进入、返回退出）
        if (isAnimation && extras) {
            transaction.setCustomAnimations(animList.safeGet(0).orZero, animList.safeGet(1).orZero, animList.safeGet(2).orZero, animList.safeGet(3).orZero)
        }
        //使现有的fragment，全部隐藏
        bufferList.forEach {
            transaction.hide(it)
        }
        //获取到选中的fragment
        val fragment = if (isArguments) {
            newInstanceArguments()
        } else {
            newInstance()
        }
        //不为空的情况下，显示出来
        if (null != fragment) {
            transaction.show(fragment)
            transaction.commitAllowingStateLoss()
            onTabShowListener?.invoke(tab)
        }
    }

    private fun newInstance(): Fragment? {
        clazzList.safeGet(mCurrentItem).let {
            val transaction = manager.beginTransaction()
            val tag = it?.second
            var fragment = manager.findFragmentByTag(tag)
            if (null == fragment) {
                fragment = it?.first?.getDeclaredConstructor()?.newInstance() as? Fragment
                fragment ?: return null
                initCommit(transaction, fragment, tag)
            }
            return fragment
        }
    }

    private fun newInstanceArguments(): Fragment? {
        clazzBundleList.safeGet(mCurrentItem).let {
            val transaction = manager.beginTransaction()
            val tag = it?.second
            var fragment = manager.findFragmentByTag(tag)
            if (null == fragment) {
                fragment = it?.first?.getDeclaredConstructor()?.newInstance() as? Fragment
                fragment ?: return null
                fragment.arguments = it?.third
                initCommit(transaction, fragment, tag)
            }
            return fragment
        }
    }

    /**
     * 初始化提交
     */
    private fun initCommit(transaction: FragmentTransaction, fragment: Fragment, tag: String?) {
        //add会将视图保存在栈内，适用于首页切换，replace会直接替换，如果子fragment列表要切换使用此方法，需要注意，replace使用后，动画就失效了
        if (extras) {
            transaction.add(containerViewId, fragment, tag)
        } else {
            transaction.replace(containerViewId, fragment, tag)
        }
        transaction.commitAllowingStateLoss()
        //replace栈内只有一个，集合也只存一个
        if (!extras) {
            bufferList.clear()
        }
        bufferList.add(fragment)
    }

    /**
     * 获取当前选中的下标
     */
    fun getCurrentIndex(): Int {
        return mCurrentItem
    }

    /**
     * 设置manager切换时的监听
     * private val callback by lazy {
     * object : FragmentManager.FragmentLifecycleCallbacks(){
     * override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
     * super.onFragmentAttached(fm, f, context)
     * "Attached-当前fragment:${f}".logWTF("wyb")
     * }
     *
     * override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
     * super.onFragmentResumed(fm, f)
     * "Resumed-当前fragment:${f}".logWTF("wyb")
     * }
     * }
     *  }
     */
    fun setLifecycleCallbacks(owner: LifecycleOwner, callback: FragmentManager.FragmentLifecycleCallbacks) {
        manager.registerFragmentLifecycleCallbacks(callback, false)
        owner.doOnDestroy {
            manager.unregisterFragmentLifecycleCallbacks(callback)
        }
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
        isAnimation = true
        animList = elements.toMutableList()
    }

    /**
     * 设置点击事件
     */
    fun setOnItemClickListener(onTabShow: ((tab: Int) -> Unit)) {
        this.onTabShowListener = onTabShow
    }

}

/**
 * 默认返回自身和自身class名小写，也可指定
 */
fun Class<*>.getBind(name: String? = null): Pair<Class<*>, String> {
    return this to getSimpleName(name)
}

/**
 * 默认返回自身和自身class名小写以及请求的id
 */
fun Class<*>.getBindBundle(name: String? = null, vararg pairs: Pair<String, Any?>): Triple<Class<*>, String, Bundle> {
    val bundle = Bundle()
    pairs.forEach {
        val key = it.first
        when (val value = it.second) {
            is Int -> bundle.putInt(key, value)
            is Byte -> bundle.putByte(key, value)
            is Char -> bundle.putChar(key, value)
            is Long -> bundle.putLong(key, value)
            is Float -> bundle.putFloat(key, value)
            is Short -> bundle.putShort(key, value)
            is Double -> bundle.putDouble(key, value)
            is Boolean -> bundle.putBoolean(key, value)
            is String? -> bundle.putString(key, value)
            is Bundle? -> bundle.putBundle(key, value)
            is IntArray? -> bundle.putIntArray(key, value)
            is ByteArray? -> bundle.putByteArray(key, value)
            is CharArray? -> bundle.putCharArray(key, value)
            is LongArray? -> bundle.putLongArray(key, value)
            is FloatArray? -> bundle.putFloatArray(key, value)
            is Parcelable? -> bundle.putParcelable(key, value)
            is ShortArray? -> bundle.putShortArray(key, value)
            is DoubleArray? -> bundle.putDoubleArray(key, value)
            is BooleanArray? -> bundle.putBooleanArray(key, value)
            is CharSequence? -> bundle.putCharSequence(key, value)
            is Serializable? -> bundle.putSerializable(key, value)
        }
    }
    return Triple(this, getSimpleName(name), bundle)
}