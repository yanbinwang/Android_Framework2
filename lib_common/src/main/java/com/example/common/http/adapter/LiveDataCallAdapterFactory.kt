package com.example.common.http.adapter

import androidx.lifecycle.LiveData
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by WangYanBin on 2020/6/8.
 */
class LiveDataCallAdapterFactory : CallAdapter.Factory() {

    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if(returnType !is ParameterizedType){
            throw IllegalArgumentException("返回值需为参数化类型")
        }
        //获取returnType的class类型
        val returnClass = getRawType(returnType)
        if(returnClass != LiveData::class.java){
            throw IllegalArgumentException("返回值不是LiveData类型")
        }
        val type = getParameterUpperBound(0, returnType)
        return LiveDataCallAdapter<Any>(type)
    }

}