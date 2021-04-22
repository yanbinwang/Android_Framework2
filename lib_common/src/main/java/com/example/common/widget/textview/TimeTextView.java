package com.example.common.widget.textview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.base.utils.TimeTaskHelper;

import java.text.MessageFormat;

/**
 * Created by wangyanbin
 * 倒计时textview
 * 配置enable的xml和默認text文案即可
 */
@SuppressLint("AppCompatCustomView")
public class TimeTextView extends TextView {

    public TimeTextView(Context context) {
        super(context);
        initialize();
    }

    public TimeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TimeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    //公共属性，可在此配置
    private void initialize() {
        setGravity(Gravity.CENTER);
    }

    public void countDown() {
        countDown(60);
    }

    public void countDown(long second) {
        TimeTaskHelper.startCountDown(second, new TimeTaskHelper.OnCountDownListener() {
            @Override
            public void onFinish() {
                setEnabled(true);
                setText("重发验证码");
            }

            @Override
            public void onTick(long second) {
                setEnabled(false);
                setText(MessageFormat.format("已发送{0}S", second));
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        TimeTaskHelper.stopCountDown();
    }

}
