package com.example.home.activity

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.base.page.Extra
import com.example.common.config.ARouterPath
import com.example.common.utils.builder.TitleBuilder
import com.example.framework.utils.function.intentSerializable
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.enterAnimation
import com.example.home.R
import com.example.home.databinding.ActivityScaleBinding
import com.example.home.widget.scale.ScaleAdapter
import com.example.home.widget.scale.ScaleImageView

/**
 * @description 大图伸缩
 * @author yan
 */
@Route(path = ARouterPath.ScaleActivity)
class ScaleActivity : BaseActivity<ActivityScaleBinding>() {
    private val titleBuilder by lazy { TitleBuilder(this, mBinding?.titleRoot) }
    private val list by lazy { intentSerializable<ArrayList<String>>(Extra.BUNDLE_LIST) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.set_alpha_in, R.anim.set_alpha_none)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_alpha_none, R.anim.set_alpha_in)
    }

    override fun initView(savedInstanceState: Bundle?) {
        requestedOrientation = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        super.initView(savedInstanceState)
        initImmersionBar(false)
        titleBuilder.setLeft(tintColor = R.color.bgWhite)
    }

    override fun initData() {
        super.initData()
        val imgList = list?.toNewList { ScaleImageView(this) to it }
        mBinding?.vpPage?.apply {
            adapter = ScaleAdapter(imgList)
            currentItem = 0
            animation = enterAnimation()
        }
    }

}