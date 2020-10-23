package com.example.common.widget.xrecyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DefaultItemAnimator;

import com.example.base.utils.DisplayUtil;
import com.example.common.R;
import com.example.common.widget.empty.EmptyLayout;
import com.example.common.widget.xrecyclerview.callback.OnEmptyClickListener;
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration;
import com.example.common.widget.xrecyclerview.refresh.XRefreshLayout;
import com.example.common.widget.xrecyclerview.refresh.callback.OnXRefreshListener;
import com.example.common.widget.xrecyclerview.refresh.callback.SwipeRefreshLayoutDirection;

/**
 * author: wyb
 * date: 2017/11/20.
 * <p>
 * 一般自定义view或viewGroup基本上都会去实现onMeasure、onLayout、onDraw方法，还有另外两个方法是onFinishInflate和onSizeChanged。
 * onFinishInflate方法只有在布局文件中加载view实例会回调，如果直接new一个view的话是不会回调的。
 */
@SuppressLint("InflateParams")
public class XRecyclerView extends ViewGroup {
    private EmptyLayout el;//自定义封装的空布局
    private XRefreshLayout xRefresh;//刷新控件 类型1才有
    private DetectionRecyclerView dRv;//数据列表
    private OnEmptyClickListener onEmptyClickListener;//空布局点击
    private OnXRefreshListener onXRefreshListener;//刷新回调
    private int refreshType, emptyType, refreshDirection;//页面类型(0无刷新-1带刷新)刷新类型（0顶部-1底部-2全部）是否具有空布局（0无-1有）

    public XRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    public XRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(attrs);
    }

    private void initialize(AttributeSet attrs) {
        TypedArray mTypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.XRecyclerView);
        refreshType = mTypedArray.getInt(R.styleable.XRecyclerView_refreshType, 0);
        refreshDirection = mTypedArray.getInt(R.styleable.XRecyclerView_refreshDirection, 2);
        emptyType = mTypedArray.getInt(R.styleable.XRecyclerView_emptyType, 0);
        mTypedArray.recycle();
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

    //当view被手机绘制好的时候各个view方法的初始化
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initRefreshType(refreshType);
    }

    private void initRefreshType(int refreshType) {
        View view = null;
        switch (refreshType) {
            //不带刷新
            case 0:
                view = LayoutInflater.from(getContext()).inflate(R.layout.view_xrecyclerview, null);
                dRv = view.findViewById(R.id.d_rv);
                if (0 != emptyType) {
                    el = new EmptyLayout(getContext());

                    dRv.setEmptyView(el.setListView(dRv));
                    dRv.setHasFixedSize(true);
                    dRv.setItemAnimator(new DefaultItemAnimator());
                    el.setOnEmptyRefreshListener(() -> {
                        if (null != onEmptyClickListener) {
                            onEmptyClickListener.onClickListener();
                        }
                    });
                }
                break;
            //带刷新
            case 1:
                view = LayoutInflater.from(getContext()).inflate(R.layout.view_xrecyclerview_refresh, null);
                el = view.findViewById(R.id.el);
                xRefresh = view.findViewById(R.id.x_refresh);
                dRv = view.findViewById(R.id.d_rv);
                //设置刷新的方式，默认上下皆有
                switch (refreshDirection) {
                    case 0:
                        xRefresh.setDirection(SwipeRefreshLayoutDirection.TOP);
                        break;
                    case 1:
                        xRefresh.setDirection(SwipeRefreshLayoutDirection.BOTTOM);
                        break;
                    case 2:
                        xRefresh.setDirection(SwipeRefreshLayoutDirection.BOTH);
                        break;
                }
                dRv.setHasFixedSize(true);
                dRv.setItemAnimator(new DefaultItemAnimator());
                if (0 != emptyType) {
                    el.setOnEmptyRefreshListener(() -> {
                        if (null != onEmptyClickListener) {
                            onEmptyClickListener.onClickListener();
                        }
                    });
                } else {
                    el.setVisibility(View.GONE);
                }
                xRefresh.setOnRefreshListener(onXRefreshListener);
                break;
        }
        addView(view);
    }

    //当数据正在加载的时候显示
    public void showLoading() {
        if (0 != emptyType) {
            el.showLoading();
        }
    }

    //当数据为空时(显示需要显示的图片，以及内容字)
    public void showEmpty() {
        if (0 != emptyType) {
            el.showEmpty();
        }
    }

    //当数据为空时(显示需要显示的图片，以及内容字)---传入图片
    public void showEmpty(int imgInt, String emptyStr) {
        if (0 != emptyType) {
            el.showEmpty(imgInt, emptyStr);
        }
    }

    //当数据错误时（没有网络）
    public void showError() {
        if (0 != emptyType) {
            el.showError();
        }
    }

    //类型1的时候才会显示
    public void setVisibilityEmptyView(int visibility) {
        if (refreshType == 1 && 0 != emptyType) {
            el.setVisibility(visibility);
        }
    }

    //设置禁止刷新
    public void finishRefreshing() {
        if (refreshType == 1) {
            xRefresh.finishRefreshing();
        }
    }

    //修改背景颜色
    public void setEmptyBackgroundColor(int color) {
        el.setBackgroundColor(color);
    }

    //选择下标
    public void scrollToPosition(int position) {
        dRv.scrollToPosition(position);
    }

    //获取空布局
    public EmptyLayout getEmptyView() {
        return el;
    }

    //添加分隔线
    public void addItemDecoration(int horizontalSpace, int verticalSpace, boolean hasHorizontalEdge, boolean hasVerticalEdge) {
        SparseArray<SCommonItemDecoration.ItemDecorationProps> propMap = new SparseArray<>();
        SCommonItemDecoration.ItemDecorationProps prop1 = new SCommonItemDecoration.ItemDecorationProps(DisplayUtil.dip2px(getContext(), horizontalSpace), DisplayUtil.dip2px(getContext(), verticalSpace), hasHorizontalEdge, hasVerticalEdge);
        propMap.put(0, prop1);
        dRv.addItemDecoration(new SCommonItemDecoration(propMap));
    }

    //返回页面整体
    public DetectionRecyclerView getRecyclerView() {
        return dRv;
    }

    //空布局刷新
    public void setOnEmptyViewClickListener(OnEmptyClickListener onEmptyClickListener) {
        this.onEmptyClickListener = onEmptyClickListener;
    }

    //刷新页面刷新
    public void setOnXRefreshListener(OnXRefreshListener onXRefreshListener) {
        this.onXRefreshListener = onXRefreshListener;
    }

}
