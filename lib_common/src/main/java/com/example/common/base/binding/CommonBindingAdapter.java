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

    //---------------------------------------------图片注入开始---------------------------------------------
    @BindingAdapter(value = {"app:imageRes"})
    public static void setImageRes(ImageView imageView, int imageRes) {
        imageView.setImageResource(imageRes);
    }

    @BindingAdapter(value = {"app:imageUrl"})
    public static void setImageDisplay(ImageView view, String url) {
        ImageLoader.Companion.getInstance().displayImage(view, url);
    }

    @BindingAdapter(value = {"app:imageRoundUrl", "app:imageRoundRadius"})
    public static void setImageRoundDisplay(ImageView view, String url, int roundingRadius) {
        ImageLoader.Companion.getInstance().displayRoundImage(view, url, roundingRadius);
    }

    @BindingAdapter(value = {"app:imageCircleUrl"})
    public static void setImageCircleDisplay(ImageView view, String url) {
        ImageLoader.Companion.getInstance().displayCircleImage(view, url);
    }
    //---------------------------------------------图片注入结束---------------------------------------------

    //---------------------------------------------系统方法注入开始---------------------------------------------
    @BindingAdapter(value = {"app:textColorRes"})
    public static void setTextColor(TextView textView, int textColorRes) {
        textView.setTextColor(textView.getResources().getColor(textColorRes));
    }

    @BindingAdapter(value = {"app:visible"})
    public static void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter(value = {"app:selected"})
    public static void setSelected(View view, boolean select) {
        view.setSelected(select);
    }

    @BindingAdapter(value = {"adjustWidth"})
    public static void setAdjustWidth(View view, int adjustWidth) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = adjustWidth;
        view.setLayoutParams(params);
    }

    @BindingAdapter(value = {"app:adjustHeight"})
    public static void setAdjustHeight(View view, int adjustHeight) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = adjustHeight;
        view.setLayoutParams(params);
    }
    //---------------------------------------------系统方法注入结束---------------------------------------------

}
