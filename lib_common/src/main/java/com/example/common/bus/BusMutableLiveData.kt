package com.example.common.bus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.*

/**
 *  Created by wangyanbin
 */
class BusMutableLiveData<T> : MutableLiveData<T>() {
    private val observerMap by lazy { HashMap<Observer<*>, Observer<*>>() }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, observer)
        try {
            hook(observer)
        } catch (ignored: Exception) {
        }
    }

    private fun hook(observer: Observer<in T>) {
        //get wrapper's version
        val classLiveData = LiveData::class.java
        val fieldObservers = classLiveData.getDeclaredField("mObservers")
        fieldObservers.isAccessible = true
        val objectObservers = fieldObservers[this]
        val classObservers = objectObservers.javaClass
        val methodGet = classObservers.getDeclaredMethod("get", Any::class.java)
        methodGet.isAccessible = true
        val objectWrapperEntry = methodGet.invoke(objectObservers, observer)
        var objectWrapper: Any? = null
        if (objectWrapperEntry is Map.Entry<*, *>) objectWrapper = objectWrapperEntry.value
        if (objectWrapper == null) throw NullPointerException("Wrapper can not be bull!")
        val classObserverWrapper = objectWrapper.javaClass.superclass
        val fieldLastVersion = classObserverWrapper.getDeclaredField("mLastVersion")
        fieldLastVersion.isAccessible = true
        //get livedata's version
        val fieldVersion = classLiveData.getDeclaredField("mVersion")
        fieldVersion.isAccessible = true
        val objectVersion = fieldVersion[this]
        //set wrapper's version
        fieldLastVersion[objectWrapper] = objectVersion
    }

    private class ObserverWrapper<T>(observer: Observer<T>) : Observer<T> {
        private var observer: Observer<T>? = observer

        override fun onChanged(t: T) {
            if (observer != null) {
                if (isCallOnObserve()) return
                observer?.onChanged(t)
            }
        }

        private fun isCallOnObserve(): Boolean {
            val stackTrace = Thread.currentThread().stackTrace
            if (stackTrace.isNotEmpty()) {
                for (element in stackTrace) {
                    if ("android.arch.lifecycle.LiveData" == element.className && "observeForever" == element.methodName) return true
                }
            }
            return false
        }
    }

    override fun observeForever(observer: Observer<in T>) {
        if (!observerMap.containsKey(observer)) observerMap[observer] = ObserverWrapper(observer)
        super.observeForever(observerMap[observer] as Observer<in T>)
    }

    override fun removeObserver(observer: Observer<in T>) {
        val realObserver = if (observerMap.containsKey(observer)) {
            observerMap.remove(observer)
        } else {
            observer
        }
        super.removeObserver(realObserver as Observer<in T>)
    }

}