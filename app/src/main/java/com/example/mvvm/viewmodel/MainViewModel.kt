package com.example.mvvm.viewmodel

import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.launch
import com.example.common.utils.file.getStringFromAssert
import com.example.common.utils.function.toList
import com.example.framework.utils.function.value.toNewList
import com.example.framework.utils.function.value.toSafeFloat
import com.example.mvvm.bean.KLineBean
import com.github.fujianlian.klinechart.DataHelper
import com.github.fujianlian.klinechart.KLineEntity
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class MainViewModel : BaseViewModel() {
    val kLineData by lazy { MutableLiveData<List<KLineEntity>?>() }

    fun getPageData() {
        launch {
            val data = withContext(IO) {
                context.getStringFromAssert("ibm.json")
                    .toList<KLineBean>(object : TypeToken<List<KLineBean>>() {}.type)
            }?.subList(0, 500)
            val newData = data.toNewList {
                KLineEntity().apply {
                    Date = it.Date
                    openPrice = it.Open.toSafeFloat()
                    closePrice = it.Close.toSafeFloat()
                    highPrice = it.High.toSafeFloat()
                    lowPrice = it.Low.toSafeFloat()
                    volume = it.Volume.toSafeFloat()
                }
            }
            DataHelper.calculate(newData)
            kLineData.value = newData
        }

    }

}