package com.example.common.http.adapter

import com.example.framework.widget.lifecycle.BusMutableLiveData
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by WangYanBin on 2020/6/8.
 */
class LiveDataCallAdapter<R>(var type: Type) : CallAdapter<R, BusMutableLiveData<R>> {

    override fun adapt(call: Call<R>?): BusMutableLiveData<R> {
        return object : BusMutableLiveData<R>() {
            //这个作用是业务在多线程中,业务处理的线程安全问题,确保单一线程作业
            val flag = AtomicBoolean(false)
            override fun onActive() {
                super.onActive()
                if (flag.compareAndSet(false, true)) {
                    call!!.enqueue(object : Callback<R> {
                        override fun onFailure(call: Call<R>?, t: Throwable?) {
                            postValue(null)
                        }

                        override fun onResponse(call: Call<R>?, response: Response<R>?) {
                            postValue(response?.body())
                        }
                    })
                }
            }
        }
    }

    override fun responseType(): Type {
        return type
    }

}