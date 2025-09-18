package com.example.mvvm.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.get
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.launch
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.common.utils.ScreenUtil.shouldUseWhiteSystemBarsForColor
import com.example.common.utils.function.color
import com.example.common.utils.function.safeRecycle
import com.example.common.utils.toJson
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
                val coverList = mContext?.let { requestAffair { suspendingGetImageCover(it, data) } } ?: arrayListOf(true to color(R.color.appStatusBar))
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
    suspend fun suspendingGetImageCover(context: Context, uriList: ArrayList<Int>): ArrayList<Pair<Boolean, Int>> {
        return withContext(IO) {
            val colorList = MutableList(uriList.size) { true to color(R.color.appStatusBar) }
            uriList.forEachIndexed { index, imgUrl ->
                // 为每个图片处理设置超时 5秒超时
                val color = withTimeoutOrNull(5000) {
                    getImageCover(context, imgUrl)
                } ?: run {
                    color(R.color.appStatusBar)
                }
                colorList[index] = shouldUseWhiteSystemBarsForColor(color) to color
            }
            "${colorList.toJson()}".logWTF("wyb")
            colorList.toArrayList()
        }
    }

    /**
     * 获取图片背景
     * @param context 上下文对象
     * @return 图片背景色，如果获取失败返回白色
     */
    private suspend fun getImageCover(context: Context, imageUrl: Int): Int {
        return withContext(IO) {
            var bitmap: Bitmap? = null
            var futureTarget: FutureTarget<Bitmap>? = null
            try {
                // 加载图片时可以指定尺寸，避免过大的Bitmap占用过多内存
                futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    // 限制图片尺寸，加速处理
                    .submit(200, 200)
                // 等待结果
                bitmap = futureTarget.get() ?: return@withContext Color.WHITE
                // 记得释放Bitmap内存
                bitmap.getCenterPixelColor()
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Color.WHITE
            } finally {
                // 取消Glide请求，避免内存泄漏
                futureTarget?.let {
                    Glide.with(context).clear(it)
                }
                bitmap.safeRecycle()
            }
        }
    }

    /**
     * 提取Bitmap在x轴中心点颜色
     */
    fun Bitmap?.getCenterPixelColor(): Int {
        // 如果bitmap为空，返回默认颜色值
        this ?: return Color.WHITE
        // 计算中心坐标
        val centerX = width / 2
        // Y轴取第1个像素（索引从0开始，所以是0）
        val topY = 0
        // 确保坐标在有效范围内
        return if (width > 0 && height > 0 && centerX in 0 until width && topY in 0 until height) {
            getPixel(centerX, topY)
        } else {
            Color.WHITE
        }
    }

}