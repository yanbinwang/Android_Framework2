package com.example.common.widget.xrecyclerview.refresh;

import android.content.Context;
import android.view.View;

import com.example.common.R;
import com.lcodecore.tkrefreshlayout.IBottomView;

/**
 * Created by WangYanBin on 2020/9/17.
 * 自定义刷新-底部
 */
public class RefreshBottomView implements IBottomView {
    private Context context;

    public RefreshBottomView(Context context) {
        this.context = context;
    }

    @Override
    public View getView() {
        return View.inflate(context, R.layout.view_refresh_bottom, null);
    }

    @Override
    public void onPullingUp(float fraction, float maxBottomHeight, float bottomHeight) {
    }

    @Override
    public void onPullReleasing(float fraction, float maxBottomHeight, float bottomHeight) {
    }

    @Override
    public void startAnim(float maxBottomHeight, float bottomHeight) {
    }

    @Override
    public void onFinish() {
    }

    @Override
    public void reset() {
    }

}