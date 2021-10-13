package com.example.base.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.core.widget.NestedScrollView;

import com.example.base.utils.LogUtil;

/**
 * Created by wyb on 2017/6/28.
 * 可监听滑动范围的scrollview
 */
public class XScrollView extends NestedScrollView {
    private boolean isTop;//是否滑动到顶端
    private int touchSlop, downY;
    private OnScrollToBottomListener onScrollBottomListener;

    public XScrollView(Context context) {
        super(context);
        initialize();
    }

    public XScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public XScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (scrollY != 0 && null != onScrollBottomListener && isTop()) {
            onScrollBottomListener.onScrollBottomListener(clampedY);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isTop = false;
                downY = (int) e.getRawY();
                LogUtil.i("-----::----downY-----::" + downY);
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) e.getRawY();
                LogUtil.i("-----::----moveY-----::" + moveY);
                //判断是向下滑动，才设置为true
                isTop = downY - moveY > 0;
                if (Math.abs(moveY - downY) > touchSlop) return true;
        }
        return super.onInterceptTouchEvent(e);
    }

    /**
     * 是否滑到顶
     * @return
     */
    public boolean isTop() {
        return isTop;
    }

    public void setOnScrollToBottomListener(OnScrollToBottomListener onScrollBottomListener) {
        this.onScrollBottomListener = onScrollBottomListener;
    }

    public interface OnScrollToBottomListener {

        void onScrollBottomListener(boolean isBottom);

    }

}