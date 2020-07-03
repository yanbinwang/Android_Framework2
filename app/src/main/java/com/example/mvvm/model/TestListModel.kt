package com.example.mvvm.model

/**
 * Created by WangYanBin on 2020/7/3.
 */
class TestListModel(
    var title: String? = null,
    var describe: String? = null,
    var avatar: String? = null
)

//public class TestListModel extends BaseObservable {
//    private String title;
//    private String describe;
//    private String avatar;
//
//    public TestListModel(String title, String describe, String avatar) {
//        this.title = title;
//        this.describe = describe;
//        this.avatar = avatar;
//    }
//
//    @Bindable
//    public String getAvatar() {
//        return avatar;
//    }
//
//    public void setAvatar(String avatar) {
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