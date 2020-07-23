package com.example.common.widget.empty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.example.common.R;
import com.example.common.databinding.ViewEmptyBinding;


/**
 * Created by android on 2017/8/7.
 *
 * @author Wyb
 * <p>
 * 数据为空时候显示的页面（适用于列表，详情等）
 * 情况如下：
 * <p>
 * 1.加载中
 * 2.加载错误(只有断网情况下会显示点击刷新按钮)
 * 3.空布局(没有数据的时候显示)
 */
@SuppressLint("InflateParams")
public class EmptyLayout extends ViewGroup {
    private ViewEmptyBinding binding;
    private OnEmptyRefreshListener onEmptyRefreshListener;
    public static String EMPTY_TXT = "没有数据", ERROR_TXT = "没有网络";//数据为空时的内容,数据加载失败的内容

    public EmptyLayout(Context context) {
        super(context);
        initialize();
    }

    public EmptyLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initialize();
    }

    public EmptyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        binding = DataBindingUtil.bind(LayoutInflater.from(getContext()).inflate(R.layout.view_empty, null));
        binding.srlEmptyRefresh.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.blue_2e60df));
        binding.srlEmptyRefresh.setOnRefreshListener(() -> {
            binding.srlEmptyRefresh.setRefreshing(false);
            if (null != onEmptyRefreshListener) {
                onEmptyRefreshListener.onRefreshListener();
            }
        });
        binding.getRoot().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));//设置LayoutParams
        binding.getRoot().setOnClickListener(null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            v.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            v.layout(0, 0, r, b);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(binding.getRoot());
    }

    //设置列表所需的emptyview
    public View setListView(View listView) {
        removeView(binding.getRoot());
        ((ViewGroup) listView.getParent()).addView(binding.getRoot());//添加到当前的View hierarchy
        return binding.getRoot();
    }

    //当数据正在加载的时候显示（接口返回快速时会造成闪屏）
    public void showLoading() {
        binding.srlEmptyRefresh.setVisibility(View.GONE);
        binding.ivEmpty.setVisibility(View.GONE);
        binding.tvEmpty.setVisibility(View.GONE);
    }

    //当数据为空时(显示需要显示的图片，以及内容字)
    public void showEmpty() {
        binding.srlEmptyRefresh.setVisibility(View.VISIBLE);
        binding.ivEmpty.setVisibility(View.VISIBLE);
        binding.ivEmpty.setBackgroundResource(0);
        binding.ivEmpty.setImageResource(R.mipmap.img_data_empty);
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.tvEmpty.setText(EMPTY_TXT);
    }

    //当数据为空时(显示需要显示的图片，以及内容字)---传入图片-1：原图 0：不需要图片 default：传入的图片
    public void showEmpty(int resId, String emptyText) {
        binding.srlEmptyRefresh.setVisibility(View.VISIBLE);
        binding.ivEmpty.setBackgroundResource(0);
        if (-1 == resId) {
            binding.ivEmpty.setVisibility(View.VISIBLE);
            binding.ivEmpty.setImageResource(R.mipmap.img_net_err);
        } else if (0 == resId) {
            binding.ivEmpty.setVisibility(View.GONE);
        } else {
            binding.ivEmpty.setVisibility(View.VISIBLE);
            binding.ivEmpty.setImageResource(resId);
        }
        binding.tvEmpty.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(emptyText)) {
            binding.tvEmpty.setText(EMPTY_TXT);
        } else {
            binding.tvEmpty.setText(emptyText);
        }
    }

    //当数据错误时（没有网络）
    public void showError() {
        binding.srlEmptyRefresh.setVisibility(View.VISIBLE);
        binding.ivEmpty.setVisibility(View.VISIBLE);
        binding.ivEmpty.setBackgroundResource(0);
        binding.ivEmpty.setImageResource(R.mipmap.img_net_err);
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.tvEmpty.setText(ERROR_TXT);
    }

    //设置背景颜色
    public void setBackgroundColor(int color) {
        binding.srlEmptyRefresh.setBackgroundColor(color);
    }

    //设置点击
    public void setOnEmptyRefreshListener(OnEmptyRefreshListener onEmptyRefreshListener) {
        this.onEmptyRefreshListener = onEmptyRefreshListener;
    }

}