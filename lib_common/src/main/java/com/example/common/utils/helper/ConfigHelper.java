package com.example.common.utils.helper;

import com.tencent.mmkv.MMKV;

/**
 * Created by WangYanBin on 2020/6/1.
 * 存储应用配置
 */
public class ConfigHelper {
    private static MMKV mmkv;

    static {
        mmkv = MMKV.defaultMMKV();
    }

}
