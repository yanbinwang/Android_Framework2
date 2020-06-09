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

import com.example.common.R;
import com.example.framework.utils.AnimationLoader;
import com.example.common.widget.dialog.callback.OnAppConfirmDialogListener;
import com.example.common.widget.dialog.callback.OnAppConfirmOrCancelDialogListener;


/**
 * author: wyb
 * date: 2017/8/25.
 * 类似苹果的弹出窗口类
 */
@SuppressLint("InflateParams")
public class AppDialog {
    private static AnimationSet mAnimIn, mAnimOut;

    //包含確定取消的提示框
    public static void show(Context context, String tipStr, String contentStr, String sureStr, String cancelStr, OnAppConfirmOrCancelDialogListener onAppConfirmOrCancelDialogListener) {
        //定义开始和退出的动画
        mAnimIn = AnimationLoader.getInAnimation(context);
        mAnimOut = AnimationLoader.getOutAnimation(context);
        //得到dialog的布局
        Dialog myDialog = new Dialog(context, R.style.appDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.view_dialog_confirm_or_cancel, null);
        LinearLayout dialogContainerLin = view.findViewById(R.id.ll_dialog_container);
        myDialog.setContentView(dialogContainerLin, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //当布局show出来的时候执行开始动画
        myDialog.setOnShowListener(dialog -> view.startAnimation(mAnimIn));
        //当布局销毁时执行结束动画
        myDialog.setOnDismissListener(dialog -> view.startAnimation(mAnimOut));
        myDialog.show();

        //标题
        TextView dialogTipTxt = myDialog.findViewById(R.id.tv_dialog_tip);
        //内容
        TextView dialogContentTxt = myDialog.findViewById(R.id.tv_dialog_container);
        //确定
        TextView dialogSureTxt = myDialog.findViewById(R.id.tv_dialog_sure);
        //取消
        TextView dialogCancelTxt = myDialog.findViewById(R.id.tv_dialog_cancel);

        //如果没有传入标题字段,则隐藏标题view
        if (TextUtils.isEmpty(tipStr)) {
            dialogTipTxt.setVisibility(View.GONE);
        }

        //对控件赋值
        dialogTipTxt.setText(TextUtils.isEmpty(tipStr) ? "" : tipStr);
        dialogContentTxt.setText(TextUtils.isEmpty(contentStr) ? "" : contentStr);
        dialogSureTxt.setText(TextUtils.isEmpty(sureStr) ? "" : sureStr);
        dialogCancelTxt.setText(TextUtils.isEmpty(cancelStr) ? "" : cancelStr);

//        if (tipStr.contains("发现新版本") || tipStr.equals("安装应用")) {
//            dialogContentTxt.setGravity(Gravity.START);
//            myDialog.setOnKeyListener((dialog, keyCode, event) -> true);
//        }

        //点击了取消按钮的回调
        dialogCancelTxt.setOnClickListener(v -> {
            myDialog.dismiss();
            if (null != onAppConfirmOrCancelDialogListener) {
                onAppConfirmOrCancelDialogListener.onDialogCancel();
            }
        });

        //点击了确定按钮的回调
        dialogSureTxt.setOnClickListener(v -> {
            myDialog.dismiss();
            if (null != onAppConfirmOrCancelDialogListener) {
                onAppConfirmOrCancelDialogListener.onDialogConfirm();
            }

        });
    }

    //包含確定的提示框
    public static void show(Context context, String tipStr, String contentStr, String sureStr, OnAppConfirmDialogListener onAppConfirmDialogListener) {
        //定义开始和退出的动画
        mAnimIn = AnimationLoader.getInAnimation(context);
        mAnimOut = AnimationLoader.getOutAnimation(context);
        //得到dialog的布局
        Dialog myDialog = new Dialog(context, R.style.appDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.view_dialog_confirm, null);
        LinearLayout dialogContainerLin = view.findViewById(R.id.ll_dialog_container);
        myDialog.setContentView(dialogContainerLin, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        //当布局show出来的时候执行开始动画
        myDialog.setOnShowListener(dialog -> view.startAnimation(mAnimIn));
        //当布局销毁时执行结束动画
        myDialog.setOnDismissListener(dialog -> view.startAnimation(mAnimOut));
        myDialog.show();

        //标题
        TextView dialogTipTxt = myDialog.findViewById(R.id.tv_dialog_tip);
        //内容
        TextView dialogContentTxt = myDialog.findViewById(R.id.tv_dialog_container);
        //确定
        TextView dialogSureTxt = myDialog.findViewById(R.id.tv_dialog_sure);

        //如果没有传入标题字段,则隐藏标题view
        if (TextUtils.isEmpty(tipStr)) {
            dialogTipTxt.setVisibility(View.GONE);
        }

        //对控件赋值
        dialogTipTxt.setText(TextUtils.isEmpty(tipStr) ? "" : tipStr);
        dialogContentTxt.setText(TextUtils.isEmpty(contentStr) ? "" : contentStr);
        dialogSureTxt.setText(TextUtils.isEmpty(sureStr) ? "" : sureStr);

//        if (tipStr.contains("发现新版本")) {
//            dialogContentTxt.setGravity(Gravity.START);
//            myDialog.setOnKeyListener((dialog, keyCode, event) -> true);
//        }

        //点击了确定按钮的回调
        dialogSureTxt.setOnClickListener(v -> {
            myDialog.dismiss();
            if (null != onAppConfirmDialogListener) {
                onAppConfirmDialogListener.onDialogConfirm();
            }
        });
    }

}