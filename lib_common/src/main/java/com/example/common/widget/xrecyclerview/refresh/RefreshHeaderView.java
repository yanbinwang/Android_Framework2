package com.example.common.widget.xrecyclerview.refresh;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.common.R;
import com.lcodecore.tkrefreshlayout.IHeaderView;
import com.lcodecore.tkrefreshlayout.OnAnimEndListener;

/**
 * Created by WangYanBin on 2020/9/17.
 * 自定义刷新-头部
 */
public class RefreshHeaderView extends ViewGroup implements IHeaderView {
    private ImageView ivArrow;
    private ImageView ivLoading;
    private TextView tvContent;
    private final String pullDownStr = "下拉刷新";
    private final String releaseRefreshStr = "释放刷新";
    private final String refreshingStr = "正在刷新";

    public RefreshHeaderView(Context context) {
        super(context);
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

    //获取刷新的整体view
    @Override
    public View getView() {
        View rootView = View.inflate(getContext(), R.layout.view_refresh_header, null);
        ivArrow = rootView.findViewById(R.id.iv_arrow);
        ivLoading = rootView.findViewById(R.id.iv_loading);
        tvContent = rootView.findViewById(R.id.tv_content);
        return rootView;
    }

    //当控件下拉时触发
    @Override
    public void onPullingDown(float fraction, float maxHeadHeight, float headHeight) {
        if (fraction < 1f) tvContent.setText(pullDownStr);
        if (fraction > 1f) tvContent.setText(releaseRefreshStr);
        ivArrow.setRotation(fraction * headHeight / maxHeadHeight * 180);
    }

    //刷新被复原时--复位
    @Override
    public void onPullReleasing(float fraction, float maxHeadHeight, float headHeight) {
        if (fraction < 1f) {
            tvContent.setText(pullDownStr);
            ivArrow.setRotation(fraction * headHeight / maxHeadHeight * 180);
            if (ivArrow.getVisibility() == GONE) {
                ivArrow.setVisibility(VISIBLE);
                ivLoading.setVisibility(GONE);
            }
        }
    }

    //触发执行动画时，文字和图片的样式
    @Override
    public void startAnim(float maxHeadHeight, float headHeight) {
        tvContent.setText(refreshingStr);
        ivArrow.setVisibility(GONE);
        ivLoading.setVisibility(VISIBLE);
        ((AnimationDrawable) ivLoading.getDrawable()).start();
    }

    //动画执行完毕时，结束
    @Override
    public void onFinish(OnAnimEndListener animEndListener) {
        animEndListener.onAnimEnd();
    }

    //重置复位
    @Override
    public void reset() {
        ivArrow.setVisibility(VISIBLE);
        ivLoading.setVisibility(GONE);
        tvContent.setText(pullDownStr);
    }

}
