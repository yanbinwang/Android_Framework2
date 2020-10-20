package com.example.common.widget.xrecyclerview.refresh;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.example.common.R;
import com.example.common.widget.xrecyclerview.refresh.callback.OnXRefreshListener;

/**
 * Created by WangYanBin on 2020/9/17.
 * 刷新控件二次封装，设置对应项目的默认值
 */
public class XRefreshLayout extends SwipeRefreshLayout{

    public XRefreshLayout(Context context) {
        super(context);
        initialize();
    }

    public XRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.black));
    }

    public void setOnXRefreshListener(OnXRefreshListener onXRefreshListener) {
        setOnRefreshListener(onXRefreshListener);
    }

    public void finishRefreshing(){
        setRefreshing(false);
    }

}