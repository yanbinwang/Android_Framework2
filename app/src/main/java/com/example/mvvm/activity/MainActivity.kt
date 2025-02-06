package com.example.mvvm.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.base.page.Extra
import com.example.common.config.ARouterPath
import com.example.common.utils.builder.TabLayoutBuilder
import com.example.framework.utils.builder.FragmentBuilder
import com.example.framework.utils.function.value.clearFragmentSavedState
import com.example.mvvm.R
import com.example.mvvm.databinding.ActivityMainBinding
import com.example.mvvm.widget.MainIndicator

/**
 * 首页
 */
@Route(path = ARouterPath.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var currentItem = 0
    private val indicator by lazy { MainIndicator(mBinding?.tbIndicator) }

    // <editor-fold defaultstate="collapsed" desc="处理系统回收">
    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState.clearFragmentSavedState()
        super.onCreate(savedInstanceState)
    }

    override fun recreate() {
        intent = Intent().putExtra(Extra.TAB_INDEX, indicator.mCurrentIndex)
        super.recreate()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        intent.getIntExtra(Extra.TAB_INDEX, 0).also { indicator.setSelect(it) }
    }
    // </editor-fold>

    override fun isEventBusEnabled() = true

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val builder = FragmentBuilder(supportFragmentManager, R.id.fl_content).apply {
//            bind(HomeFragment::class.java.getBind(), MarketFragment::class.java.getBind(), AccountFragment::class.java.getBind())
        }
        indicator.apply {
            init()
            bind(builder)
        }
    }

    override fun initEvent() {
        super.initEvent()
        indicator.setOnTabChangeListener(object : TabLayoutBuilder.OnTabChangeListener {
            override fun onReselected(position: Int) {
                onSelected(position, true)
            }

            override fun onSelected(position: Int) {
                onSelected(position, false)
            }

            override fun onUnselected(position: Int) {
            }
        })
    }

    private fun onSelected(index: Int, isReselected: Boolean) {
        //如果是重复点击的，或者与上一次相等的情况，不予以操作
        val unable = isReselected || index == currentItem
        if (!unable) {
//            if (index == 2 && !isLogin()) {
//                navigation(ARouterPath.LoginActivity)
//                //秒切频率太快，commit还未来得及切换，倒计时1s切回上个选项卡
//                schedule({
//                    indicator.setSelect(currentItem)
//                })
//            } else {
//                currentItem = index
//            }
        }
    }

}