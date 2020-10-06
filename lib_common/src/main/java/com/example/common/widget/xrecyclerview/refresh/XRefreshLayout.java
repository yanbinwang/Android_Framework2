package com.example.common.widget.xrecyclerview.refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.example.base.utils.DisplayUtil;
import com.example.common.R;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;

/**
 * Created by WangYanBin on 2020/9/17.
 * 自定义刷新控件（根据项目定制特定刷新样式）
 */
public class XRefreshLayout extends TwinklingRefreshLayout {
    private RefreshHeaderView headerView;
    private RefreshBottomView bottomView;
    private int refreshDirection;

    public XRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    public XRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {
        Context context = getContext();
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.XRefreshLayout);
        refreshDirection = mTypedArray.getInt(R.styleable.XRefreshLayout_direction, 2);
        mTypedArray.recycle();
        //定义刷新控件及一些属性
        headerView = new RefreshHeaderView(context);
        bottomView = new RefreshBottomView(context);
        setHeaderHeight(DisplayUtil.dip2px(context, 25));
        setMaxHeadHeight(DisplayUtil.dip2px(context, 30));
        setBottomHeight(DisplayUtil.dip2px(context, 15));
        setMaxBottomHeight(DisplayUtil.dip2px(context, 20));
        setDirection(refreshDirection);
    }

    //设置刷新样式
    public void setDirection(int refreshDirection) {
        this.refreshDirection = refreshDirection;
        switch (refreshDirection) {
            //顶部
            case 0:
                setHeaderView(headerView);
                setEnableRefresh(true);
                setEnableLoadmore(false);
                setEnableOverScroll(false);
                break;
            //底部
            case 1:
                setBottomView(bottomView);
                setEnableRefresh(false);
                setEnableLoadmore(true);
                setEnableOverScroll(false);
                break;
            //都有（默认）
            case 2:
                setHeaderView(headerView);
                setBottomView(bottomView);
                setEnableRefresh(true);
                setEnableLoadmore(true);
                setEnableOverScroll(true);
                break;
        }
    }

    public int getDirection() {
        return refreshDirection;
    }

}