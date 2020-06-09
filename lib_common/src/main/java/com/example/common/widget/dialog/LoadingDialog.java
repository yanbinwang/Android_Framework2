package com.example.common.widget.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.example.common.R;


/**
 * Created by wyb on 2017/6/28.
 * 加载动画view
 */
@SuppressLint("InflateParams")
public class LoadingDialog {
    private View view;
    private Context context;
    private Dialog loadingDialog;

    public LoadingDialog(Context context) {
        super();
        this.context = context;
    }

    public void show(boolean isClose) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.view_dialog_loading, null);
//            TextView msgTxt = view.findViewById(R.id.msg_txt);
//            if (!TextUtils.isEmpty(str)) {
//                msgTxt.setText(str);
//            }
        }
        if (loadingDialog == null) {
            loadingDialog = new Dialog(context, R.style.loadingStyle);
            loadingDialog.setContentView(view, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            loadingDialog.setCancelable(isClose);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    public void hide() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

}
