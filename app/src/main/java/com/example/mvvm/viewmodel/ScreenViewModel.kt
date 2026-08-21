package com.example.mvvm.viewmodel

import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.launch
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.common.utils.ScreenUtil
import com.example.common.utils.function.pt
import com.example.framework.utils.function.value.orZero
import com.example.framework.utils.logWTF
import com.example.thirdparty.media.utils.getPipAspectRatio
import com.example.thirdparty.media.utils.suspendingCalculateHeight
import kotlinx.coroutines.flow.flow

class ScreenViewModel : BaseViewModel() {
    private val videoUrl = "https://stream7.iqilu.com/10339/upload_transcode/202002/09/20200209105011F0zPoYzHry.mp4"
    val pageInfo by lazy { MutableLiveData<String>() } // 页面整体状态
    val videoHeight by lazy { MutableLiveData<Int>() } // 计算的实际高度

    fun getPageInfo() {
        launch {
            flow {
                emit(mContext?.let { requestAffair { suspendingCalculateHeight(it, videoUrl) } })
            }.withHandling({
                videoHeight.postValue(280.pt)
            }, {
                pageInfo.postValue(videoUrl)
                reset(false)
            }).collect {
                val displayHeight = it.orZero
                videoHeight.postValue(displayHeight)
                val ratio = getPipAspectRatio(ScreenUtil.screenWidth, displayHeight)
                "高度:${it}\n宽度:${ScreenUtil.screenWidth}\n比率:${ratio.first}:${ratio.second}".logWTF("wyb")
            }
        }.manageJob()
    }
}