package com.example.common.widget;

import static androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.base.widget.SimpleViewGroup;

/**
 * 自定义viewpage2,替换首页或一些底部切换页时使用
 */
public class PagerFlipper extends SimpleViewGroup {
    private ViewPager2 pager;//广告容器

    public PagerFlipper(Context context) {
        super(context);
        initialize();
    }

    public PagerFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public PagerFlipper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        pager = new ViewPager2(getContext());
        pager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);//去除水波纹
    }

    @Override
    public void drawView() {
        if (onFinish()) addView(pager);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        pager.setAdapter(adapter);
        pager.setOrientation(ORIENTATION_HORIZONTAL);
        pager.setOffscreenPageLimit(adapter.getItemCount());//预加载数量
        pager.setUserInputEnabled(false);//禁止左右滑动
    }

    public void registerOnPageChangeCallback(ViewPager2.OnPageChangeCallback callback) {
        pager.registerOnPageChangeCallback(callback);
    }

    public void setCurrentItem(int item) {
        setCurrentItem(item, false);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        pager.setCurrentItem(item, smoothScroll);
    }

}