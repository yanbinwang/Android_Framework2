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

import com.example.base.widget.SimpleViewGroup;
import com.example.common.R;
import com.example.common.databinding.ViewEmptyBinding;

import static com.example.common.utils.NetWorkUtil.isNetworkAvailable;

/**
 * Created by android on 2017/8/7.
 *
 * @author Wyb
 * <p>
 * 数据为空时候显示的页面（适用于列表，详情等）
 * 情况如下：
 * <p>
 * 1.加载中-无按钮
 * 2.空数据-无按钮
 * 3.加载错误(无网络，服务器错误)-有按钮
 */
@SuppressLint("InflateParams")
public class EmptyLayout extends SimpleViewGroup {
    private ViewEmptyBinding binding;
    private OnEmptyRefreshListener onEmptyRefreshListener;

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
        Context context = getContext();
        binding = DataBindingUtil.bind(LayoutInflater.from(context).inflate(R.layout.view_empty, null));
        binding.llContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_f6f8ff));

        binding.getRoot().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));//设置LayoutParams
        binding.getRoot().setBackgroundColor(ContextCompat.getColor(context, R.color.gray_f6f8ff));

        binding.tvRefresh.setOnClickListener(v -> {
            //进入加载中，并停止刷新动画
            showLoading();
            if (null != onEmptyRefreshListener) {
                onEmptyRefreshListener.onRefreshListener();
            }
        });
        binding.getRoot().setOnClickListener(null);
        showLoading();
    }

    @Override
    public void draw() {
        if (detectionInflate()) addView(binding.getRoot());
    }

    /**
     * 设置列表所需的emptyview
     */
    public View setListView(View listView) {
        removeView(binding.getRoot());
        ((ViewGroup) listView.getParent()).addView(binding.getRoot());//添加到当前的View hierarchy
        return binding.getRoot();
    }

    /**
     * 数据加载中
     */
    public void showLoading() {
        binding.ivEmpty.setImageResource(R.mipmap.img_data_loading);
        binding.tvEmpty.setText("正在玩命加载数据...");
        binding.tvRefresh.setVisibility(View.GONE);
    }

    public void showEmpty() {
        showEmpty(-1, null);
    }

    /**
     * 数据为空--只会在200并且无数据的时候展示
     */
    public void showEmpty(int resId, String text) {
        binding.ivEmpty.setImageResource(-1 == resId ? R.mipmap.img_data_empty : resId);
        binding.tvEmpty.setText(TextUtils.isEmpty(text) ? "这里还什么都没有呢~" : text);
        binding.tvRefresh.setVisibility(View.GONE);
    }

    public void showError() {
        showError(-1, null);
    }

    /**
     * 数据加载失败-无网络，服务器请求
     * 无网络优先级最高
     */
    public void showError(int resId, String text) {
        if(!isNetworkAvailable()){
            binding.ivEmpty.setImageResource(R.mipmap.img_data_net_error);
            binding.tvEmpty.setText("暂无网络，试试刷新页面吧~");
        }else{
            binding.ivEmpty.setImageResource(-1 == resId ? R.mipmap.img_data_error : resId);
            binding.tvEmpty.setText(TextUtils.isEmpty(text) ? "页面加载失败，请重试" : text);
        }
        binding.tvRefresh.setVisibility(View.VISIBLE);
    }

    /**
     * 设置背景颜色
     */
    public void setBackgroundColor(int color) {
        binding.llContainer.setBackgroundColor(color);
    }

    /**
     * 设置点击
     */
    public void setOnEmptyRefreshListener(OnEmptyRefreshListener onEmptyRefreshListener) {
        this.onEmptyRefreshListener = onEmptyRefreshListener;
    }

}