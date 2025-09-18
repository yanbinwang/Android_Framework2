package com.example.mvvm.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.launch
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForColor
import com.example.common.utils.function.color
import com.example.common.utils.toJson
import com.example.common.widget.advertising.Advertising.Companion.getImageCenterPixelColor
import com.example.framework.utils.function.value.currentTimeNano
import com.example.framework.utils.function.value.toArrayList
import com.example.framework.utils.logWTF
import com.example.mvvm.R
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AdViewModel : BaseViewModel() {
    val data by lazy { MutableLiveData<Pair<ArrayList<String>, ArrayList<Pair<Boolean, Int>>>>() }

    fun refresh(refreshNow: Boolean = false) {
        if (refreshNow) {
            refreshNow()
        } else {
            //刷新间隔大于5秒
            if (currentTimeNano - lastRefreshTime < 5000L) {
                reset(false)
                return
            }
            refreshNow()
        }
    }

    private fun refreshNow() {
        lastRefreshTime = currentTimeNano
        getPageInfo()
    }

    private fun getPageInfo() {
        launch {
            flow {
                val list = arrayListOf("bg_ad", "bg_ad2", "bg_ad3", "bg_ad4", "bg_ad5", "bg_ad6", "bg_ad7", "bg_ad8")
                val data = arrayListOf(R.mipmap.bg_ad, R.mipmap.bg_ad2, R.mipmap.bg_ad3, R.mipmap.bg_ad4, R.mipmap.bg_ad5, R.mipmap.bg_ad6, R.mipmap.bg_ad7, R.mipmap.bg_ad8)
                val coverList = mContext?.let { requestAffair { suspendingGetImageCenterPixelColor(it, data) } } ?: arrayListOf(true to color(R.color.appStatusBar))
                emit(list to coverList)
            }.withHandling(err = {
                "${it.toJson()}".logWTF("wyb")
            }, end = {
                reset(false)
            }).collect {
                "${it.toJson()}".logWTF("wyb")
                data.postValue(it)
            }
        }
    }

    /**
     * 获取完服务器集合后,可在对应协程里在加一个获取背景的协程事务
     */
    suspend fun suspendingGetImageCenterPixelColor(context: Context, uriList: ArrayList<Int>): ArrayList<Pair<Boolean, Int>> {
        return withContext(IO) {
            val colorList = MutableList(uriList.size) { true to color(R.color.appStatusBar) }
            uriList.forEachIndexed { index, imgUrl ->
                // 为每个图片处理设置超时 5秒超时
                val color = withTimeoutOrNull(5000) {
                    getImageCenterPixelColor(context, imgUrl)
                } ?: run {
                    color(R.color.appStatusBar)
                }
                colorList[index] = shouldUseWhiteSystemBarsForColor(color) to color
            }
            colorList.toArrayList()
        }
    }

}