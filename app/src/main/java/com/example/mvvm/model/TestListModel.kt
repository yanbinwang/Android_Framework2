package com.example.mvvm.model

import androidx.databinding.BaseObservable

/**
 * Created by WangYanBin on 2020/7/7.
 */
class TestListModel(
    var title: String? = null,
    var describe: String? = null,
    var avatar: Int
) : BaseObservable()

//public class TestListModel extends BaseObservable {
//    private String title;
//    private String describe;
//    private int avatar;
//
//    public TestListModel(String title, String describe, int avatar) {
//        this.title = title;
//        this.describe = describe;
//        this.avatar = avatar;
//    }
//
//    @Bindable
//    public int getAvatar() {
//        return avatar;
//    }
//
//    public void setAvatar(int avatar) {
//        this.avatar = avatar;
//        notifyPropertyChanged(BR.avatar);
//    }
//
//    @Bindable
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//        notifyPropertyChanged(BR.title);
//    }
//
//    @Bindable
//    public String getDescribe() {
//        return describe;
//    }
//
//    public void setDescribe(String describe) {
//        this.describe = describe;
//        notifyPropertyChanged(BR.describe);
//    }
//
//    @BindingAdapter(value = "app:resource", requireAll = true)
//    public static void setResource(ImageView image, String url) {
//        Toast.makeText(image.getContext(), url, Toast.LENGTH_SHORT).show();
//        image.setBackgroundResource(R.mipmap.ic_launcher_round);
//    }
//
////    public  void setBackgroundResource(View image) {
////        image.setBackgroundResource(R.mipmap.ic_launcher);
////    }
//
//}