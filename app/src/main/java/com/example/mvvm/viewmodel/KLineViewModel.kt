package com.example.mvvm.viewmodel

import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.launch
import com.example.common.network.repository.requestAffair
import com.example.common.network.repository.withHandling
import com.example.common.utils.toList
import com.example.framework.utils.function.value.toArrayList
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.function.value.toSafeFloat
import com.example.klinechart.bean.KLineChartBean
import com.example.klinechart.utils.DataHelper
import com.example.mvvm.bean.KLineBean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlin.collections.take

class KLineViewModel : BaseViewModel() {
    val uiManage by lazy { MutableLiveData<Unit>() }
    val list by lazy { MutableLiveData<List<KLineChartBean>?>() }

    fun getAll() {
        launch {
            flow {
                val list = requestAffair { suspendingKLineData() }.toList(KLineBean::class.java)?.toArrayList().toNewList { bean ->
                    val entity = KLineChartBean()
                    entity.let {
                        it.closePrice = bean.close.toSafeFloat()
                        it.date = bean.date.orEmpty()
                        it.highPrice = bean.high.toSafeFloat()
                        it.lowPrice = bean.low.toSafeFloat()
                        it.openPrice = bean.open.toSafeFloat()
                        it.volume = bean.volume.toSafeFloat()
                    }
                    entity
                }
                // 整体/赋值后端数据
                DataHelper.calculate(list)
                // 检出/赋值 UI
                emit(list)
            }.withHandling(end = {
                uiManage.postValue(Unit)
            }).collect {
                // 1) K线绘制性能瓶颈：Canvas 一次性绘制上千根K线+5条均线+BOLL轨道会明显掉帧，500是一个经验阈值，保证滑动流畅
                // 2) 屏幕实际可见量有限：手机横屏最多显示60~100根K线，500条足够覆盖"加载更多"之前的可视区域+缓冲
                list.postValue(it.take(500).toArrayList())
            }
        }
    }

//    fun getData(offset: Int, size: Int) {
//        launch {
//            flow {
//                val list = requestAffair { suspendingKLineData() }.toList(KLineBean::class.java)?.toArrayList().toNewList { bean ->
//                    val entity = KLineChartBean()
//                    entity.let {
//                        it.mClose = bean.close.toSafeFloat()
//                        it.mDate = bean.date.orEmpty()
//                        it.mHigh = bean.high.toSafeFloat()
//                        it.mLow = bean.low.toSafeFloat()
//                        it.mOpen = bean.open.toSafeFloat()
//                        it.mVolume = bean.volume.toSafeFloat()
//                    }
//                    entity
//                }
//                DataHelper.calculate(list)
//                val data = ArrayList<KLineChartBean>()
//                val start = max(0, list.size - 1 - offset - size)
//                val stop = min(list.size, list.size - offset)
//                for (i in start..<stop) {
//                    data.add(list[i])
//                }
//                emit(data)
//            }.withHandling(end = {
//                uiManage.postValue(false)
//            }).onStart {
//                uiManage.postValue(true)
//            }.collect {
//                list.postValue(it)
//            }
//        }
//    }

    private suspend fun suspendingKLineData(): String {
        return withContext(IO) {
            try {
                mContext?.resources?.assets?.open("ibm.json")?.use { inputStream ->
                    // available() 方法返回的是 “当前可无阻塞读取的字节数”，并不一定等于文件的总大小
//                    val buffer = ByteArray(inputStream.available())
//                    inputStream.read(buffer)
//                    String(buffer, charset("UTF-8"))
                    // 获取文件总大小（assets中可用此方法，其他流可能需要先获取长度）
                    val fileSize = inputStream.available()
                    val buffer = ByteArray(fileSize)
                    // 循环读取直到填满缓冲区或流结束
                    var totalRead = 0
                    while (totalRead < fileSize) {
                        val bytesRead = inputStream.read(buffer, totalRead, fileSize - totalRead)
                        // 流提前结束（意外情况）
                        if (bytesRead == -1) break
                        totalRead += bytesRead
                    }
                    String(buffer, Charsets.UTF_8)
                } ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

}