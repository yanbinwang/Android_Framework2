package com.example.common.widget.dialog;

import android.content.Context;

import com.example.common.R;
import com.example.common.base.BaseDialog;
import com.example.common.databinding.ViewDialogLoadingBinding;

/**
 * Created by wyb on 2017/6/28.
 * 加载动画view
 */
public class LoadingDialog extends BaseDialog<ViewDialogLoadingBinding> {

    public LoadingDialog(Context context) {
        super(context, R.style.loadingStyle);
        initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();
        setOnDismissListener(dialog -> binding.progress.stopSpinning());
    }

    @Override
    public void show() {
        super.show();
        if (!binding.progress.isSpinning()) {
            binding.progress.spin();
        }
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