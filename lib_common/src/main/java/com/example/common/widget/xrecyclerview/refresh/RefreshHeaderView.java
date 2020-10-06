package com.example.common.widget.xrecyclerview.refresh;

import android.content.Context;
import android.view.View;

import com.example.common.R;
import com.lcodecore.tkrefreshlayout.IHeaderView;
import com.lcodecore.tkrefreshlayout.OnAnimEndListener;

/**
 * Created by WangYanBin on 2020/9/17.
 * 自定义刷新-头部
 */
public class RefreshHeaderView implements IHeaderView {
    private Context context;

    public RefreshHeaderView(Context context) {
        this.context = context;
    }

    //获取刷新的整体view
    @Override
    public View getView() {
        View rootView = View.inflate(context, R.layout.view_refresh_header, null);

        return rootView;
    }

    //当控件下拉时触发
    @Override
    public void onPullingDown(float fraction, float maxHeadHeight, float headHeight) {

    }

    //刷新被复原时--复位
    @Override
    public void onPullReleasing(float fraction, float maxHeadHeight, float headHeight) {

    }

    //触发执行动画时，文字和图片的样式
    @Override
    public void startAnim(float maxHeadHeight, float headHeight) {
    }

    //动画执行完毕时，结束
    @Override
    public void onFinish(OnAnimEndListener animEndListener) {
        animEndListener.onAnimEnd();
    }

    //重置复位
    @Override
    public void reset() {
    }

}
