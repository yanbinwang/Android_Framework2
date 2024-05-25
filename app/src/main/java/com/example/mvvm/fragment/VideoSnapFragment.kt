package com.example.mvvm.fragment

import android.os.Bundle
import com.example.common.base.BaseLazyFragment
import com.example.common.base.page.Extra
import com.example.common.utils.toJsonString
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.logWTF
import com.example.mvvm.bean.VideoSnap
import com.example.mvvm.databinding.FragmentVideoSnapBinding
import com.example.mvvm.utils.VideoSnapImpl
import com.example.thirdparty.media.utils.helper.GSYVideoHelper

class VideoSnapFragment : BaseLazyFragment<FragmentVideoSnapBinding>(), VideoSnapImpl {
    private val bundle by lazy { intentParcelable<VideoSnap>(Extra.BUNDLE_BEAN) }
    private val helper by lazy { GSYVideoHelper(mActivity) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        helper.bind(mBinding?.pvVideo)
    }

    override fun releaseVideo(bean: VideoSnap?) {
        "当前bundle:${bundle.toJsonString()}".logWTF
        if (bean?.id == bundle?.id) {
            "满足释放条件，传入的bean:${bean.toJsonString()}".logWTF
        }
    }

    override fun playVideo(bean: VideoSnap?) {
        "当前bundle:${bundle.toJsonString()}".logWTF
        if (bean?.id == bundle?.id) {
            "满足播放条件，传入的bean:${bean.toJsonString()}".logWTF
        }
    }
}