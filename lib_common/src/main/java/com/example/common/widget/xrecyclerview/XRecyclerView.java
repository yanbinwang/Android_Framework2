package com.example.common.widget.xrecyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.DefaultItemAnimator;

import com.example.base.utils.DisplayUtil;
import com.example.base.widget.SimpleViewGroup;
import com.example.common.R;
import com.example.common.widget.empty.EmptyLayout;
import com.example.common.widget.xrecyclerview.callback.OnEmptyClickListener;
import com.example.common.widget.xrecyclerview.manager.SCommonItemDecoration;
import com.example.common.widget.xrecyclerview.refresh.XRefreshLayout;
import com.example.common.widget.xrecyclerview.refresh.callback.OnXRefreshBottomListener;
import com.example.common.widget.xrecyclerview.refresh.callback.OnXRefreshListener;
import com.example.common.widget.xrecyclerview.refresh.callback.OnXRefreshTopListener;
import com.example.common.widget.xrecyclerview.refresh.callback.SwipeRefreshLayoutDirection;

/**
 * author: wyb
 * date: 2017/11/20.
 * <p>
 * 一般自定义view或viewGroup基本上都会去实现onMeasure、onLayout、onDraw方法，还有另外两个方法是onFinishInflate和onSizeChanged。
 * onFinishInflate方法只有在布局文件中加载view实例会回调，如果直接new一个view的话是不会回调的。
 */
@SuppressLint("InflateParams")
public class XRecyclerView extends SimpleViewGroup {
    private EmptyLayout empty;//自定义封装的空布局
    private XRefreshLayout refresh;//刷新控件 类型1才有
    private DetectionRecyclerView recyclerView;//数据列表
    private OnEmptyClickListener onEmptyClickListener;//空布局点击
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
    public void draw() {
        if (detectionInflate()) initRefreshType(refreshType);
    }

    private void initRefreshType(int refreshType) {
        View view = null;
        switch (refreshType) {
            //不带刷新
            case 0:
                view = LayoutInflater.from(getContext()).inflate(R.layout.view_xrecyclerview, null);
                recyclerView = view.findViewById(R.id.d_rv);
                if (0 != emptyType) {
                    empty = new EmptyLayout(getContext());
                    recyclerView.setEmptyView(empty.setListView(recyclerView));
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    empty.setOnEmptyRefreshListener(() -> {
                        if (null != onEmptyClickListener) {
                            onEmptyClickListener.onClickListener();
                        }
                    });
                }
                break;
            //带刷新
            case 1:
                view = LayoutInflater.from(getContext()).inflate(R.layout.view_xrecyclerview_refresh, null);
                empty = view.findViewById(R.id.el);
                refresh = view.findViewById(R.id.x_refresh);
                recyclerView = view.findViewById(R.id.d_rv);
                //设置刷新的方式，默认上下皆有
                switch (refreshDirection) {
                    case 0:
                        refresh.setDirection(SwipeRefreshLayoutDirection.TOP);
                        break;
                    case 1:
                        refresh.setDirection(SwipeRefreshLayoutDirection.BOTTOM);
                        break;
                    case 2:
                        refresh.setDirection(SwipeRefreshLayoutDirection.BOTH);
                        break;
                }
                recyclerView.setHasFixedSize(true);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                if (0 != emptyType) {
                    empty.setOnEmptyRefreshListener(() -> {
                        if (null != onEmptyClickListener) {
                            onEmptyClickListener.onClickListener();
                        }
                    });
                } else {
                    empty.setVisibility(View.GONE);
                }
                break;
        }
        addView(view);
    }

    //当数据正在加载的时候显示
    public void showLoading() {
        if (0 != emptyType) {
            empty.showLoading();
        }
    }

    //当数据为空时(显示需要显示的图片，以及内容字)
    public void showEmpty() {
        if (0 != emptyType) {
            empty.showEmpty();
        }
    }

    //当数据为空时(显示需要显示的图片，以及内容字)---传入图片
    public void showEmpty(int imgInt, String emptyStr) {
        if (0 != emptyType) {
            empty.showEmpty(imgInt, emptyStr);
        }
    }

    //当数据错误时（没有网络）
    public void showError() {
        if (0 != emptyType) {
            empty.showError();
        }
    }

    public void hideEmpty() {
        setEmptyVisibility(View.GONE);
    }

    //类型1的时候才会显示
    public void setEmptyVisibility(int visibility) {
        if (refreshType == 1 && 0 != emptyType) {
            empty.setVisibility(visibility);
        }
    }

    //设置禁止刷新
    public void finishRefreshing() {
        if (refreshType == 1) {
            refresh.finishRefreshing();
        }
    }

    //修改背景颜色
    public void setEmptyBackgroundColor(int color) {
        empty.setBackgroundColor(color);
    }

    //选择下标
    public void scrollToPosition(int position) {
        recyclerView.scrollToPosition(position);
    }

    //获取空布局
    public EmptyLayout getEmptyView() {
        return empty;
    }

    //添加分隔线
    public void addItemDecoration(int horizontalSpace, int verticalSpace, boolean hasHorizontalEdge, boolean hasVerticalEdge) {
        SparseArray<SCommonItemDecoration.ItemDecorationProps> propMap = new SparseArray<>();
        SCommonItemDecoration.ItemDecorationProps prop1 = new SCommonItemDecoration.ItemDecorationProps(DisplayUtil.dip2px(getContext(), horizontalSpace), DisplayUtil.dip2px(getContext(), verticalSpace), hasHorizontalEdge, hasVerticalEdge);
        propMap.put(0, prop1);
        recyclerView.addItemDecoration(new SCommonItemDecoration(propMap));
    }

    //返回页面整体
    public DetectionRecyclerView getRecyclerView() {
        return recyclerView;
    }

    //空布局刷新
    public void setOnEmptyViewClickListener(OnEmptyClickListener onEmptyClickListener) {
        this.onEmptyClickListener = onEmptyClickListener;
    }

    //刷新页面刷新
    public void setOnXRefreshTopListener(OnXRefreshTopListener onXRefreshTopListener) {
        if (refreshType == 1) {
            refresh.setOnRefreshListener(onXRefreshTopListener);
        }
    }

    public void setOnXRefreshBottomListener(OnXRefreshBottomListener onXRefreshBottomListener) {
        if (refreshType == 1) {
            refresh.setOnRefreshListener(onXRefreshBottomListener);
        }
    }

    public void setOnXRefreshListener(OnXRefreshListener onXRefreshListener) {
        if (refreshType == 1) {
            refresh.setOnRefreshListener(onXRefreshListener);
        }
    }

}
