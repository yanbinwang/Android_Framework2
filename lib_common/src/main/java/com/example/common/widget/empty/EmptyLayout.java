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
import com.example.common.widget.xrecyclerview.refresh.callback.OnXRefreshListener;


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
public class EmptyLayout extends SimpleViewGroup {
    private ViewEmptyBinding binding;
    private OnEmptyRefreshListener onEmptyRefreshListener;
    private final String EMPTY_TXT = "没有数据";//数据为空时的内容
    private final String ERROR_TXT = "没有网络";//数据加载失败的内容

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
        binding.xEmptyRefresh.setOnXRefreshListener(new OnXRefreshListener() {

            @Override
            public void onRefresh() {
                super.onRefresh();
                //进入加载中，并停止刷新动画
                showLoading();
                binding.xEmptyRefresh.finishRefreshing();
                if (null != onEmptyRefreshListener) {
                    onEmptyRefreshListener.onRefreshListener();
                }
            }
        });
        binding.getRoot().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));//设置LayoutParams
        binding.getRoot().setOnClickListener(null);
        setBackgroundColor(ContextCompat.getColor(context, R.color.gray_f6f8ff));
        showLoading();
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
        binding.xEmptyRefresh.setVisibility(View.GONE);
        binding.ivEmpty.setVisibility(View.GONE);
        binding.tvEmpty.setVisibility(View.GONE);
    }

    //当数据为空时(显示需要显示的图片，以及内容字)
    public void showEmpty() {
        showEmpty(-1, null);
    }

    //当数据为空时(显示需要显示的图片，以及内容字)---传入图片-1：原图 0：不需要图片 default：传入的图片
    public void showEmpty(int resId, String emptyText) {
        binding.xEmptyRefresh.setVisibility(View.VISIBLE);
        binding.ivEmpty.setBackgroundResource(0);
        if (-1 == resId) {
            binding.ivEmpty.setVisibility(View.VISIBLE);
            binding.ivEmpty.setImageResource(R.mipmap.img_data_empty);
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
        binding.xEmptyRefresh.setVisibility(View.VISIBLE);
        binding.ivEmpty.setVisibility(View.VISIBLE);
        binding.ivEmpty.setBackgroundResource(0);
        binding.ivEmpty.setImageResource(R.mipmap.img_net_err);
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.tvEmpty.setText(ERROR_TXT);
    }

    //设置背景颜色
    public void setBackgroundColor(int color) {
        binding.xEmptyRefresh.setBackgroundColor(color);
    }

    //设置点击
    public void setOnEmptyRefreshListener(OnEmptyRefreshListener onEmptyRefreshListener) {
        this.onEmptyRefreshListener = onEmptyRefreshListener;
    }

}