package com.example.common.widget.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.common.R;
import com.example.common.base.BaseDialog;
import com.example.common.databinding.ViewDialogLoadingBinding;

/**
 * Created by wyb on 2017/6/28.
 * 加载动画view
 */
public class LoadingDialog extends BaseDialog<ViewDialogLoadingBinding> {

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.loadingStyle);
        initialize();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.view_dialog_loading;
    }

    public void show(boolean flag) {
        setCancelable(flag);
        if (!isShowing()) {
            show();
        }
    }

    public void hide() {
        if (isShowing()) {
            dismiss();
        }
    }

}