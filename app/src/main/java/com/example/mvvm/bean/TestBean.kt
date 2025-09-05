package com.example.mvvm.bean

data class TestBean(
    var userId: String? = null,
    var name: String? = null,
    var phone: String? = null
)

///**
// * @Parcelize 和 Parcelable 的作用
// * @Parcelize 是 Kotlin 提供的注解，用于自动生成 Parcelable 接口的实现代码，方便对象在组件间（如 Activity、Fragment 之间）通过 Intent 传递。
// * 被 @Parcelize 标注的类会自动将所有属性序列化到 Parcel 中。
// * @Bindable 的作用
// * @Bindable 是 Data Binding 库的注解，用于标记数据类中的属性。
// * 当标记了 @Bindable 的属性发生变化时，会通知 UI 刷新（通过 notifyPropertyChanged() 方法）。
// * 通常用于实现数据驱动 UI 的双向绑定。
// * @IgnoredOnParcel 的作用
// * 这个注解用于告诉 @Parcelize 不要将某个属性序列化到 Parcel 中。
// * 当一个属性不需要在组件间传递，或者序列化没有意义时（例如临时状态、仅用于 UI 绑定的属性），就需要用它排除。
// */
//@Parcelize
//data class User(
//    val id: String,
//    val name: String
//) : Parcelable, BaseObservable() {
//    // 仅用于UI绑定，不需要序列化的属性
//    @IgnoredOnParcel
//    @Bindable
//    var isSelected: Boolean = false
//        set(value) {
//            field = value
//            notifyPropertyChanged(BR.isSelected) // 通知UI更新
//        }
//}