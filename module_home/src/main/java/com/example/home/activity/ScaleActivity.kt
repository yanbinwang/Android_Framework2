package com.example.home.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.common.base.BaseActivity
import com.example.common.base.page.Extra
import com.example.common.bean.interf.TransparentOwner
import com.example.common.config.ARouterPath
import com.example.common.utils.builder.TitleBuilder
import com.example.framework.utils.enterAnimation
import com.example.framework.utils.function.intentSerializable
import com.example.framework.utils.function.value.toNewList
import com.example.home.R
import com.example.home.databinding.ActivityScaleBinding
import com.example.home.widget.scale.ScaleAdapter
import com.example.home.widget.scale.ScaleImageView

/**
 * @description 大图伸缩
 * @author yan
 * <activity
 *     android:name="com.example.home.activity.ScaleActivity"
 *     android:configChanges="orientation|screenSize|keyboardHidden|screenLayout|uiMode"
 *     android:theme="@style/TransparentTheme"
 *     android:windowSoftInputMode="stateHidden|adjustPan" />
 */
@TransparentOwner
@Route(path = ARouterPath.ScaleActivity)
class ScaleActivity : BaseActivity<ActivityScaleBinding>() {
    private val titleBuilder by lazy { TitleBuilder(this, mBinding?.titleRoot) }
    private val list by lazy { intentSerializable<ArrayList<String>>(Extra.BUNDLE_LIST) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initImmersionBar(false)
        titleBuilder.setLeft(tintColor = R.color.bgWhite)
    }

    override fun initData() {
        super.initData()
        val imgList = list?.toNewList { ScaleImageView(this) to it }
        mBinding?.vpPage?.apply {
            adapter = ScaleAdapter(imgList.orEmpty())
            currentItem = 0
            animation = enterAnimation()
        }
    }

}