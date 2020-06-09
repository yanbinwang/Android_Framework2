package com.example.common.base.bridge;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.example.common.BaseApplication;
import com.example.common.imageloader.ImageLoader;
import com.example.framework.utils.ToastUtil;

/**
 * Created by WangYanBin on 2020/6/9.
 * 基础行为类，定义xml中能直接执行的绑定语句
 */
public class BaseHandler {

//    //---------------------------------------------图片加载注入开始---------------------------------------------
//    @BindingAdapter(value = "app:displayImage", requireAll = true)
//    public static void displayImage(ImageView image, String string) {
//        ImageLoader.Companion.getInstance().displayImage(image, string);
//    }
//
//    @BindingAdapter(value = "app:displayRoundImage", requireAll = true)
//    public static void displayRoundImage(ImageView image, String string, int roundingRadius) {
//        ImageLoader.Companion.getInstance().displayRoundImage(image, string, roundingRadius);
//    }
//
//    @BindingAdapter(value = "app:displayCircleImage", requireAll = true)
//    public static void displayCircleImage(ImageView image, String string) {
//        ImageLoader.Companion.getInstance().displayCircleImage(image, string);
//    }
//    //---------------------------------------------图片加载注入结束---------------------------------------------
//
//    //---------------------------------------------系统方法注入开始---------------------------------------------
//    @BindingAdapter(value = "app:showToast", requireAll = true)
//    public static void showToast(String msg) {
//        ToastUtil.INSTANCE.mackToastSHORT(msg, BaseApplication.getInstance().getApplicationContext());
//    }
//    //---------------------------------------------系统方法注入结束---------------------------------------------

}
