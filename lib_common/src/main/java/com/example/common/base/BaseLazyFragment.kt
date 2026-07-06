package com.example.common.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding

/**
 * Created by WangYanBin on 2020/6/10.
 * 数据懒加载，当界面不可展示时，不执行加载数据的方法 (Viewpager2专用)
 *
 * 1）ViewPager2(isAdded如果是无缓存模式下记得做判断，但如果只有2个页面无需判断，因为默认不管如何都是加载左右两个页面，3个及以上需要管控)
 * 1.子页面在适配器加载出来时只会加载当前下标页面的onResume方法，比如2个子页面，一开始适配器加载出来只会执行第一个页面的onResume方法
 * 2.所有子页面的onHiddenChanged会在主页面的Activity/Fragment被盖住时全部触发，并且触发多次（如果你并未点击到后面几页，就只会频繁触发当前页的onHiddenChanged）且还会触发当前选中子页面的onPause，然后页面重新回来后，执行当前选中子页面的onResume方法
 *
 * ------第一次进页面生命周期------
 * 当前页数：第1页---生命周期：onResume---页面显影（hidden-false显示，true不显示）：显示
 * ------点击另一个页面执行的生命周期------
 * 当前页数：第1页---生命周期：onPause---页面显影（hidden-false显示，true不显示）：显示
 * 当前页数：第2页---生命周期：onResume---页面显影（hidden-false显示，true不显示）：显示
 * ------页面被盖住后执行的生命周期------
 * 当前页数：第1页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：隐藏
 * 当前页数：第2页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：隐藏
 * 当前页数：第2页---生命周期：onPause---页面显影（hidden-false显示，true不显示）：隐藏
 * 当前页数：第1页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：显示
 * 当前页数：第2页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：显示
 * ------盖住的页面被关闭后，回到当前页面执行的生命周期------
 * 当前页数：第2页---生命周期：onResume---页面显影（hidden-false显示，true不显示）：显示
 *
 * 总结：如果是Activity内的Viewpager2，管控好onResume和onPause即可，左右子页面切换之前的子页面会回调onPause，当前子页面会onResume，属于正常交互情况
 * 如果是Fragment内的Viewpager2，onHiddenChanged也需要管控，Fragment切成另一个页面，切过去和切回来都会触发onHiddenChanged，但onHiddenChanged不会在页面初始化加载时触发
 *
 * 2)FragmentManager
 * 1.replace方法会直接清空之前的管理器内的所有页面，重新创建，所以只要管控好当前选中的页面的onResume方法即可，可以在该生命周期内网络请求或者刷新(replace不要使用addToBackStack，不然资源释放会出问题)
 *
 * ------第一次进页面生命周期------
 * 当前页数：第1页---生命周期：onResume---页面显影（hidden-false显示，true不显示）：显示
 * ------点击另一个页面执行的生命周期------
 * 当前页数：第1页---生命周期：onPause---页面显影（hidden-false显示，true不显示）：显示
 * 当前页数：第2页---生命周期：onResume---页面显影（hidden-false显示，true不显示）：显示
 * 当前页数：第1页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：隐藏
 * ------页面被盖住后执行的生命周期------
 * 当前页数：第2页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：隐藏
 * 当前页数：第2页---生命周期：onPause---页面显影（hidden-false显示，true不显示）：隐藏
 * 当前页数：第2页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：显示
 * ------盖住的页面被关闭后，回到当前页面执行的生命周期------
 * 当前页数：第2页---生命周期：onResume---页面显影（hidden-false显示，true不显示）：显示
 *
 * 总结：如果是Activity内的FragmentManager，管控好onResume和onPause即可，左右子页面切换之前的子页面会回调onPause，当前子页面会onResume，属于正常交互情况
 * 如果是Fragment内的FragmentManager，onHiddenChanged也需要管控，Fragment切成另一个页面，切过去和切回来都会触发onHiddenChanged，但onHiddenChanged不会在页面初始化加载时触发
 *
 * 2.add方法会保存之前添加的fragment，所以当前页面如果已经被add后，再切回来的话onResume方法是不执行的，会执行onHiddenChanged方法，
 * 而onResume方法则会在整个页面被盖住，盖住其的页面被关闭时所有管理器内存储的页面都会执行一次，故而如果是用FragmentManager的add来切换页面，需要一些特殊处理
 *
 * ------第一次进页面生命周期------
 * 当前页数：第1页---生命周期：onResume---页面显影（hidden-false显示，true不显示）：显示
 * ------点击另一个页面执行的生命周期------
 * 当前页数：第2页---生命周期：onResume---页面显影（hidden-false显示，true不显示）：显示
 * 当前页数：第1页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：隐藏
 * ------页面被盖住后执行的生命周期------
 * 当前页数：第2页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：隐藏
 * 当前页数：第1页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：隐藏
 * 当前页数：第1页---生命周期：onPause---页面显影（hidden-false显示，true不显示）：隐藏
 * 当前页数：第2页---生命周期：onPause---页面显影（hidden-false显示，true不显示）：隐藏
 * 当前页数：第2页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：显示
 * 当前页数：第1页---生命周期：onHiddenChanged---页面显影（hidden-false显示，true不显示）：隐藏
 * ------盖住的页面被关闭后，回到当前页面执行的生命周期-----
 * 当前页数：第1页---生命周期：onResume---页面显影（hidden-false显示，true不显示）：隐藏
 * 当前页数：第2页---生命周期：onResume---页面显影（hidden-false显示，true不显示）：显示
 *
 * 总结：如果是Activity内的FragmentManager，管控好onResume和onHiddenChanged，onResume在触发一次后，之后只会触发onHiddenChanged
 * 如果是Fragment内的FragmentManager，在Fragment切成另一个页面，切过去和切回来都会触发onHiddenChanged，但onHiddenChanged不会在页面初始化加载时触发
 *
 * 页面处理方法：
 * override fun onResume() {
 *     super.onResume()
 *     if (isHidden) return
 *     refreshNow()
 * }
 * override fun onHiddenChanged(hidden: Boolean) {
 *     super.onHiddenChanged(hidden)
 *     if (!hidden) refreshNow()
 * }
 * private fun refreshNow() {
 *     viewModel?.refresh()
 * }
 */
abstract class BaseLazyFragment<VDB : ViewDataBinding> : BaseFragment<VDB>() {
    private var isDataLoadEnabled = true   // 是否允许加载数据
    private var hasLoadedData = false      // 数据是否已真正加载
    private var hasReachedResume = false   // 是否已经历过可见的 onResume

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isLazyLoadEnabled = true
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (isHidden) return
        if (!hasReachedResume) {
            hasReachedResume = true
            if (isDataLoadEnabled) {
                initData()
                hasLoadedData = true
            }
        }
    }

    /**
     * 纯配置方法：仅更新加载开关，无任何副作用
     * 通常在 new Fragment / Adapter 初始化时调用
     */
    open fun setDataLoadEnabled(enabled: Boolean) {
        isDataLoadEnabled = enabled
    }

    /**
     * 显式行为方法：手动触发首次加载（带防重入保护）
     * 当外部决定"现在可以加载了"时主动调用
     */
    open fun tryFirstLoad() {
        if (isDataLoadEnabled && hasReachedResume && !hasLoadedData) {
            initData()
            hasLoadedData = true
        }
    }
    // </editor-fold>

}