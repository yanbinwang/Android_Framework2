package com.example.home.activity

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import com.example.common.base.BaseActivity
import com.example.common.base.page.Extras
import com.example.common.utils.function.getStatusBarHeight
import com.example.framework.utils.function.intentSerializable
import com.example.framework.utils.function.view.click
import com.example.framework.utils.function.view.margin
import com.example.home.R
import com.example.home.databinding.ActivityScaleBinding
import com.example.home.view.scale.ScaleAdapter
import com.example.home.view.scale.ScaleImageView

/**
 * @description 大图伸缩
 * @author yan
 */
class ScaleActivity : BaseActivity<ActivityScaleBinding>() {
    private val list by lazy { intentSerializable(Extras.BUNDLE_LIST) as? ArrayList<String> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.set_alpha_trans_in, R.anim.set_alpha_no)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.set_alpha_no, R.anim.set_alpha_trans_in)
    }

    override fun initView() {
        requestedOrientation = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        super.initView()
        initImmersionBar(false, false)
        binding.ivLeft.apply {
            margin(top = getStatusBarHeight())
            click { finish() }
        }
    }

    override fun initData() {
        super.initData()
        val imgList = ArrayList<ScaleImageView>()
        list?.forEach { _ ->
            val img = ScaleImageView(this)
            img.adjustViewBounds = true
            imgList.add(img)
        }
        binding.vpPage.apply {
            adapter = ScaleAdapter(imgList, list.orEmpty())
            currentItem = 0
        }
    }

}