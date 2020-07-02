package com.example.mvvm.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import com.example.mvvm.BR;
import com.example.mvvm.R;

/**
 * Created by WangYanBin on 2020/6/4.
 */
public class TestListModel extends BaseObservable {
    private String title;
    private String describe;
    private String avatar;

    public TestListModel(String title, String describe, String avatar) {
        this.title = title;
        this.describe = describe;
        this.avatar = avatar;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        notifyPropertyChanged(BR.avatar);
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
        notifyPropertyChanged(BR.describe);
    }

    @BindingAdapter(value = "app:resource", requireAll = true)
    public static void setResource(ImageView image, String url) {
        Toast.makeText(image.getContext(), url, Toast.LENGTH_SHORT).show();
        image.setBackgroundResource(R.mipmap.ic_launcher_round);
    }

//    public  void setBackgroundResource(View image) {
//        image.setBackgroundResource(R.mipmap.ic_launcher);
//    }

}
