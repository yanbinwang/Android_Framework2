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
import com.example.klinechart.entity.KLineEntity
import com.example.klinechart.utils.DataHelper
import com.example.mvvm.bean.KLineBean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

class KLineViewModel : BaseViewModel() {
    val uiManage by lazy { MutableLiveData<Boolean>() }
    val list by lazy { MutableLiveData<List<KLineEntity>?>() }

    fun getAll() {
        launch {
            val data = ArrayList<KLineEntity>()
            flow {
                val list = requestAffair { suspendingKLineData() }.toList(KLineBean::class.java)?.toArrayList().toNewList { bean->
                    val entity = KLineEntity()
                    entity.let {
                        it.mClose = bean.Close.toSafeFloat()
                        it.mDate = bean.Date.orEmpty()
                        it.mHigh = bean.High.toSafeFloat()
                        it.mLow = bean.Low.toSafeFloat()
                        it.mOpen = bean.Open.toSafeFloat()
                        it.mVolume = bean.Volume.toSafeFloat()
                    }
                    entity
                }
                DataHelper.calculate(list)
                emit(list)
            }.withHandling(end = {
                uiManage.postValue(false)
            }).onStart {
                uiManage.postValue(true)
            }.collect {
                data.clear()
                data.addAll(it.orEmpty())
                list.postValue(data)
            }
        }
    }

    fun getData(offset: Int, size: Int) {
        launch {
            flow {
                val list = requestAffair { suspendingKLineData() }.toList(KLineBean::class.java)?.toArrayList().toNewList { bean->
                    val entity = KLineEntity()
                    entity.let {
                        it.mClose = bean.Close.toSafeFloat()
                        it.mDate = bean.Date.orEmpty()
                        it.mHigh = bean.High.toSafeFloat()
                        it.mLow = bean.Low.toSafeFloat()
                        it.mOpen = bean.Open.toSafeFloat()
                        it.mVolume = bean.Volume.toSafeFloat()
                    }
                    entity
                }
                DataHelper.calculate(list)
                emit(list)
            }.withHandling(end = {
                uiManage.postValue(false)
            }).onStart {
                uiManage.postValue(true)
            }.collect {
                it ?: return@collect
                val data = ArrayList<KLineEntity>()
                val start = max(0, it.size - 1 - offset - size)
                val stop = min(it.size, it.size - offset)
                for (i in start..<stop) {
                    data.add(it[i])
                }
                list.postValue(data)
            }
        }
    }

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