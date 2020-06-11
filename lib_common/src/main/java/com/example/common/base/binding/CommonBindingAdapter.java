package com.example.common.base.binding;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.example.common.imageloader.ImageLoader;

/**
 * Created by WangYanBin on 2020/6/10.
 * 基础全局xml绑定
 */
public class CommonBindingAdapter {

//    //---------------------------------------------图片注入开始---------------------------------------------
//    @BindingAdapter(value = {"imageRes"}, requireAll = false)
//    public static void setImageRes(ImageView imageView, int imageRes) {
//        imageView.setImageResource(imageRes);
//    }
//
//    @BindingAdapter(value = "displayImage", requireAll = false)
//    public static void setDisplayImage(ImageView view, String url) {
//        ImageLoader.Companion.getInstance().displayImage(view, url);
//    }
//
//    @BindingAdapter(value = "displayRoundImage", requireAll = false)
//    public static void setDisplayRoundImage(ImageView view, String url, int roundingRadius) {
//        ImageLoader.Companion.getInstance().displayRoundImage(view, url, roundingRadius);
//    }
//
//    @BindingAdapter(value = "displayCircleImage", requireAll = false)
//    public static void setDisplayCircleImage(ImageView view, String url) {
//        ImageLoader.Companion.getInstance().displayCircleImage(view, url);
//    }
//    //---------------------------------------------图片注入结束---------------------------------------------
//
//    //---------------------------------------------系统方法注入开始---------------------------------------------
//    @BindingAdapter(value = {"textColor"}, requireAll = false)
//    public static void setTextColor(TextView textView, int textColorRes) {
//        textView.setTextColor(textView.getResources().getColor(textColorRes));
//    }
//
//    @BindingAdapter(value = {"adjustWidth"})
//    public static void adjustWidth(View view, int adjustWidth) {
//        ViewGroup.LayoutParams params = view.getLayoutParams();
//        params.width = adjustWidth;
//        view.setLayoutParams(params);
//    }
//
//    @BindingAdapter(value = {"adjustHeight"})
//    public static void adjustHeight(View view, int adjustHeight) {
//        ViewGroup.LayoutParams params = view.getLayoutParams();
//        params.height = adjustHeight;
//        view.setLayoutParams(params);
//    }
//    //---------------------------------------------系统方法注入结束---------------------------------------------

}
