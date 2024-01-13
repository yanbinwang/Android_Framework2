package com.example.framework.utils.builder

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.framework.utils.function.value.getSimpleName
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
 */
class FragmentBuilder(private val manager: FragmentManager, private val containerViewId: Int) {
    private var isArguments = false
    private var currentItem = 0
    private var clazzList: List<Pair<Class<*>, String>>? = null
    private var clazzBundleList: List<Triple<Class<*>, String, Bundle>>? = null
    private var onTabShowListener: ((tab: Int) -> Unit)? = null
    private val list by lazy { ArrayList<Fragment>() }

    /**
     *  HomeFragment::class.java.getBind()
     *  first：class名
     *  second：tag值，不传默认为class名
     */
    fun bind(clazzList: List<Pair<Class<*>, String>>, defaultCurrentItem: Int = 0) {
        this.list.clear()
        this.isArguments = false
        this.clazzList = clazzList
        selectTab(defaultCurrentItem)
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
    fun bindBundle(clazzBundleList: List<Triple<Class<*>, String, Bundle>>, defaultCurrentItem: Int = 0) {
        this.list.clear()
        this.isArguments = true
        this.clazzBundleList = clazzBundleList
        selectTab(defaultCurrentItem)
    }

    fun bindBundle(vararg clazzTriple: Triple<Class<*>, String, Bundle>) {
        bindBundle(listOf(*clazzTriple))
    }

    /**
     * 切换选择
     */
    fun selectTab(tab: Int) {
        currentItem = tab
        val transaction = manager.beginTransaction()
        list.forEach { transaction.hide(it) }
        val fragment = if (isArguments) newInstanceArguments() else newInstance()
        if (null != fragment) {
            transaction.show(fragment)
            transaction.commitAllowingStateLoss()
            onTabShowListener?.invoke(tab)
        }
    }

    private fun newInstance(): Fragment? {
        clazzList.safeGet(currentItem).let {
            val transaction = manager.beginTransaction()
            var fragment = manager.findFragmentByTag(it?.second)
            if (null == fragment) {
                fragment = it?.first?.getDeclaredConstructor()?.newInstance() as? Fragment
                fragment ?: return null
                transaction.add(containerViewId, fragment, it?.second)
                transaction.commitAllowingStateLoss()
                list.add(fragment)
            }
            return fragment
        }
    }

    private fun newInstanceArguments(): Fragment? {
        clazzBundleList.safeGet(currentItem).let {
            val transaction = manager.beginTransaction()
            var fragment = manager.findFragmentByTag(it?.second)
            if (null == fragment) {
                fragment = it?.first?.getDeclaredConstructor()?.newInstance() as? Fragment
                fragment ?: return null
                fragment.arguments = it?.third
                transaction.add(containerViewId, fragment, it?.second)
                transaction.commitAllowingStateLoss()
                list.add(fragment)
            }
            return fragment
        }
    }

    /**
     * 获取集合
     */
    fun getList(): List<Fragment> {
        return list
    }

    /**
     * 获取对应的fragment
     */
    fun <T : Fragment> getFragment(index: Int): T? {
        return list.safeGet(index) as? T
    }

    /**
     * 获取当前选中的下标
     */
    fun getCurrentIndex(): Int {
        return currentItem
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
fun Class<*>.getBind(name: String? = null, vararg pairs: Pair<String, Any?>): Triple<Class<*>, String, Bundle> {
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