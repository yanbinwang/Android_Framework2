package com.example.framework.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

import com.example.framework.utils.LogUtil;


/**
 * Created by wyb on 2017/6/28.
 * 可监听滑动范围的scrollview
 */
public class XScrollView extends ScrollView {
    private int downX;
    private int downY;
    private int mTouchSlop;
    private boolean isTop = false;//是不是滑动到了最低端 ；使用这个方法，解决了上拉加载的问题
    private OnScrollToBottomListener onScrollToBottom;

    public XScrollView(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public XScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public XScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (scrollY != 0 && null != onScrollToBottom && isTop()) {
            onScrollToBottom.onScrollBottomListener(clampedY);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTop(false);
                downX = (int) e.getRawX();
                downY = (int) e.getRawY();
                LogUtil.INSTANCE.i("-----::----downY-----::", downY + "");
                break;
            case MotionEvent.ACTION_MOVE:
                int moveY = (int) e.getRawY();
                LogUtil.INSTANCE.i("-----::----moveY-----::", moveY + "");
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

    public void setOnScrollToBottomLintener(OnScrollToBottomListener listener) {
        onScrollToBottom = listener;
    }

    public interface OnScrollToBottomListener {
        void onScrollBottomListener(boolean isBottom);
    }

}