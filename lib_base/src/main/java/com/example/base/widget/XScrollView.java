package com.example.base.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

import com.example.base.utils.LogUtil;


/**
 * Created by wyb on 2017/6/28.
 * 可监听滑动范围的scrollview
 */
public class XScrollView extends ScrollView {
    private int mTouchSlop, downY;
    private boolean isTop;//是否滑动到顶端
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
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
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
                setTop(false);
                downY = (int) e.getRawY();
                LogUtil.i("-----::----downY-----::", downY + "");
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) e.getRawY();
                LogUtil.i("-----::----moveY-----::", moveY + "");
                //判断是向下滑动，才设置为true
                if (downY - moveY > 0) {
                    setTop(true);
                } else {
                    setTop(false);
                }
                if (Math.abs(moveY - downY) > mTouchSlop) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(e);
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }

    public void setOnScrollToBottomListener(OnScrollToBottomListener onScrollBottomListener) {
        this.onScrollBottomListener = onScrollBottomListener;
    }

    public interface OnScrollToBottomListener {

        void onScrollBottomListener(boolean isBottom);

    }

}