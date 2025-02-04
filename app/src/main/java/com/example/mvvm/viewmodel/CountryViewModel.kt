package com.example.mvvm.viewmodel

import androidx.lifecycle.MutableLiveData
import com.example.common.base.bridge.BaseViewModel
import com.example.common.base.bridge.launch
import com.example.common.utils.toList
import com.example.framework.utils.function.value.orZero
import com.example.mvvm.R
import com.example.mvvm.bean.CountryBean
import com.example.mvvm.widget.sidebar.CharacterParser
import com.example.mvvm.widget.sidebar.PinyinComparator
import com.example.mvvm.widget.sidebar.bean.SortBean
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Collections
import java.util.Locale

/**
 * 城市选择
 */
class CountryViewModel : BaseViewModel() {
    private val pinyinComparator by lazy { PinyinComparator() } //拼音符号的处理类
    val list by lazy { MutableLiveData<List<SortBean>?>() }

    fun getPageInfo() {
        launch {
            val sourceList = withContext(IO) {
//                //城市集合
//                val countryList = ArrayList<CountryBean>()
//                var json: JSONObject?
//                mContext.resources.openRawResource(R.raw.city_msg).use { inputStream ->
//                    val byte = ByteArray(inputStream.available())
//                    inputStream.read(byte)
//                    json = JSONObject(String(byte, charset("gbk")))
//                }
//                val jsonArr = json?.optJSONArray("communitys")
//                for (j in 0 until jsonArr?.length().orZero) {
//                    val arr = jsonArr?.optJSONArray(j)
//                    for (i in 0 until arr?.length().orZero) {
//                        val jsonObj = arr?.optJSONObject(i)
//                        val country = CountryBean()
//                        country.countryId = jsonObj?.optString("id")
//                        country.countryName = jsonObj?.optString("name")
//                        countryList.add(country)
//                    }
//                }
//                filledData(countryList)
                val stringBuilder = StringBuilder()
                try {
                    val bufferedReader = BufferedReader(InputStreamReader(mContext.assets.open("pcas-code.json")))
                    var nextStr: String?
                    while (null != (bufferedReader.readLine().also { nextStr = it })) {
                        stringBuilder.append(nextStr)
                    }
                } catch (e: IOException) {
                    stringBuilder.delete(0, stringBuilder.length)
                }
                val json = stringBuilder.toString()
                val list = json.toList(CountryBean::class.java)
                filledData(list)
            }
            //根据拼音字幕升序排序
            Collections.sort(sourceList, pinyinComparator)
            //数据处理
            if (sourceList.isEmpty()) {
                empty()
            } else {
                reset(false)
                //排序好后返回
                list.postValue(sourceList)
            }
        }
    }

    /**
     * 整理数据，填充数据
     */
//    private fun filledData(list: List<CountryBean>): List<SortBean> {
//        val mList = ArrayList<SortBean>()
//        list.forEach {
//            val bean = SortBean()
//            val name = it.countryName
//            bean.name = name
//            bean.beseId = it.countryId
//            //汉字转化为拼音
//            val pinyin = CharacterParser.instance.getSelling(name.orEmpty())
//            //取第一个拼音字母
//            val sortString = pinyin.substring(0, 1).uppercase(Locale.getDefault())
//            //match()的参数一般为正则表达式---A-Z之间的数
//            if (sortString.matches("[A-Z]".toRegex())) {
//                bean.sortLetters = sortString.uppercase(Locale.getDefault())
//            } else {
//                bean.sortLetters = "#"
//            }
//            mList.add(bean)
//        }
//        return mList
//    }

    private fun filledData(list: List<CountryBean>?): List<SortBean> {
        val mList = ArrayList<SortBean>()
        list?.forEach {
            val bean = SortBean()
            val name = it.name
            bean.name = name
            bean.beseId = it.code
            //汉字转化为拼音
            val pinyin = CharacterParser.instance.getSelling(name.orEmpty())
            //取第一个拼音字母
            val sortString = pinyin.substring(0, 1).uppercase(Locale.getDefault())
            //match()的参数一般为正则表达式---A-Z之间的数
            if (sortString.matches("[A-Z]".toRegex())) {
                bean.sortLetters = sortString.uppercase(Locale.getDefault())
            } else {
                bean.sortLetters = "#"
            }
            mList.add(bean)
        }
        return mList
    }

}