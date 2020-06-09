package com.example.mvvm.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.mvvm.BR;

import java.io.Serializable;

/**
 * Created by WangYanBin on 2020/6/3.
 */
public class UserInfoModel extends BaseObservable implements Serializable {
     private String name;
     private int age;
     private String avatar;

    public UserInfoModel(String name, int age, String avatar) {
        this.name = name;
        this.age = age;
        this.avatar = avatar;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        notifyPropertyChanged(BR.age);
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        notifyPropertyChanged(BR.avatar);
    }
}
