package com.example.common.widget.advertising;

import static androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.base.function.FunctionsContextKt;
import com.example.base.utils.WeakHandler;
import com.example.base.widget.SimpleViewGroup;
import com.example.common.R;
import com.example.common.widget.advertising.adapter.AdvertisingAdapter;
import com.example.common.widget.advertising.callback.AdvertisingImpl;
import com.example.common.widget.advertising.callback.OnAdvertisingClickListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wangyanbin
 * 广告控件
 */
@SuppressLint("ClickableViewAccessibility")
public class Advertising extends SimpleViewGroup implements AdvertisingImpl, DefaultLifecycleObserver {
    private boolean allow = true, scroll = true, local;//是否允许滑动，是否自动滚动，是否是本地
    private int curIndex, oldIndex, margin, focusedId, normalId;//当前选中的数组索引,上次选中的数组索引,左右边距,圆点选中时的背景ID,圆点正常时的背景ID
    private Timer timer;//自动滚动的定时器
    private List<String> list;//图片网络路径数组
    private ArrayList<Integer> localList;//图片本地路径数组
    private ViewPager2 banner;//广告容器
    private LinearLayout ovalLayout;//圆点容器
    private OnAdvertisingClickListener onAdvertisingClickListener;//监听
    private final int halfPosition = Integer.MAX_VALUE / 2;//计算中心值
    private final AdvertisingAdapter adapter = new AdvertisingAdapter();//图片适配器
    private final WeakHandler weakHandler = new WeakHandler(Looper.getMainLooper());//切线程

    // <editor-fold defaultstate="collapsed" desc="基类方法">
    public Advertising(Context context) {
        super(context);
        initialize();
    }

    public Advertising(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public Advertising(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        banner = new ViewPager2(getContext());
        //去除水波纹
        banner.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        banner.setAdapter(adapter);
        banner.setOrientation(ORIENTATION_HORIZONTAL);
        banner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //切换圆点
                curIndex = position % list.size();
                if (ovalLayout != null && list.size() > 1) {
                    //圆点取消
                    ovalLayout.getChildAt(oldIndex).setBackgroundResource(normalId);
                    //圆点选中
                    ovalLayout.getChildAt(curIndex).setBackgroundResource(focusedId);
                    oldIndex = curIndex;
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                allow = positionOffsetPixels == 0;
            }
        });
    }

    @Override
    public void drawView() {
        if (onFinish()) addView(banner);
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);
        if (scroll) startTimer();
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onPause(owner);
        if (scroll) stopTimer();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimer();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实现方法">
    public void start(@NotNull ArrayList<Integer> resList, @Nullable LinearLayout ovalLayout) {
        this.localList = resList;
        this.list = new ArrayList<>();
        for (Integer ignored : resList) {
            list.add("");
        }
        start(list, ovalLayout, R.mipmap.ic_ad_select, R.mipmap.ic_ad_unselect, 10, true);
    }

    public void start(@NotNull List<String> uriList) {
        start(uriList, null);
    }

    public void start(@NotNull List<String> uriList, @Nullable LinearLayout ovalLayout) {
        start(uriList, ovalLayout, R.mipmap.ic_ad_select, R.mipmap.ic_ad_unselect, 10, false);
    }

    @Override
    public void start(@NotNull List<String> uriList, @Nullable LinearLayout ovalLayout, int focusedId, int normalId, int margin, boolean local) {
        this.local = local;
        this.list = uriList;
        this.ovalLayout = ovalLayout;
        this.focusedId = focusedId;
        this.normalId = normalId;
        this.margin = margin;
        //设置数据
        initData();
    }

    /**
     * 初始化圆点,图片数据
     */
    private void initData() {
        //如果只有一第图时不显示圆点容器
        if (ovalLayout != null && list.size() < 2) {
            ovalLayout.getLayoutParams().height = 0;
        } else if (ovalLayout != null) {
            ovalLayout.setGravity(Gravity.CENTER);
            //如果true代表垂直，否则水平
            boolean direction = ovalLayout.getLayoutParams().height > ovalLayout.getLayoutParams().width;
            //左右边距
            int ovalMargin = FunctionsContextKt.dip2px(getContext(), margin);
            //添加圆点
            for (int i = 0; i < list.size(); i++) {
                ImageView imageView = new ImageView(getContext());
                ovalLayout.addView(imageView);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                if (direction) {
                    layoutParams.setMargins(ovalMargin, 0, ovalMargin, 0);
                } else {
                    layoutParams.setMargins(0, ovalMargin, 0, ovalMargin);
                }
                imageView.setLayoutParams(layoutParams);
                imageView.setBackgroundResource(normalId);
            }
            //选中第一个
            ovalLayout.getChildAt(0).setBackgroundResource(focusedId);
        }
        //设置图片数据
        if (local) adapter.setLocalList(localList); else adapter.setList(list);
        adapter.setOnItemClickListener(position -> {
            if (null != onAdvertisingClickListener) {
                onAdvertisingClickListener.onItemClick(position);
            }
        });
        //设置默认选中的起始位置
        int position = 0;
        if (list.size() > 1) position = halfPosition - (halfPosition % list.size());
        banner.setCurrentItem(position, false);
    }

    /**
     * 开始自动滚动任务 图片大于1张才滚动
     */
    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    if (allow && list.size() > 1) {
                        weakHandler.post(() -> {
                            int current = banner.getCurrentItem();
                            int position = current + 1;
                            if (current == 0 || current == Integer.MAX_VALUE) position = halfPosition - (halfPosition % list.size());
                            banner.setCurrentItem(position);
                        });
                    }
                }
            }, 3000, 3000);
        }
    }

    /**
     * 停止自动滚动任务
     */
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * 绑定对应页面的生命周期-》对应回调重写对应方法
     * @param lifecycleOwner
     */
    public void addLifecycleObserver(LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    @Override
    public void setAutoScroll(boolean scroll) {
        this.scroll = scroll;
        if (!scroll) stopTimer();
    }

    @Override
    public void setOrientation(int orientation) {
        banner.setOrientation(orientation);
    }

    @Override
    public void setPageTransformer(int marginPx) {
        banner.setPageTransformer(new MarginPageTransformer(FunctionsContextKt.dip2px(getContext(), marginPx)));
    }

    @Override
    public void setOnAdvertisingClickListener(@NotNull OnAdvertisingClickListener onAdvertisingClickListener) {
        this.onAdvertisingClickListener = onAdvertisingClickListener;
    }
    // </editor-fold>

}