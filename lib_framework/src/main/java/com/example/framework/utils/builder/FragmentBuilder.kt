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
import com.example.framework.utils.function.value.safeSize
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 *  Created by wangyanbin
 *  页面切换管理工具类
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
 *  1.四个子Fragment是在MainActivity内部的
 *  2.当前Fragment第一次加载时调取onResume
 *  3.二级页面被打开其后关闭，统一调取一次（栈内初始化了几个Fragment就回调几个）
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
class FragmentBuilder(private val fragmentManager: FragmentManager, private val observer: LifecycleOwner, private val containerViewId: Int, private val isAdd: Boolean = true) {
    private var isArguments = false//是否是添加参数的模式
    private var isAnimation = false//是否执行动画
    private var currentItem = -1//默认下标->不指定任何值
    private var managerLength = 0//当前需要管理的总长度
    private var anim: MutableList<Int>? = null//动画集合
    private var clazz: MutableList<Pair<Class<*>, String>>? = null//普通模式class集合
    private var clazzBundle: MutableList<Triple<Class<*>, String, Bundle>>? = null//参数模式class集合
    private var listener: ((tab: Int) -> Unit)? = null//切换监听
    private val buffer by lazy { ConcurrentHashMap<Int, Fragment>() }//存储声明的fragment

    init {
        observer.doOnDestroy {
            anim?.clear()
            buffer.clear()
            clazz?.clear()
            clazzBundle?.clear()
        }
    }

    /**
     *  HomeFragment::class.java.getBind()
     *  first：class名
     *  second：tag值，不传默认为class名
     */
    fun bind(list: List<Pair<Class<*>, String>>, default: Int = 0) {
        isArguments = false
        buffer.clear()
        clazz = list.toMutableList()
        managerLength = list.safeSize
        selectTab(default)
    }

    fun bind(vararg clazzPair: Pair<Class<*>, String>, default: Int = 0) {
        bind(listOf(*clazzPair), default)
    }

    /**
     * SceneListFragment::class.java.getBindBundle("Scene${i}", pairs = arrayOf(Extra.ID to i))
     * first：class名
     * second：内存中存储的tag
     * third：pair对象 （first，fragment透传的key second，透传的值）
     */
    fun bindBundle(list: List<Triple<Class<*>, String, Bundle>>, default: Int = 0) {
        isArguments = true
        buffer.clear()
        clazzBundle = list.toMutableList()
        managerLength = list.safeSize
        selectTab(default)
    }

    fun bindBundle(vararg clazzTriple: Triple<Class<*>, String, Bundle>, default: Int = 0) {
        bindBundle(listOf(*clazzTriple), default)
    }

    /**
     * 切换选择
     * 重复选择或者超过初始化长度都return
     */
    fun selectTab(tab: Int, recreate: Boolean = false) {
        if (recreate) {
            selectTabNow(tab)
        } else {
            if (currentItem == tab || tab > managerLength - 1 || tab < 0) return
            selectTabNow(tab)
        }
    }

    private fun selectTabNow(tab: Int) {
        currentItem = tab
        val transaction = fragmentManager.beginTransaction()
        //设置动画（进入、退出、返回进入、返回退出）->只有add这种保留原fragment在栈内的情况才会设置动画
        if (isAnimation && isAdd) {
            if (anim.safeSize == 2) {
                transaction.setCustomAnimations(anim.safeGet(0).orZero, anim.safeGet(1).orZero)
            } else {
                transaction.setCustomAnimations(anim.safeGet(0).orZero, anim.safeGet(1).orZero, anim.safeGet(2).orZero, anim.safeGet(3).orZero)
            }
        }
        //使现有的fragment，全部隐藏
        for ((_, value) in buffer) {
            transaction.hide(value)
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
            listener?.invoke(tab)
        }
    }

    private fun newInstance(): Fragment? {
        clazz.safeGet(currentItem).let {
            val transaction = fragmentManager.beginTransaction()
            val tag = it?.second
            var fragment = fragmentManager.findFragmentByTag(tag)
            if (null == fragment) {
                fragment = it?.first?.getDeclaredConstructor()?.newInstance() as? Fragment
                fragment ?: return null
                commit(transaction, fragment, tag)
            }
            return fragment
        }
    }

    private fun newInstanceArguments(): Fragment? {
        clazzBundle.safeGet(currentItem).let {
            val transaction = fragmentManager.beginTransaction()
            val tag = it?.second
            var fragment = fragmentManager.findFragmentByTag(tag)
            if (null == fragment) {
                fragment = it?.first?.getDeclaredConstructor()?.newInstance() as? Fragment
                fragment ?: return null
                fragment.arguments = it?.third
                commit(transaction, fragment, tag)
            }
            return fragment
        }
    }

    /**
     * 初始化提交
     */
    private fun commit(transaction: FragmentTransaction, fragment: Fragment, tag: String?) {
        //add会将视图保存在栈内，适用于首页切换，replace会直接替换，如果子fragment列表要切换使用此方法，需要注意，replace使用后，动画就失效了
        if (isAdd) {
            transaction.add(containerViewId, fragment, tag)
        } else {
            transaction.replace(containerViewId, fragment, tag)
        }
        transaction.commitAllowingStateLoss()
        //replace栈内只有一个，集合也只存一个
        if (!isAdd) {
            buffer.clear()
        }
        buffer[currentItem] = fragment
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
        return buffer[index] as? T
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
        anim = elements.toMutableList()
    }

    /**
     * 设置点击事件
     */
    fun setOnTabSelectedListener(listener: ((tab: Int) -> Unit)) {
        this.listener = listener
    }

    /**
     * 设置manager切换时的监听
     * private val callback by lazy {
     * object : FragmentManager.FragmentLifecycleCallbacks(){
     * override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
     * super.onFragmentAttached(fm, f, context)
     * }
     *
     * override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
     * super.onFragmentResumed(fm, f)
     * }
     * }
     *  }
     */
    fun registerLifecycleCallbacks(callback: FragmentManager.FragmentLifecycleCallbacks) {
        fragmentManager.registerFragmentLifecycleCallbacks(callback, false)
        observer.doOnDestroy {
            fragmentManager.unregisterFragmentLifecycleCallbacks(callback)
        }
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