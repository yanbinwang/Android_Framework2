package com.example.common.bus;

import androidx.lifecycle.MutableLiveData;

import com.example.framework.utils.lifecycle.BusMutableLiveData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by WangYanBin on 2020/6/8.
 */
public final class LiveDataBus {
    private final Map<String, BusMutableLiveData<Object>> bus;

    private LiveDataBus() {
        bus = new HashMap<>();
    }

    private static class SingletonHolder {
        private static final LiveDataBus DEFAULT_BUS = new LiveDataBus();
    }

    public static LiveDataBus get() {
        return SingletonHolder.DEFAULT_BUS;
    }

    public <T> MutableLiveData<T> with(String key, Class<T> type) {
        if (!bus.containsKey(key)) {
            bus.put(key, new BusMutableLiveData<>());
        }
        return (MutableLiveData<T>) bus.get(key);
    }

    public MutableLiveData<Object> with(String key) {
        return with(key, Object.class);
    }

}
