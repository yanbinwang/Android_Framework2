package com.example.mvvm.bridge;

import androidx.lifecycle.MutableLiveData;

import com.example.common.base.bridge.BaseViewModel;
import com.example.mvvm.databinding.ActivityUserInfoBinding;
import com.example.mvvm.model.UserInfoModel;

/**
 * Created by WangYanBin on 2020/6/3.
 */
public class UserInfoViewModel extends BaseViewModel {
    public MutableLiveData<UserInfoModel> userInfoModel = new MutableLiveData<>();

    //接收传递信息
    public void getPageModel() {
        UserInfoModel model = (UserInfoModel) activity.get().getIntent().getSerializableExtra("model");
        userInfoModel.postValue(model);
    }

}
