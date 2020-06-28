package com.example.common.widget.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.common.R;
import com.example.common.widget.dialog.callback.OnConfirmDialogListener;
import com.example.common.widget.dialog.callback.OnConfirmOrCancelDialogListener;
import com.example.framework.utils.AnimationLoader;


/**
 * author: wyb
 * date: 2017/8/25.
 * 类似苹果的弹出窗口类
 */
@SuppressLint("InflateParams")
public class AppDialog extends Dialog {
    private AnimationSet mAnimIn, mAnimOut;
    private OnConfirmOrCancelDialogListener onConfirmOrCancelDialogListener;
    private OnConfirmDialogListener onConfirmDialogListener;

    public AppDialog(@NonNull Context context) {
        super(context, R.style.appDialogStyle);
        //定义开始和退出的动画
        mAnimIn = AnimationLoader.getInAnimation(context);
        mAnimOut = AnimationLoader.getOutAnimation(context);
    }

    //包含確定取消的提示框
    public AppDialog setParams(String tipText, String contentText, String sureText, String cancelText) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_confirm_or_cancel, null);
        setContentView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //当布局show出来的时候执行开始动画
        setOnShowListener(dialog -> view.startAnimation(mAnimIn));
        //当布局销毁时执行结束动画
        setOnDismissListener(dialog -> view.startAnimation(mAnimOut));
        //标题
        TextView dialogTipTxt = findViewById(R.id.tv_dialog_tip);
        //内容
        TextView dialogContentTxt = findViewById(R.id.tv_dialog_container);
        //确定
        TextView dialogSureTxt = findViewById(R.id.tv_dialog_sure);
        //取消
        TextView dialogCancelTxt = findViewById(R.id.tv_dialog_cancel);

        //如果没有传入标题字段,则隐藏标题view
        if (TextUtils.isEmpty(tipText)) {
            dialogTipTxt.setVisibility(View.GONE);
        }

        //对控件赋值
        dialogTipTxt.setText(TextUtils.isEmpty(tipText) ? "" : tipText);
        dialogContentTxt.setText(TextUtils.isEmpty(contentText) ? "" : contentText);
        dialogSureTxt.setText(TextUtils.isEmpty(sureText) ? "" : sureText);
        dialogCancelTxt.setText(TextUtils.isEmpty(cancelText) ? "" : cancelText);

//        if (tipText.contains("发现新版本") || tipText.equals("安装应用")) {
//            dialogContentTxt.setGravity(Gravity.START);
//            setOnKeyListener((dialog, keyCode, event) -> true);
//        }

        //点击了取消按钮的回调
        dialogCancelTxt.setOnClickListener(v -> {
            dismiss();
            if (null != onConfirmOrCancelDialogListener) {
                onConfirmOrCancelDialogListener.onDialogCancel();
            }
        });

        //点击了确定按钮的回调
        dialogSureTxt.setOnClickListener(v -> {
            dismiss();
            if (null != onConfirmOrCancelDialogListener) {
                onConfirmOrCancelDialogListener.onDialogConfirm();
            }
        });
        return this;
    }

    //包含確定的提示框
    public AppDialog setParams(String tipText, String contentText, String sureText) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_confirm, null);
        setContentView(view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //当布局show出来的时候执行开始动画
        setOnShowListener(dialog -> view.startAnimation(mAnimIn));
        //当布局销毁时执行结束动画
        setOnDismissListener(dialog -> view.startAnimation(mAnimOut));

        //标题
        TextView dialogTipTxt = findViewById(R.id.tv_dialog_tip);
        //内容
        TextView dialogContentTxt = findViewById(R.id.tv_dialog_container);
        //确定
        TextView dialogSureTxt = findViewById(R.id.tv_dialog_sure);

        //如果没有传入标题字段,则隐藏标题view
        if (TextUtils.isEmpty(tipText)) {
            dialogTipTxt.setVisibility(View.GONE);
        }

        //对控件赋值
        dialogTipTxt.setText(TextUtils.isEmpty(tipText) ? "" : tipText);
        dialogContentTxt.setText(TextUtils.isEmpty(contentText) ? "" : contentText);
        dialogSureTxt.setText(TextUtils.isEmpty(sureText) ? "" : sureText);

//        if (tipText.contains("发现新版本")) {
//            dialogContentTxt.setGravity(Gravity.START);
//            setOnKeyListener((dialog, keyCode, event) -> true);
//        }

        //点击了确定按钮的回调
        dialogSureTxt.setOnClickListener(v -> {
            dismiss();
            if (null != onConfirmDialogListener) {
                onConfirmDialogListener.onDialogConfirm();
            }
        });
        return this;
    }

    public AppDialog setOnConfirmOrCancelDialogListener(OnConfirmOrCancelDialogListener onConfirmOrCancelDialogListener) {
        this.onConfirmOrCancelDialogListener = onConfirmOrCancelDialogListener;
        return this;
    }

    public AppDialog setOnConfirmDialogListener(OnConfirmDialogListener onConfirmDialogListener) {
        this.onConfirmDialogListener = onConfirmDialogListener;
        return this;
    }

    public static AppDialog with(Context context) {
        return new AppDialog(context);
    }

}