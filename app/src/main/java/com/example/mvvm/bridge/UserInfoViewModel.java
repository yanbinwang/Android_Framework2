package com.example.mvvm.bridge;

import com.example.common.base.bridge.BaseViewModel;
import com.example.mvvm.databinding.ActivityUserInfoBinding;
import com.example.mvvm.model.UserInfoModel;

/**
 * Created by WangYanBin on 2020/6/3.
 */
public class UserInfoViewModel extends BaseViewModel {
    private UserInfoModel model;

//    //接收传递信息
//    public void getPageModel() {
//        ActivityUserInfoBinding binding = getBinding();
//        model = (UserInfoModel) activity.get().getIntent().getSerializableExtra("model");
//        binding.setModel(model);
//    }

//    @BindingAdapter(value = "bind:loadImage", requireAll = true)
//    public static void loadImage(ImageView image, String url) {
//        Toast.makeText(context.get(), url, Toast.LENGTH_SHORT).show();
//        image.setBackgroundResource(R.mipmap.ic_launcher_round);
//    }

}
