package com.example.mvvm.viewmodel

import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.launch
import com.example.common.utils.file.getStringFromAssert
import com.example.common.utils.function.toList
import com.github.fujianlian.klinechart.DataHelper
import com.github.fujianlian.klinechart.KLineEntity
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class MainViewModel : BaseViewModel() {
    val kLineData by lazy { MutableLiveData<List<KLineEntity>?>() }

    fun getPageData() {
        launch {
            val data = withContext(IO) { context.getStringFromAssert("ibm.json").toList<KLineEntity>(object : TypeToken<List<KLineEntity>>() {}.type) }?.subList(0, 500)
            DataHelper.calculate(data)
            kLineData.value = data
        }

    }

}