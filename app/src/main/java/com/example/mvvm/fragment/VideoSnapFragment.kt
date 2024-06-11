package com.example.mvvm.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.common.base.BaseLazyFragment
import com.example.common.base.page.Extra
import com.example.common.utils.toJsonString
import com.example.framework.utils.function.intentParcelable
import com.example.framework.utils.logWTF
import com.example.mvvm.bean.VideoSnapBean
import com.example.mvvm.databinding.FragmentVideoSnapBinding
import com.example.mvvm.utils.VideoSnapImpl
import com.example.thirdparty.media.utils.helper.GSYVideoHelper

@SuppressLint("SetTextI18n")
class VideoSnapFragment : BaseLazyFragment<FragmentVideoSnapBinding>(), VideoSnapImpl {
    private val helper by lazy { GSYVideoHelper(activity) }
    private val bundle by lazy { intentParcelable<VideoSnapBean>(Extra.BUNDLE_BEAN) }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        helper.bind(mBinding?.player)
        helper.setUrl("https://txmov2.a.yximgs.com/upic/2021/04/25/21/BMjAyMTA0MjUyMTE5NTZfMjExMjA0ODM0NF80ODQ1MzY1OTE5Ml8xXzM=_b_B8ad92744b489ba4e164a12802e2e37d4.mp4")
    }

    override fun releaseVideo(bean: VideoSnapBean?) {
        "当前bundle:${bundle.toJsonString()}".logWTF
        if (bean?.id == bundle?.id) {
            "满足释放条件，传入的bean:${bean.toJsonString()}".logWTF
            helper.pause()
        }
    }

    override fun playVideo(bean: VideoSnapBean?) {
        "当前bundle:${bundle.toJsonString()}".logWTF
        if (bean?.id == bundle?.id) {
            "满足播放条件，传入的bean:${bean.toJsonString()}".logWTF
            helper.start()
        }
    }

}